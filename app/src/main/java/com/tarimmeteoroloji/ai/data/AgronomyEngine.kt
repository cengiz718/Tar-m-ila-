package com.tarimmeteoroloji.ai.data

import com.tarimmeteoroloji.ai.model.AgronomicReport
import com.tarimmeteoroloji.ai.model.CropInfo
import com.tarimmeteoroloji.ai.model.CurrentWeather
import com.tarimmeteoroloji.ai.model.SprayEvaluation
import com.tarimmeteoroloji.ai.model.SprayStatusLevel
import com.tarimmeteoroloji.ai.model.WeatherDay
import com.tarimmeteoroloji.ai.model.WeatherSnapshot
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

object AgronomyEngine {

    fun weatherIcon(code: Int): String {
        if (code == 0) return "☀️"
        if (code <= 3) return "⛅"
        if (code < 50) return "🌫️"
        if (code < 70) return "🌧️"
        if (code < 80) return "🌨️"
        if (code < 95) return "🌦️"
        return "⛈️"
    }

    fun weatherLabel(code: Int): String {
        if (code == 0) return "Açık / Güneşli"
        if (code <= 2) return "Az Bulutlu"
        if (code == 3) return "Çok Bulutlu"
        if (code < 50) return "Sisli / Puslu"
        if (code < 60) return "Çisenti"
        if (code < 70) return "Yağmurlu"
        if (code < 80) return "Kar / Karla Karışık"
        if (code < 95) return "Sağanak Yağışlı"
        return "Gök Gürültülü Sağanak"
    }

    fun formatDayLabel(date: String, index: Int): String {
        if (index == 0) return "Bugün"
        return if (date.length >= 10) {
            "${date.substring(8, 10)}.${date.substring(5, 7)}"
        } else {
            date.ifBlank { "Gün ${index + 1}" }
        }
    }

    fun parseWeatherSnapshot(rawJson: String): WeatherSnapshot {
        val root = JSONObject(rawJson)
        val curObj = root.optJSONObject("current")
        val current = if (curObj != null) {
            CurrentWeather(
                temperature = curObj.optDouble("temperature_2m", 0.0),
                humidity = curObj.optInt("relative_humidity_2m", 0),
                windSpeed = curObj.optDouble("wind_speed_10m", 0.0),
                precipitation = curObj.optDouble("precipitation", 0.0),
                weatherCode = curObj.optInt("weather_code", 0)
            )
        } else {
            null
        }

        val dailyObj = root.getJSONObject("daily")
        val dates = dailyObj.getJSONArray("time")
        val codes = dailyObj.getJSONArray("weather_code")
        val tMax = dailyObj.getJSONArray("temperature_2m_max")
        val tMin = dailyObj.getJSONArray("temperature_2m_min")
        val rain = dailyObj.getJSONArray("precipitation_sum")
        val rainProb = dailyObj.optJSONArray("precipitation_probability_max")
        val wind = dailyObj.getJSONArray("wind_speed_10m_max")
        val hum = dailyObj.getJSONArray("relative_humidity_2m_max")
        val et0 = dailyObj.optJSONArray("et0_fao_evapotranspiration")

        val count = minOf(
            dates.length(),
            codes.length(),
            tMax.length(),
            tMin.length(),
            rain.length(),
            wind.length(),
            hum.length()
        )
        val days = ArrayList<WeatherDay>(count)
        for (i in 0 until count) {
            days.add(
                WeatherDay(
                    date = dates.optString(i, ""),
                    weatherCode = codes.optInt(i, 0),
                    tempMax = tMax.optDouble(i, 0.0),
                    tempMin = tMin.optDouble(i, 0.0),
                    precipitationSum = rain.optDouble(i, 0.0),
                    precipitationProbMax = rainProb?.optInt(i, 0) ?: 0,
                    windSpeedMax = wind.optDouble(i, 0.0),
                    humidityMax = hum.optInt(i, 0),
                    et0 = et0?.optDouble(i, 0.0) ?: 0.0
                )
            )
        }
        return WeatherSnapshot(current = current, daily = days)
    }

