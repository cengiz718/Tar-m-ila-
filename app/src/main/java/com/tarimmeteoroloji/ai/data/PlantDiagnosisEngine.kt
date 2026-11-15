package com.tarimmeteoroloji.ai.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import com.tarimmeteoroloji.ai.model.DiseaseProfile
import com.tarimmeteoroloji.ai.model.PlantDiagnosisResult
import com.tarimmeteoroloji.ai.model.SprayEvaluation
import com.tarimmeteoroloji.ai.model.SprayStatusLevel
import com.tarimmeteoroloji.ai.model.SymptomPattern
import com.tarimmeteoroloji.ai.model.WeatherSnapshot
import kotlin.math.roundToInt

object PlantDiagnosisEngine {

    private data class FrameMetrics(
        val healthyGreen: Int,
        val powderyWhite: Int,
        val yellowRust: Int,
        val necroticSpot: Int,
        val totalPlantSamples: Int
    )

    fun analyzeMedia(
        context: Context,
        uri: Uri,
        isVideo: Boolean,
        selectedCropId: String?,
        weather: WeatherSnapshot?,
        sprayEval: SprayEvaluation?
    ): PlantDiagnosisResult {
        val frames: List<Bitmap> = if (isVideo) {
            extractVideoFrames(context, uri, maxFrames = 5)
        } else {
            listOfNotNull(decodeScaledBitmap(context, uri))
        }

        require(frames.isNotEmpty()) {
            if (isVideo) "Video kareleri okunamadı." else "Fotoğraf yüklenemedi."
        }

        var sumHealthy = 0
        var sumPowdery = 0
        var sumYellow = 0
        var sumNecrotic = 0
        var sumTotal = 0

        for (frame in frames) {
            val m = analyzeBitmapPixels(frame)
            sumHealthy += m.healthyGreen
            sumPowdery += m.powderyWhite
            sumYellow += m.yellowRust
            sumNecrotic += m.necroticSpot
            sumTotal += m.totalPlantSamples
        }

        val denom = sumTotal.coerceAtLeast(1)
        val healthyPct = ((sumHealthy * 100.0) / denom).roundToInt().coerceIn(0, 100)
        val powderyPct = ((sumPowdery * 100.0) / denom).roundToInt().coerceIn(0, 100)
        val yellowPct = ((sumYellow * 100.0) / denom).roundToInt().coerceIn(0, 100)
        val necroticPct = ((sumNecrotic * 100.0) / denom).roundToInt().coerceIn(0, 100)

        val affectedPct = (powderyPct + yellowPct + necroticPct).coerceIn(0, 100)

        val dominantPattern = when {
            affectedPct < 10 -> SymptomPattern.HEALTHY
            powderyPct >= yellowPct && powderyPct >= necroticPct && powderyPct >= 8 ->
                SymptomPattern.POWDERY_WHITE
            necroticPct >= yellowPct && necroticPct >= 8 ->
                SymptomPattern.NECROTIC_SPOT
            yellowPct >= 8 ->
                SymptomPattern.YELLOW_RUST
            else -> SymptomPattern.HEALTHY
        }

        val severityLabel = when {
            dominantPattern == SymptomPattern.HEALTHY -> "Düşük / Sağlıklı"
            affectedPct >= 40 -> "Yüksek Şiddet (%$affectedPct belirti)"
            affectedPct >= 20 -> "Orta Şiddet (%$affectedPct belirti)"
            else -> "Başlangıç Düzeyi (%$affectedPct belirti)"
        }

        val curTemp = weather?.current?.temperature ?: 20.0
        val curHum = weather?.current?.humidity ?: 65

        val ranked = rankDiseases(
            pattern = dominantPattern,
            selectedCropId = selectedCropId,
            curTemp = curTemp,
            curHum = curHum
        )

        val primary = ranked.first()
        val secondary = ranked.getOrNull(1)

        val rawConfidence = when {
            dominantPattern == SymptomPattern.HEALTHY -> (92 - affectedPct).coerceIn(65, 95)
            selectedCropId != null && primary.cropIds.contains(selectedCropId) ->
                (68 + (affectedPct / 2)).coerceIn(68, 94)
            else -> (58 + (affectedPct / 2)).coerceIn(58, 88)
        }

        val weatherAdvice = buildWeatherTimingAdvice(
            isHealthy = dominantPattern == SymptomPattern.HEALTHY,
            sprayEval = sprayEval,
            weather = weather
        )

        val cropLabel = selectedCropId?.let { id ->
            CropCatalog.crops.firstOrNull { it.id == id }?.let { "${it.emoji} ${it.name}" }
        } ?: "Genel Bitki (Otomatik)"

        val preview = frames.firstOrNull()
        // Recycle extra extracted frames while keeping preview alive
        for (i in 1 until frames.size) {
            frames[i].recycle()
        }

        return PlantDiagnosisResult(
            isVideo = isVideo,
            framesAnalyzed = frames.size,
            previewBitmap = preview,
            selectedCropName = cropLabel,
            healthyGreenPct = healthyPct,
            powderyWhitePct = powderyPct,
            yellowRustPct = yellowPct,
            necroticSpotPct = necroticPct,
            affectedPct = affectedPct,
            severityLabel = severityLabel,
            dominantPattern = dominantPattern,
            primaryMatch = primary,
            secondaryMatch = secondary,
            confidencePct = rawConfidence,
            weatherTimingAdvice = weatherAdvice
        )
    }

