package com.tarimmeteoroloji.ai.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tarimmeteoroloji.ai.data.AgriculturalSearchEngine
import com.tarimmeteoroloji.ai.data.AgronomyEngine
import com.tarimmeteoroloji.ai.data.CropCatalog
import com.tarimmeteoroloji.ai.model.AgriculturalSearchCategory
import com.tarimmeteoroloji.ai.model.AgriculturalSearchResult
import com.tarimmeteoroloji.ai.model.BkuCategory
import com.tarimmeteoroloji.ai.model.CityPreset
import com.tarimmeteoroloji.ai.model.CropInfo
import com.tarimmeteoroloji.ai.model.FuelCityPrices
import com.tarimmeteoroloji.ai.model.GuideTopicId
import com.tarimmeteoroloji.ai.model.NewsArticle
import com.tarimmeteoroloji.ai.model.PlantDiagnosisResult
import com.tarimmeteoroloji.ai.ui.theme.AppColors

@Composable
fun CityPickerDialog(
    onDismiss: () -> Unit,
    onSelectCity: (CityPreset) -> Unit,
    onOpenSearch: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Konum Seçin veya Arama Yapın",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    CropCatalog.presetCities.forEachIndexed { idx, city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCity(city)
                                    onDismiss()
                                }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 ${city.name}",
                                color = AppColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Seç",
                                color = AppColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        if (idx < CropCatalog.presetCities.lastIndex) {
                            HorizontalDivider(color = AppColors.Divider)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.DayCardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenSearch()
                        },
                        border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AppColors.PrimaryLight,
                            contentColor = AppColors.Primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🔍 İlçe/Köy Ara", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onDismiss) {
                        Text("İptal", color = AppColors.TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GeocodeSearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Konum Ara",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = {
                            Text("Örn: Samsat, Tut, Çelikhan, Polatlı...", fontSize = 13.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("İptal", color = AppColors.TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trimmed = query.trim()
                                if (trimmed.isNotEmpty()) {
                                    onSearch(trimmed)
                                    onDismiss()
                                }
                            },
                            enabled = query.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ara", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CropDetailDialog(
    crop: CropInfo,
    avgTemp: Double,
    maxHum: Double,
    totalRain: Double,
    activeSpeechId: String? = null,
    onToggleSpeak: (String, String) -> Unit = { _, _ -> },
    onOpenBkuGuide: (String) -> Unit,
    onOpenDiagnosisForCrop: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val riskText = remember(crop, avgTemp, maxHum, totalRain) {
        AgronomyEngine.evaluateCropRisk(crop, avgTemp, maxHum, totalRain)
    }
    val cropSpeechId = "crop_detail_${crop.id}"
    val isCropSpeaking = activeSpeechId == cropSpeechId
    val cropSpeechText = remember(crop, riskText) {
        buildString {
            append("${crop.name} yetiştiricilik ve bitki koruma rehberi. ")
            append("Güncel meteorolojik risk: $riskText. ")
            append("İklim ve toprak istekleri: ${crop.climateAndSoil}. ")
            append("Don ve sıcaklık eşikleri: ${crop.frostAndTempThresholds}. ")
            append("Su ve sulama programı: ${crop.waterNeedsAndIrrigation}. ")
            append("Gübreleme programı: ${crop.fertilizationProgram}. ")
            append("Başlıca hastalıklar: ${crop.diseases}. ")
            crop.diseaseDetails.forEach { d ->
                append("${d.name}: ${d.symptoms}. Yapılacak ilaçlama: ${d.sprayAction}. ")
            }
            append("Hasat ve depolama: ${crop.harvestAndStorage}.")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(crop.colorHex))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${crop.emoji} ${crop.name} • Detaylı Bitki & Hastalık Rehberi",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCropSpeaking) AppColors.DangerBg else Color.White.copy(alpha = 0.22f))
                                .clickable { onToggleSpeak(cropSpeechId, cropSpeechText) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isCropSpeaking) "⏹️ Durdur" else "🔊 Dinle",
                                color = if (isCropSpeaking) AppColors.DangerText else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = crop.subtitle,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 11.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 510.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botanical hero illustration + quick risk summary
                    CropHeroIllustration(
                        cropId = crop.id,
                        accentColor = Color(crop.colorHex),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.PrimaryLight)
                            .border(1.dp, AppColors.PrimaryBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "📊 GÜNCEL METEOROLOJİK RİSK DURUMU",
                            color = AppColors.Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = riskText,
                            color = AppColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Phenology visual timeline
                    Text(
                        text = "📅 FENOLOJİK TAKVİM & KRİTİK DÖNEMLER",
                        color = AppColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        crop.phenologySteps.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppColors.DayCardBg)
                                    .border(1.dp, AppColors.Divider, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(crop.colorHex))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${step.stage} (${step.timing})",
                                        color = AppColors.TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = step.focus,
                                        color = AppColors.TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // Visual Disease Atlas for this crop
                    Text(
                        text = "🔬 GÖRSELLİ HASTALIK & ZARARLI ATLASI",
                        color = AppColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        crop.diseaseDetails.forEach { d ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.DayCardBg)
                                    .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DiseaseSymptomIllustration(
                                        pattern = d.visualSymptomKind,
                                        modifier = Modifier
                                            .width(68.dp)
                                            .height(68.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = d.name,
                                            color = AppColors.Primary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = d.agent,
                                            color = AppColors.TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "Görünüm: ${d.visualBadge}",
                                            color = Color(crop.colorHex),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(crop.colorHex).copy(alpha = 0.12f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "🔍 Belirtiler: ${d.symptoms}",
                                    color = AppColors.TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                                Text(
                                    text = "🌦️ Yayılma Koşulu: ${d.favorableWeather}",
                                    color = AppColors.TextDetail,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                                Text(
                                    text = "🧪 Yapılacak İlaçlama: ${d.sprayAction}",
                                    color = AppColors.Primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 17.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.PrimaryLight)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    // Detailed Agronomic Reference Sections
                    DetailSection("🌍 İKLİM & TOPRAK İSTEKLERİ:", crop.climateAndSoil)
                    DetailSection("❄️ DON & SICAKLIK EŞİKLERİ:", crop.frostAndTempThresholds)
                    DetailSection("💧 SU İHTİYACI & SULAMA PROGRAMI:", crop.waterNeedsAndIrrigation)
                    DetailSection("🌱 GÜBRELEME & BESLEME PROGRAMI:", crop.fertilizationProgram)
                    DetailSection("📏 GÖZLEM & MÜDAHALE EŞİĞİ:", crop.monitoringThreshold)
                    DetailSection("🧪 ETKEN MADDE GRUPLARI:", crop.activeIngredientGroups)
                    DetailSection("🛡️ ENTEGRE MÜCADELE ÖZETİ:", crop.management)
                    DetailSection("🧺 HASAT & DEPOLAMA KRİTERLERİ:", crop.harvestAndStorage)

                    Text(
                        text = "⚠️ İlaç seçimi, doz ve hasat öncesi bekleme süresi (PHI) için mutlaka ürün etiketindeki ruhsat bilgilerini esas alınız.",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.DayCardBg)
                            .padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.DayCardBg)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenDiagnosisForCrop(crop.id)
                        },
                        border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📸 Teşhis", color = AppColors.Primary, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Kapat", color = AppColors.TextSecondary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenBkuGuide(crop.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🧪 İlaç Rehberi", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgronomyGuideDialog(
    topic: GuideTopicId,
    initialFilterText: String,
    activeSpeechId: String? = null,
    onToggleSpeak: (String, String) -> Unit = { _, _ -> },
    onSelectTopic: (GuideTopicId) -> Unit,
    onStartVoiceSearch: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var searchText by remember(topic, initialFilterText) { mutableStateOf(initialFilterText) }
    var selectedCategory by remember(topic) { mutableStateOf(BkuCategory.ALL) }

    val filteredBku = remember(searchText, selectedCategory) {
        val q = searchText.trim().lowercase()
        CropCatalog.bkuEntries.filter { entry ->
            val catOk = selectedCategory == BkuCategory.ALL || entry.category == selectedCategory
            val qOk = q.isEmpty() ||
                "${entry.activeGroup} ${entry.targets} ${entry.crops} ${entry.category.label}"
                    .lowercase()
                    .contains(q)
            catOk && qOk
        }
    }

    val title = when (topic) {
        GuideTopicId.METEOROLOGY -> "☁️ Tarımsal Meteoroloji & İlaçlama Kuralları"
        GuideTopicId.BKU -> "🧪 Etken Madde & Güvenli İlaçlama Rehberi"
        GuideTopicId.IPM -> "🌿 Entegre Mücadele (IPM) & Bitki Besleme"
    }

    val guideSpeechId = "guide_${topic.name}"
    val isGuideSpeaking = activeSpeechId == guideSpeechId
    val guideSpeechText = remember(topic, filteredBku) {
        when (topic) {
            GuideTopicId.METEOROLOGY ->
                "Tarımsal meteoroloji ve ilaçlama kuralları. " +
                    CropCatalog.meteorologyGuideSections.joinToString(". ") { sec ->
                        "${sec.heading}. ${sec.items.joinToString(". ")}"
                    }
            GuideTopicId.IPM ->
                "Entegre mücadele ve bitki besleme rehberi. " +
                    CropCatalog.ipmGuideSections.joinToString(". ") { sec ->
                        "${sec.heading}. ${sec.items.joinToString(". ")}"
                    }
            GuideTopicId.BKU ->
                "Etken madde ve güvenli ilaçlama rehberi. " +
                    filteredBku.take(8).joinToString(". ") { e ->
                        "${e.activeGroup}. Hedefler: ${e.targets}. Hava koşulu: ${e.weatherRule}. Uyarı: ${e.caution}"
                    }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isGuideSpeaking) AppColors.DangerBg else AppColors.PrimaryBorder)
                                .clickable { onToggleSpeak(guideSpeechId, guideSpeechText) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isGuideSpeaking) "⏹️ Durdur" else "🔊 Dinle",
                                color = if (isGuideSpeaking) AppColors.DangerText else AppColors.Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            GuideTopicId.METEOROLOGY to "☁️ Meteoroloji",
                            GuideTopicId.BKU to "🧪 Etken Maddeler",
                            GuideTopicId.IPM to "🌿 IPM & Besleme"
                        )
                        tabs.forEach { (id, label) ->
                            val selected = topic == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) AppColors.PrimaryBorder else AppColors.PrimaryDark)
                                    .clickable { onSelectTopic(id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) AppColors.Primary else AppColors.PrimarySubtleText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (topic) {
                        GuideTopicId.METEOROLOGY -> {
                            CropCatalog.meteorologyGuideSections.forEach { sec ->
                                GuideCard(sec.heading, sec.items)
                            }
                        }
                        GuideTopicId.IPM -> {
                            CropCatalog.ipmGuideSections.forEach { sec ->
                                GuideCard(sec.heading, sec.items)
                            }
                        }
                        GuideTopicId.BKU -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            "Ürün, hastalık veya etken madde ara...",
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onStartVoiceSearch()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("🎙️ Ses", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BkuCategory.entries.forEach { cat ->
                                    val active = selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) AppColors.Primary else AppColors.DayCardBg)
                                            .border(
                                                1.dp,
                                                if (active) AppColors.Primary else AppColors.Divider,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = cat.label,
                                            color = if (active) Color.White else AppColors.TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            if (filteredBku.isEmpty()) {
                                Text(
                                    text = "Aramanızla eşleşen kayıt bulunamadı.",
                                    color = AppColors.TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AppColors.DayCardBg)
                                        .padding(14.dp)
                                )
                            } else {
                                filteredBku.forEach { entry ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AppColors.DayCardBg)
                                            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = entry.activeGroup,
                                                color = AppColors.Primary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = entry.category.label,
                                                color = AppColors.Primary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(AppColors.PrimaryLight)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "Hedefler: ${entry.targets}",
                                            color = AppColors.TextPrimary,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Ürünler: ${entry.crops}",
                                            color = AppColors.TextPrimary,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Hava Koşulu: ${entry.weatherRule}",
                                            color = AppColors.TextDetail,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "⚠️ ${entry.caution}",
                                            color = AppColors.WarnText,
                                            fontSize = 11.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.WarnBg)
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.DayCardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Çevrimdışı Gömülü Tarımsal Bilgi Bankası",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kapat", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantDiagnosisDialog(
    selectedCropId: String?,
    onSelectCropId: (String?) -> Unit,
    isAnalyzing: Boolean,
    diagnosisError: String? = null,
    result: PlantDiagnosisResult?,
    activeSpeechId: String? = null,
    onToggleSpeak: (String, String) -> Unit = { _, _ -> },
    onTakePhoto: () -> Unit,
    onRecordVideo: () -> Unit,
    onPickFromGallery: () -> Unit,
    onOpenBkuGuide: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "📸 Foto / Video ile Bitki Hastalık & İlaçlama Teşhisi",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 510.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Önce bitki türünü seçin (isteğe bağlı, doğruluğu artırır):",
                        color = AppColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val allSelected = selectedCropId == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (allSelected) AppColors.Primary else AppColors.DayCardBg)
                                .border(
                                    1.dp,
                                    if (allSelected) AppColors.Primary else AppColors.Divider,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectCropId(null) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🌱 Genel",
                                color = if (allSelected) Color.White else AppColors.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        CropCatalog.crops.forEach { crop ->
                            val active = selectedCropId == crop.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) Color(crop.colorHex) else AppColors.DayCardBg)
                                    .border(
                                        1.dp,
                                        if (active) Color(crop.colorHex) else AppColors.Divider,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectCropId(crop.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${crop.emoji} ${crop.id}",
                                    color = if (active) Color.White else AppColors.TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text(
                        text = "2. Hastalıklı yaprağın/meyvenin fotoğrafını çekin veya kısa videosunu kaydedin:",
                        color = AppColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTakePhoto,
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📷 Foto Çek", color = Color.White, fontSize = 12.sp)
                        }
                        Button(
                            onClick = onRecordVideo,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎥 Video Çek", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onPickFromGallery,
                        border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AppColors.PrimaryLight,
                            contentColor = AppColors.Primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "🖼️ Galeriden Foto veya Video Seç",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isAnalyzing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.PrimaryLight)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.Primary,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                            )
                            Text(
                                text = "Görüntü/video kareleri yaprak lekesi, küf ve pas belirtileri açısından analiz ediliyor...",
                                color = AppColors.Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (diagnosisError != null && !isAnalyzing) {
                        Text(
                            text = "⚠️ $diagnosisError",
                            color = AppColors.DangerText,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.DangerBg)
                                .border(1.dp, AppColors.DangerBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        )
                    }

                    if (result != null && !isAnalyzing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.DayCardBg)
                                .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (result.previewBitmap != null) {
                                Image(
                                    bitmap = result.previewBitmap.asImageBitmap(),
                                    contentDescription = "Analiz edilen bitki görüntüsü",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }

                            Text(
                                text = if (result.isVideo) {
                                    "🎥 Video Analizi (${result.framesAnalyzed} kare tarandı) • ${result.selectedCropName}"
                                } else {
                                    "📷 Fotoğraf Analizi • ${result.selectedCropName}"
                                },
                                color = AppColors.TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🦠 OLASI HASTALIK: ${result.primaryMatch.diseaseName}",
                                    color = AppColors.Primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                val diagSpeechId = "diagnosis_result"
                                val isDiagSpeaking = activeSpeechId == diagSpeechId
                                val diagSpeechText = buildString {
                                    append("${result.selectedCropName} teşhis sonucu. ")
                                    append("Olası hastalık: ${result.primaryMatch.diseaseName}. ")
                                    append("Eşleşme yüzde ${result.confidencePct}, şiddet ${result.severityLabel}. ")
                                    append("Belirtiler: ${result.primaryMatch.visualSigns}. ")
                                    append("Yapılacak ilaçlama önerisi: ${result.primaryMatch.sprayRecommendation}. ")
                                    append("Önerilen etken maddeler: ${result.primaryMatch.activeIngredients}. ")
                                    append("Hava ve ilaçlama zamanlaması: ${result.weatherTimingAdvice}. ")
                                    append("Kültürel tedbirler: ${result.primaryMatch.culturalMeasures}.")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isDiagSpeaking) "⏹️ Durdur" else "🔊 Dinle",
                                    color = if (isDiagSpeaking) AppColors.DangerText else AppColors.Primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDiagSpeaking) AppColors.DangerBg else AppColors.PrimaryLight)
                                        .clickable { onToggleSpeak(diagSpeechId, diagSpeechText) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "Etmen: ${result.primaryMatch.agentType} • Eşleşme: %${result.confidencePct} • Şiddet: ${result.severityLabel}",
                                color = AppColors.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "🔬 Görüntü Doku Dağılımı: Sağlıklı Yeşil %${result.healthyGreenPct} | Beyaz Küf %${result.powderyWhitePct} | Sarı/Pas %${result.yellowRustPct} | Koyu Leke %${result.necroticSpotPct}",
                                color = AppColors.TextDetail,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            )

                            DetailSection("🔍 Tespit Edilen Görsel Belirti:", "${result.dominantPattern.label} — ${result.primaryMatch.visualSigns}")
                            DetailSection("🧪 YAPILACAK İLAÇLAMA ÖNERİSİ:", result.primaryMatch.sprayRecommendation)
                            DetailSection("💊 Önerilen Etken Madde Grupları:", result.primaryMatch.activeIngredients)
                            DetailSection("🌦️ Anlık Hava & İlaçlama Zamanlaması:", result.weatherTimingAdvice)
                            DetailSection("⏱️ Uygulama & Fitotoksisite Notu:", result.primaryMatch.timingAndWeatherNote)
                            DetailSection("🌿 Kültürel & Entegre Tedbirler:", result.primaryMatch.culturalMeasures)

                            if (result.secondaryMatch != null) {
                                Text(
                                    text = "ℹ️ Alternatif Olasılık: ${result.secondaryMatch.diseaseName} (${result.secondaryMatch.activeIngredients})",
                                    color = AppColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "⚠️ Kesin teşhis ve ruhsatlı doz için yaprak altını büyüteçle kontrol edin; uygulama öncesi ürün etiketini ve hasat bekleme süresini (PHI) esas alın.",
                                color = AppColors.WarnText,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.WarnBg)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.DayCardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (result != null) {
                        OutlinedButton(
                            onClick = {
                                val filter = selectedCropId ?: ""
                                onDismiss()
                                onOpenBkuGuide(filter)
                            },
                            border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🧪 İlaç Rehberi", color = AppColors.Primary, fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kapat", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, body: String) {
    Column {
        Text(
            text = title,
            color = AppColors.Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun GuideCard(heading: String, items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DayCardBg)
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = heading,
            color = AppColors.Primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        items.forEach { item ->
            Text(
                text = "• $item",
                color = AppColors.TextPrimary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceSearchDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    isListening: Boolean,
    partialTranscript: String,
    voiceStatusText: String,
    rmsLevel: Float,
    autoSpeakEnabled: Boolean,
    onToggleAutoSpeak: (Boolean) -> Unit,
    results: List<AgriculturalSearchResult>,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onOpenResult: (AgriculturalSearchResult) -> Unit,
    onDismiss: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val dynamicScale = if (isListening) {
        (pulseScale + (rmsLevel.coerceIn(0f, 10f) / 80f)).coerceAtMost(1.28f)
    } else {
        1.0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Column {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎙️ Tarlada Sesli Tarım Arama",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (autoSpeakEnabled) AppColors.PrimaryBorder else AppColors.PrimaryDark
                                )
                                .clickable {
                                    val next = !autoSpeakEnabled
                                    if (!next) onStopSpeaking()
                                    onToggleAutoSpeak(next)
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (autoSpeakEnabled) "🔊 Sesli Yanıt: Açık" else "🔇 Sesli Yanıt: Kapalı",
                                color = if (autoSpeakEnabled) AppColors.Primary else AppColors.PrimarySubtleText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Elleriniz doluyken ürün, hastalık, don eşiği veya ilaç adı söyleyin",
                        color = AppColors.PrimarySubtleText,
                        fontSize = 11.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hands-free mic control panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isListening) Color(0xFFE8F5E9) else AppColors.DayCardBg)
                            .border(
                                1.dp,
                                if (isListening) AppColors.Primary else AppColors.Divider,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(dynamicScale)
                                    .clip(CircleShape)
                                    .background(if (isListening) AppColors.DangerText else AppColors.Primary)
                                    .clickable {
                                        if (isListening) onStopListening() else onStartListening()
                                    }
                            ) {
                                Text(
                                    text = if (isListening) "⏹️" else "🎙️",
                                    fontSize = 22.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isListening) "Dinliyorum... Türkçe konuşun" else "Sesli Arama Hazır",
                                    color = if (isListening) AppColors.Primary else AppColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when {
                                        isListening && partialTranscript.isNotEmpty() -> "“$partialTranscript”"
                                        voiceStatusText.isNotEmpty() -> voiceStatusText
                                        else -> "Mikrofona dokunun ve sorunuzu söyleyin."
                                    },
                                    color = AppColors.TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { if (isListening) onStopListening() else onStartListening() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isListening) AppColors.DangerText else AppColors.Primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    text = if (isListening) "⏹️ Dinlemeyi Bitir" else "🎙️ Konuşmaya Başla",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (results.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { onSpeakText(results.first().speechText) },
                                    border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = AppColors.PrimaryLight,
                                        contentColor = AppColors.Primary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Text("🔊 İlk Sonucu Oku", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Search text input (synced with recognized speech & manual edits)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            placeholder = {
                                Text("Örn: Bağ küllemesi, Badem don, Kükürt...", fontSize = 12.sp)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (query.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    onStopSpeaking()
                                    onQueryChange("")
                                },
                                border = BorderStroke(1.dp, AppColors.Divider),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Temizle", color = AppColors.TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    // Quick spoken prompt chips
                    Text(
                        text = "💡 Örnek Sesli Aramalar (dokunarak da arayabilirsiniz):",
                        color = AppColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AgriculturalSearchEngine.quickVoiceExamples.forEach { example ->
                            val active = query.equals(example, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) AppColors.Primary else AppColors.PrimaryLight)
                                    .border(
                                        1.dp,
                                        if (active) AppColors.Primary else AppColors.PrimaryBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onQueryChange(example)
                                    }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "“$example”",
                                    color = if (active) Color.White else AppColors.Primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Search Results
                    Text(
                        text = if (query.isBlank()) {
                            "📚 Hızlı Erişim (Konuştuğunuzda sonuçlar burada listelenir):"
                        } else {
                            "🔍 Bulunan Kayıtlar (${results.size}):"
                        },
                        color = AppColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (results.isEmpty()) {
                        Text(
                            text = "“$query” için tarımsal bilgi bankasında doğrudan eşleşme bulunamadı. Daha kısa bir ifade söylemeyi deneyin (ör. “Bağ mildiyö”, “Zeytin”, “Bakır”, “Don eşiği”).",
                            color = AppColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.DayCardBg)
                                .border(1.dp, AppColors.Divider, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        )
                    } else {
                        results.forEach { item ->
                            val badgeBg = when (item.category) {
                                AgriculturalSearchCategory.DISEASE -> AppColors.WarnBg
                                AgriculturalSearchCategory.BKU -> AppColors.PrimaryLight
                                AgriculturalSearchCategory.METEOROLOGY -> Color(0xFFE3F2FD)
                                AgriculturalSearchCategory.IPM -> Color(0xFFE8F5E9)
                                AgriculturalSearchCategory.CROP -> AppColors.PrimaryLight
                            }
                            val badgeColor = when (item.category) {
                                AgriculturalSearchCategory.DISEASE -> AppColors.WarnText
                                AgriculturalSearchCategory.METEOROLOGY -> Color(0xFF1565C0)
                                else -> AppColors.Primary
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.DayCardBg)
                                    .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.category.emoji} ${item.category.label}",
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                    Text(
                                        text = "🔊 Dinle",
                                        color = AppColors.Primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AppColors.PrimaryLight)
                                            .clickable { onSpeakText(item.speechText) }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = item.title,
                                    color = AppColors.Primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.subtitle,
                                    color = AppColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = item.detailSummary,
                                    color = AppColors.TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                                Text(
                                    text = item.actionHint,
                                    color = AppColors.Primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 17.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.PrimaryLight)
                                        .padding(8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { onOpenResult(item) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (item.cropId != null) "Ürün Rehberini Aç →" else "Tam Bölümü Aç →",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.DayCardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Çevrimdışı Tarımsal Bilgi Bankası",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Button(
                        onClick = {
                            onStopListening()
                            onStopSpeaking()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kapat", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsArticleDialog(
    article: NewsArticle,
    activeSpeechId: String?,
    onToggleSpeak: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isSpeaking = activeSpeechId == article.id
    val fullSpeechText = "${article.title}. ${article.summary}. Kaynak: ${article.source}, tarih: ${article.publishedAt}."

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📰 ${article.category}",
                            color = AppColors.PrimaryBorder,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSpeaking) AppColors.DangerBg else AppColors.PrimaryBorder)
                                .clickable { onToggleSpeak(article.id, fullSpeechText) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isSpeaking) "⏹️ Durdur" else "🔊 Sesli Oku",
                                color = if (isSpeaking) AppColors.DangerText else AppColors.Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${article.source} • ${article.publishedAt}",
                        color = AppColors.PrimarySubtleText,
                        fontSize = 11.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = article.summary,
                        color = AppColors.TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "ℹ️ Bu haber Tarım Meteoroloji AI içine güvenli şekilde çekilmiştir; harici web tarayıcısına yönlendirme yapmaz.",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.DayCardBg)
                            .padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.DayCardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onToggleSpeak(article.id, fullSpeechText) },
                        border = BorderStroke(
                            1.dp,
                            if (isSpeaking) AppColors.DangerBorder else AppColors.PrimaryBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isSpeaking) "⏹️ Okumayı Durdur" else "🔊 Haberi Dinle",
                            color = if (isSpeaking) AppColors.DangerText else AppColors.Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kapat", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FuelCityPickerDialog(
    cities: List<FuelCityPrices>,
    selectedCityName: String?,
    onSelectCity: (FuelCityPrices) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(cities, search) {
        val q = AgriculturalSearchEngine.normalizeTurkish(search)
        if (q.isEmpty()) cities
        else cities.filter {
            AgriculturalSearchEngine.normalizeTurkish(it.cityName).contains(q)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "⛽ Akaryakıt İli / Bölgesi Seçin",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        singleLine = true,
                        placeholder = { Text("İl adı yazın (Adıyaman, Konya, İzmir...)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filtered.forEachIndexed { idx, city ->
                        val active = city.cityName.equals(selectedCityName, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (active) AppColors.PrimaryLight else Color.Transparent)
                                .clickable {
                                    onSelectCity(city)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = city.cityName,
                                    color = if (active) AppColors.Primary else AppColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "Motorin ${city.diesel} • Benzin ${city.gasoline95}",
                                    color = AppColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = if (active) "Seçili" else "Seç",
                                color = AppColors.Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (idx < filtered.lastIndex) {
                            HorizontalDivider(color = AppColors.Divider)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.DayCardBg)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Kapat", color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}