    fun evaluateSprayCondition(
        temp: Double,
        hum: Int,
        wind: Double,
        curRain: Double,
        todayRain: Double,
        rainProb: Int
    ): SprayEvaluation {
        return when {
            curRain > 0.2 || todayRain >= 3.0 || rainProb >= 70 -> SprayEvaluation(
                level = SprayStatusLevel.DANGER,
                title = "⛔ İLAÇLAMA ÖNERİLMEZ (Yağış Riski)",
                detail = "Yağış ilacın yapraktan yıkanmasına ve etkisinin kaybolmasına neden olur. Yağış sonrası yaprak yüzeyi kuruduğunda tekrar değerlendirin."
            )
            wind > 15.0 -> SprayEvaluation(
                level = SprayStatusLevel.DANGER,
                title = "⛔ İLAÇLAMA ÖNERİLMEZ (Yüksek Rüzgâr: ${wind.roundToInt()} km/s)",
                detail = "15 km/s üzeri rüzgârda ilaç zerrecikleri sürüklenir (drift), hedef bitkiye ulaşmaz ve çevreye zarar verir."
            )
            temp > 30.0 || temp < 5.0 -> SprayEvaluation(
                level = SprayStatusLevel.WARN,
                title = "⚠️ DİKKATLİ OLUN (Sıcaklık Sınırda: ${temp.roundToInt()}°C)",
                detail = if (temp > 30.0) {
                    "30°C üzerinde hızlı buharlaşma ve yaprak yanıklığı (fitotoksisite) riski vardır. Sabah erken veya akşam serinliğini bekleyin."
                } else {
                    "5°C altında bitki metabolizması yavaştır ve don riski ilacın etkinliğini düşürebilir."
                }
            )
            hum < 35 -> SprayEvaluation(
                level = SprayStatusLevel.WARN,
                title = "⚠️ DÜŞÜK NEM (%$hum) - Buharlaşma Riski",
                detail = "Düşük nemde damlacıklar yaprağa ulaşmadan kuruyabilir. Sabah erken saatleri tercih edin."
            )
            else -> SprayEvaluation(
                level = SprayStatusLevel.OK,
                title = "✅ İLAÇLAMA İÇİN KOŞULLAR UYGUN",
                detail = "Sıcaklık (${temp.roundToInt()}°C), rüzgâr (${wind.roundToInt()} km/s) ve nem (%$hum) püskürtme için ideal aralıkta."
            )
        }
    }

    fun evaluateDailySprayStatus(day: WeatherDay): SprayEvaluation {
        val avgTemp = (day.tempMax + day.tempMin) / 2.0
        return when {
            day.precipitationSum >= 2.0 || day.precipitationProbMax >= 65 -> SprayEvaluation(
                level = SprayStatusLevel.DANGER,
                title = "⛔ Yağışlı",
                detail = "Günlük yağış beklentisi (${String.format(Locale.US, "%.1f", day.precipitationSum)} mm, %${day.precipitationProbMax} olasılık) ilacı yıkayabilir."
            )
            day.windSpeedMax > 15.0 -> SprayEvaluation(
                level = SprayStatusLevel.DANGER,
                title = "⛔ Rüzgârlı",
                detail = "Maksimum rüzgâr ${day.windSpeedMax.roundToInt()} km/s seviyesinde; drift (sürüklenme) riski yüksek."
            )
            day.tempMax > 30.0 || day.tempMin < 3.0 || day.precipitationProbMax >= 40 -> SprayEvaluation(
                level = SprayStatusLevel.WARN,
                title = "⚠️ Sınırda",
                detail = "Gündüz sıcaklığı veya yağış/rüzgâr geçişleri nedeniyle yalnızca sabah serinliğinde ilaçlama önerilir."
            )
            avgTemp in 8.0..28.0 -> SprayEvaluation(
                level = SprayStatusLevel.OK,
                title = "✅ Uygun",
                detail = "Gün içi rüzgâr (${day.windSpeedMax.roundToInt()} km/s) ve sıcaklık (${day.tempMin.roundToInt()}°–${day.tempMax.roundToInt()}°C) püskürtmeye elverişli."
            )
            else -> SprayEvaluation(
                level = SprayStatusLevel.WARN,
                title = "⚠️ Serin",
                detail = "Sıcaklık düşük olduğu için sistemik ilaçların emilimi yavaşlayabilir."
            )
        }
    }

    fun findNextIdealSprayDay(days: List<WeatherDay>): String? {
        for (i in days.indices) {
            val d = days[i]
            val avgT = (d.tempMax + d.tempMin) / 2.0
            if (d.precipitationSum < 1.5 && d.precipitationProbMax < 45 && d.windSpeedMax <= 15.0 && avgT in 8.0..28.0) {
                return formatDayLabel(d.date, i)
            }
        }
        return null
    }

