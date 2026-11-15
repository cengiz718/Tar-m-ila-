package com.tarimmeteoroloji.ai.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tarimmeteoroloji.ai.R
import com.tarimmeteoroloji.ai.model.SymptomPattern

@DrawableRes
fun cropDrawableRes(cropId: String): Int {
    return when (cropId) {
        "Bağ" -> R.drawable.crop_grapevine
        "Zeytin" -> R.drawable.crop_olive
        "Badem" -> R.drawable.crop_almond
        "Buğday" -> R.drawable.crop_wheat
        "Arpa" -> R.drawable.crop_barley
        "Nohut" -> R.drawable.crop_chickpea
        "Nar" -> R.drawable.crop_pomegranate
        "Elma" -> R.drawable.crop_apple
        "Sebze" -> R.drawable.crop_vegetables
        else -> R.drawable.crop_wheat
    }
}

@Composable
fun CropHeroIllustration(
    cropId: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.10f))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Image(
            painter = painterResource(id = cropDrawableRes(cropId)),
            contentDescription = "$cropId tarım görseli",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun DiseaseSymptomIllustration(
    pattern: SymptomPattern,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF6FAF7))
            .border(1.dp, Color(0xFFDCE6DE), RoundedCornerShape(10.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val leaf = Path().apply {
                moveTo(w * 0.16f, h * 0.82f)
                quadraticTo(w * 0.14f, h * 0.25f, w * 0.78f, h * 0.18f)
                quadraticTo(w * 0.84f, h * 0.78f, w * 0.16f, h * 0.82f)
                close()
            }
            val baseLeafColor = when (pattern) {
                SymptomPattern.YELLOW_RUST -> Color(0xFF8BC34A)
                else -> Color(0xFF4CAF50)
            }
            drawPath(leaf, color = baseLeafColor)
            drawPath(leaf, color = Color(0xFF2E7D32), style = Stroke(width = 2.5f))

            drawLine(
                color = Color(0xFF1B5E20),
                start = Offset(w * 0.10f, h * 0.90f),
                end = Offset(w * 0.74f, h * 0.22f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF1B5E20).copy(alpha = 0.55f),
                start = Offset(w * 0.36f, h * 0.62f),
                end = Offset(w * 0.30f, h * 0.38f),
                strokeWidth = 1.8f
            )
            drawLine(
                color = Color(0xFF1B5E20).copy(alpha = 0.55f),
                start = Offset(w * 0.50f, h * 0.48f),
                end = Offset(w * 0.68f, h * 0.56f),
                strokeWidth = 1.8f
            )

            when (pattern) {
                SymptomPattern.POWDERY_WHITE -> {
                    val spots = listOf(
                        Offset(w * 0.40f, h * 0.46f) to w * 0.11f,
                        Offset(w * 0.56f, h * 0.38f) to w * 0.10f,
                        Offset(w * 0.52f, h * 0.58f) to w * 0.09f,
                        Offset(w * 0.66f, h * 0.30f) to w * 0.07f
                    )
                    spots.forEach { (center, rad) ->
                        drawCircle(Color.White.copy(alpha = 0.85f), radius = rad, center = center)
                        drawCircle(Color(0xFFE0E0E0), radius = rad * 0.6f, center = center)
                    }
                }
                SymptomPattern.YELLOW_RUST -> {
                    drawOval(
                        color = Color(0xFFFFEB3B).copy(alpha = 0.65f),
                        topLeft = Offset(w * 0.28f, h * 0.35f),
                        size = Size(w * 0.36f, h * 0.28f)
                    )
                    val stripes = listOf(0.36f, 0.44f, 0.52f)
                    stripes.forEach { sy ->
                        drawLine(
                            color = Color(0xFFE65100),
                            start = Offset(w * 0.34f, h * sy),
                            end = Offset(w * 0.62f, h * (sy - 0.12f)),
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                SymptomPattern.NECROTIC_SPOT -> {
                    val lesions = listOf(
                        Offset(w * 0.44f, h * 0.46f) to w * 0.11f,
                        Offset(w * 0.58f, h * 0.56f) to w * 0.08f,
                        Offset(w * 0.62f, h * 0.34f) to w * 0.07f
                    )
                    lesions.forEach { (c, r) ->
                        drawCircle(Color(0xFFFBC02D).copy(alpha = 0.75f), radius = r * 1.25f, center = c)
                        drawCircle(Color(0xFF4E342E), radius = r, center = c)
                        drawCircle(Color(0xFF212121), radius = r * 0.48f, center = c)
                    }
                }
                SymptomPattern.HEALTHY -> {
                    drawCircle(Color(0xFFE8F5E9), radius = w * 0.04f, center = Offset(w * 0.48f, h * 0.42f))
                }
            }
        }
    }
}