    private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    val maxDim = maxOf(info.size.width, info.size.height).coerceAtLeast(1)
                    if (maxDim > 512) {
                        val scale = 512f / maxDim
                        decoder.setTargetSize(
                            (info.size.width * scale).roundToInt().coerceAtLeast(1),
                            (info.size.height * scale).roundToInt().coerceAtLeast(1)
                        )
                    }
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } ?: return null

            scaleDownIfNeeded(raw, 360)
        } catch (_: Exception) {
            null
        }
    }

    private fun extractVideoFrames(context: Context, uri: Uri, maxFrames: Int): List<Bitmap> {
        val retriever = MediaMetadataRetriever()
        val frames = mutableListOf<Bitmap>()
        try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L

            if (durationMs <= 0L) {
                retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let {
                    frames.add(scaleDownIfNeeded(it, 360))
                }
            } else {
                val count = maxFrames.coerceAtLeast(1)
                for (i in 0 until count) {
                    val timeUs = ((durationMs * 1000L) * (i + 1)) / (count + 1)
                    val bmp = retriever.getFrameAtTime(
                        timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    if (bmp != null) {
                        frames.add(scaleDownIfNeeded(bmp, 360))
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore unreadable frame
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
        return frames
    }

    private fun scaleDownIfNeeded(bmp: Bitmap, maxEdge: Int): Bitmap {
        val w = bmp.width
        val h = bmp.height
        val maxDim = maxOf(w, h)
        if (maxDim <= maxEdge) return bmp
        val ratio = maxEdge.toFloat() / maxDim.toFloat()
        val nw = (w * ratio).roundToInt().coerceAtLeast(1)
        val nh = (h * ratio).roundToInt().coerceAtLeast(1)
        val scaled = bmp.scale(nw, nh)
        if (scaled !== bmp) {
            bmp.recycle()
        }
        return scaled
    }

    private fun analyzeBitmapPixels(bitmap: Bitmap): FrameMetrics {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val hsv = FloatArray(3)
        var healthy = 0
        var powdery = 0
        var yellowRust = 0
        var necrotic = 0
        var totalSamples = 0

        // Focus slightly more on center 80% region where leaf/plant subject usually sits
        val xStart = (w * 0.1).toInt()
        val xEnd = (w * 0.9).toInt()
        val yStart = (h * 0.1).toInt()
        val yEnd = (h * 0.9).toInt()

        for (y in yStart until yEnd step 2) {
            val rowOffset = y * w
            for (x in xStart until xEnd step 2) {
                val c = pixels[rowOffset + x]
                val r = Color.red(c)
                val g = Color.green(c)
                val b = Color.blue(c)
                Color.RGBToHSV(r, g, b, hsv)

                val hue = hsv[0] // 0..360
                val sat = hsv[1] // 0..1
                val valB = hsv[2] // 0..1

                // Skip pure blown-out sky/glare or pure pitch black shadow
                if (valB > 0.96f && sat < 0.08f) continue
                if (valB < 0.06f) continue

                // Classify plant tissue & symptom signatures
                when {
                    // Healthy chlorophyll green
                    hue in 68f..165f && sat >= 0.18f && valB >= 0.14f -> {
                        healthy++
                        totalSamples++
                    }
                    // Yellowing / chlorosis / orange rust pustules
                    hue in 18f..67f && sat >= 0.25f && valB >= 0.24f -> {
                        yellowRust++
                        totalSamples++
                    }
                    // Necrotic dark brown/black spots on leaf surface
                    (hue in 0f..55f || hue >= 330f) && valB in 0.08f..0.38f && sat >= 0.18f -> {
                        necrotic++
                        totalSamples++
                    }
                    // Powdery white / ash-gray fungal coating
                    sat in 0.05f..0.24f && valB in 0.62f..0.94f && g >= b - 8 -> {
                        powdery++
                        totalSamples++
                    }
                }
            }
        }

        // Fallback if very pale/washed image had few categorized samples
        if (totalSamples < 30) {
            return FrameMetrics(
                healthyGreen = 100,
                powderyWhite = 0,
                yellowRust = 0,
                necroticSpot = 0,
                totalPlantSamples = 100
            )
        }

        return FrameMetrics(
            healthyGreen = healthy,
            powderyWhite = powdery,
            yellowRust = yellowRust,
            necroticSpot = necrotic,
            totalPlantSamples = totalSamples
        )
    }

    private fun rankDiseases(
        pattern: SymptomPattern,
        selectedCropId: String?,
        curTemp: Double,
        curHum: Int
    ): List<DiseaseProfile> {
        val candidates = CropCatalog.diseaseProfiles.filter { it.pattern == pattern }
        if (candidates.isEmpty()) {
            return listOf(CropCatalog.diseaseProfiles.last())
        }

        return candidates.sortedByDescending { profile ->
            var score = 0
            if (selectedCropId != null && profile.cropIds.contains(selectedCropId)) {
                score += 100
            }
            if (curTemp in profile.preferredTempMin..profile.preferredTempMax) {
                score += 20
            }
            if (curHum >= profile.minHumidity) {
                score += 15
            }
            score
        }
    }

    private fun buildWeatherTimingAdvice(
        isHealthy: Boolean,
        sprayEval: SprayEvaluation?,
        weather: WeatherSnapshot?
    ): String {
        if (isHealthy) {
            return "Görselde acil ilaçlama gerektiren yoğun belirti görülmedi. Meteorolojik verilere göre haftalık koruyucu gözleme devam edebilirsiniz."
        }
        val eval = sprayEval
            ?: return "Hava verisi henüz yüklenmedi; rüzgâr <15 km/s, sıcaklık 10–25°C ve yağışsız saatlerde uygulama yapın."

        return when (eval.level) {
            SprayStatusLevel.OK ->
                "✅ Anlık Hava İlaçlamaya Uygun: ${eval.detail}"
            SprayStatusLevel.WARN ->
                "⚠️ Dikkatli İlaçlama: ${eval.title}. ${eval.detail}"
            SprayStatusLevel.DANGER -> {
                val nextGoodDay = weather?.daily?.let { AgronomyEngine.findNextIdealSprayDay(it) }
                val suggestion = if (nextGoodDay != null) {
                    " Tahminlere göre en yakın uygun gün: $nextGoodDay."
                } else {
                    " Rüzgârın 15 km/s altına indiği ve yağışsız saatleri bekleyin."
                }
                "⛔ Şu An İlaçlama Atmayın (${eval.title}): ${eval.detail}$suggestion"
            }
        }
    }
}