    fun buildAgronomicReport(days: List<WeatherDay>): AgronomicReport {
        if (days.isEmpty()) {
            return AgronomicReport(
                sections = listOf("Meteorolojik veriler bekleniyor..."),
                avgTemp = 20.0,
                maxHum = 65.0,
                totalRain = 0.0
            )
        }

        var totalRain = 0.0
        var minNightTemp = 99.0
        var sumAvgTemp = 0.0
        var maxHum = 0.0
        var totalEt0 = 0.0
        var humidDays = 0
        var frostDays = 0
        var rainyDays = 0

        for (day in days) {
            totalRain += day.precipitationSum
            if (day.precipitationSum >= 2.0) rainyDays++
            if (day.tempMin < minNightTemp) minNightTemp = day.tempMin
            if (day.tempMin <= 2.0) frostDays++
            if (day.humidityMax >= 78) humidDays++
            if (day.humidityMax > maxHum) maxHum = day.humidityMax.toDouble()
            sumAvgTemp += (day.tempMax + day.tempMin) / 2.0
            totalEt0 += day.et0
        }

        val avgTemp = sumAvgTemp / days.size
        val sections = mutableListOf<String>()

        if (frostDays > 0) {
            sections.add(
                String.format(
                    Locale.US,
                    "❄️ DON UYARISI: Önümüzdeki 7 günde %d gece sıcaklık %.1f°C seviyesine düşüyor. Çiçeklenme dönemindeki meyve bahçeleri ve erken ekim sebzeler için don tedbiri alın.",
                    frostDays,
                    minNightTemp
                )
            )
        } else {
            sections.add(
                String.format(
                    Locale.US,
                    "🌡️ SICAKLIK DENGESİ: 7 günlük ortalama sıcaklık %.1f°C civarında seyredecek; zirai don riski görünmüyor.",
                    avgTemp
                )
            )
        }

        if (humidDays >= 3 && avgTemp in 14.0..27.0) {
            sections.add(
                "🍄 YÜKSEK MANTARİ HASTALIK RİSKİ: Haftanın $humidDays gününde nem %78 üzerine çıkıyor ve sıcaklık fungal spor gelişimi için uygun. Bağda mildiyö/külleme, tahıllarda pas ve septorya gözlemini sıklaştırın."
            )
        } else if (humidDays >= 2) {
            sections.add(
                "🌿 ORTA DÜZEY HASTALIK RİSKİ: Nem artışının görüldüğü $humidDays günde bahçe ve tarla içi havalanmaya dikkat edin."
            )
        } else {
            sections.add(
                "✅ DÜŞÜK FUNGAL RİSK: Bağıl nem seviyeleri mantari hastalık baskısı oluşturacak sınırın altında."
            )
        }

        val waterDeficit = totalEt0 - totalRain
        if (totalEt0 > 0) {
            val base = String.format(
                Locale.US,
                "💧 7 GÜNLÜK SU BÜTÇESİ: Toplam yağış %.1f mm, bitki su tüketimi (ET₀) %.1f mm. ",
                totalRain,
                totalEt0
            )
            if (waterDeficit > 12) {
                sections.add(
                    base + String.format(
                        Locale.US,
                        "Net su açığı %.1f mm; sulama planlaması önerilir.",
                        waterDeficit
                    )
                )
            } else {
                sections.add(base + "Toprak nem dengesi yağışlarla destekleniyor.")
            }
        } else {
            sections.add(
                String.format(
                    Locale.US,
                    "💧 YAĞIŞ ÖZETİ: 7 günde toplam %.1f mm yağış (%d yağışlı gün) bekleniyor.",
                    totalRain,
                    rainyDays
                )
            )
        }

        return AgronomicReport(
            sections = sections,
            avgTemp = avgTemp,
            maxHum = maxHum,
            totalRain = totalRain
        )
    }

    fun evaluateCropRisk(
        crop: CropInfo,
        avgTemp: Double,
        maxHum: Double,
        totalRain: Double
    ): String {
        val tempMatch = avgTemp in crop.tempRiskMin..crop.tempRiskMax
        val humMatch = maxHum >= crop.humidityThreshold

        return when {
            tempMatch && humMatch ->
                "🔴 YÜKSEK RİSK (Hava koşulları bu üründe hastalık gelişimi için elverişli)"
            tempMatch || humMatch || totalRain >= 10.0 ->
                "🟡 ORTA RİSK (Düzenli tarla/bahçe gözlemi önerilir)"
            else ->
                "🟢 DÜŞÜK RİSK (Mevcut meteorolojik baskı düşük)"
        }
    }
}
