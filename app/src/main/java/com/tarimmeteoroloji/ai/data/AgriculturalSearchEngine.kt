package com.tarimmeteoroloji.ai.data

import com.tarimmeteoroloji.ai.model.AgriculturalSearchCategory
import com.tarimmeteoroloji.ai.model.AgriculturalSearchResult
import com.tarimmeteoroloji.ai.model.CropInfo
import com.tarimmeteoroloji.ai.model.GuideTopicId
import java.util.Locale

object AgriculturalSearchEngine {

    private val TR_LOCALE = Locale("tr", "TR")

    private val STOP_WORDS = setOf(
        "nedir", "nasil", "ne", "zaman", "yapilir", "atilir", "uygulanir", "verilir",
        "hakkinda", "bana", "soyle", "goster", "bul", "ara", "bilgi", "ver", "icin",
        "ile", "ve", "veya", "kac", "derece", "derecede", "olur", "var", "mi", "mu",
        "bu", "su", "hangi", "ilac", "ilaci", "ilaclari", "hastalik", "hastaligi",
        "mucadele", "mucadelesi", "sebebi", "nedeni"
    )

    private val SYNONYM_MAP: Map<String, List<String>> = mapOf(
        "uzum" to listOf("bag", "asma"),
        "asma" to listOf("bag", "uzum"),
        "domates" to listOf("sebze", "tuta", "alternaria"),
        "biber" to listOf("sebze", "kulleme", "beyazsinek"),
        "bordo" to listOf("bakir", "bakirli"),
        "bulamaci" to listOf("bakir", "bakirli"),
        "don" to listOf("soguk", "esik", "radyasyon"),
        "soguk" to listOf("don", "esik"),
        "sulama" to listOf("su", "et0", "damlama"),
        "gubre" to listOf("gubreleme", "besleme", "azot", "potasyum"),
        "gubreleme" to listOf("gubre", "besleme"),
        "ilaclama" to listOf("puskurtme", "ruzgar", "bku"),
        "ruzgar" to listOf("drift", "suruklenme", "ilaclama"),
        "ot" to listOf("yabani", "herbisit"),
        "kurt" to listOf("larva", "guve", "bocek", "bt"),
        "bocek" to listOf("zararli", "insektisit")
    )

    val quickVoiceExamples: List<String> = listOf(
        "Bağ küllemesi ilacı",
        "Zeytin halkalı leke",
        "Badem don eşiği",
        "Kükürt kaç derece",
        "Buğday sarı pas",
        "İlaçlama rüzgâr hızı"
    )

    fun normalizeTurkish(raw: String): String {
        val lowered = raw.lowercase(TR_LOCALE)
        val sb = StringBuilder(lowered.length)
        for (ch in lowered) {
            val mapped = when (ch) {
                'ç' -> 'c'
                'ğ' -> 'g'
                'ı', 'î', 'ï' -> 'i'
                'ö' -> 'o'
                'ş' -> 's'
                'ü' -> 'u'
                'â' -> 'a'
                'û' -> 'u'
                '₀' -> '0'
                else -> ch
            }
            if (mapped.isLetterOrDigit() || mapped == ' ') {
                sb.append(mapped)
            } else {
                sb.append(' ')
            }
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    private fun extractTokens(query: String): List<String> {
        val norm = normalizeTurkish(query)
        if (norm.isEmpty()) return emptyList()
        val rawTokens = norm.split(' ').filter { it.length >= 2 }
        val meaningful = rawTokens.filter { it !in STOP_WORDS }
        return meaningful.ifEmpty { rawTokens }
    }

    private fun expandTokens(tokens: List<String>): Set<String> {
        val expanded = LinkedHashSet<String>()
        for (t in tokens) {
            expanded.add(t)
            SYNONYM_MAP[t]?.forEach { expanded.add(it) }
        }
        return expanded
    }

    private fun scoreText(
        tokens: List<String>,
        expandedTokens: Set<String>,
        fullNormalizedQuery: String,
        primaryField: String,
        secondaryField: String,
        bodyField: String
    ): Int {
        val normPrimary = normalizeTurkish(primaryField)
        val normSecondary = normalizeTurkish(secondaryField)
        val normBody = normalizeTurkish(bodyField)

        var score = 0
        if (fullNormalizedQuery.length >= 3) {
            if (normPrimary.contains(fullNormalizedQuery)) score += 90
            else if (normSecondary.contains(fullNormalizedQuery)) score += 60
            else if (normBody.contains(fullNormalizedQuery)) score += 35
        }

        var matchedCoreCount = 0
        for (tok in tokens) {
            val hitPrimary = normPrimary.contains(tok)
            val hitSecondary = normSecondary.contains(tok)
            val hitBody = normBody.contains(tok)
            if (hitPrimary || hitSecondary || hitBody) {
                matchedCoreCount++
            }
            if (hitPrimary) score += 38
            else if (hitSecondary) score += 22
            else if (hitBody) score += 11
        }

        for (syn in expandedTokens) {
            if (syn in tokens) continue
            if (normPrimary.contains(syn)) score += 20
            else if (normSecondary.contains(syn)) score += 12
            else if (normBody.contains(syn)) score += 6
        }

        if (tokens.size > 1 && matchedCoreCount == tokens.size) {
            score += 45
        }
        return score
    }

    private fun pickRelevantCropSummary(crop: CropInfo, expandedTokens: Set<String>): Pair<String, String> {
        return when {
            expandedTokens.any { it in setOf("don", "soguk", "sicaklik", "esik", "kiragi") } -> {
                "❄️ Don & Sıcaklık Eşikleri: ${crop.frostAndTempThresholds}" to
                    "Kritik Dönem: ${crop.criticalStage}"
            }
            expandedTokens.any { it in setOf("sulama", "su", "et0", "damlama", "kurak") } -> {
                "💧 Su & Sulama: ${crop.waterNeedsAndIrrigation}" to
                    crop.irrigationAndNutrition
            }
            expandedTokens.any { it in setOf("gubre", "gubreleme", "besleme", "azot", "potasyum", "fosfor", "bor", "cinko") } -> {
                "🌱 Gübreleme Programı: ${crop.fertilizationProgram}" to
                    crop.irrigationAndNutrition
            }
            expandedTokens.any { it in setOf("hasat", "depolama", "briks", "saklama") } -> {
                "🧺 Hasat & Depolama: ${crop.harvestAndStorage}" to
                    "Kritik Dönem: ${crop.criticalStage}"
            }
            expandedTokens.any { it in setOf("toprak", "iklim", "ph", "kirec", "budama", "fenoloji") } -> {
                "🌍 İklim & Toprak: ${crop.climateAndSoil}" to
                    "Fenoloji: ${crop.phenologySteps.joinToString(" → ") { "${it.stage} (${it.timing})" }}"
            }
            else -> {
                "⏳ ${crop.criticalStage}. ${crop.climateAndSoil}" to
                    "🧪 Başlıca Etken Maddeler: ${crop.activeIngredientGroups}"
            }
        }
    }

    fun search(query: String): List<AgriculturalSearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return defaultHighlights()
        }

        val fullNorm = normalizeTurkish(trimmed)
        val tokens = extractTokens(trimmed)
        if (tokens.isEmpty()) {
            return defaultHighlights()
        }
        val expanded = expandTokens(tokens)
        val results = mutableListOf<AgriculturalSearchResult>()

        // 1. Search Crops & Their Embedded CropDiseases
        CropCatalog.crops.forEach { crop ->
            val cropBody = listOf(
                crop.subtitle,
                crop.climateAndSoil,
                crop.frostAndTempThresholds,
                crop.waterNeedsAndIrrigation,
                crop.fertilizationProgram,
                crop.harvestAndStorage,
                crop.criticalStage,
                crop.diseases,
                crop.symptoms,
                crop.management,
                crop.activeIngredientGroups,
                crop.monitoringThreshold,
                crop.phenologySteps.joinToString(" ") { "${it.stage} ${it.timing} ${it.focus}" }
            ).joinToString(" ")

            val cropScore = scoreText(
                tokens = tokens,
                expandedTokens = expanded,
                fullNormalizedQuery = fullNorm,
                primaryField = "${crop.id} ${crop.name}",
                secondaryField = "${crop.subtitle} ${crop.diseases} ${crop.criticalStage}",
                bodyField = cropBody
            )

            if (cropScore >= 18) {
                val (summary, actionHint) = pickRelevantCropSummary(crop, expanded)
                results += AgriculturalSearchResult(
                    id = "crop_${crop.id}",
                    category = AgriculturalSearchCategory.CROP,
                    title = "${crop.emoji} ${crop.name}",
                    subtitle = crop.subtitle,
                    detailSummary = summary,
                    actionHint = actionHint,
                    speechText = "${crop.name}. $summary. $actionHint",
                    score = cropScore,
                    cropId = crop.id
                )
            }

            crop.diseaseDetails.forEachIndexed { idx, disease ->
                val dScore = scoreText(
                    tokens = tokens,
                    expandedTokens = expanded,
                    fullNormalizedQuery = fullNorm,
                    primaryField = "${crop.name} ${disease.name}",
                    secondaryField = "${disease.agent} ${disease.visualBadge} ${crop.id}",
                    bodyField = "${disease.symptoms} ${disease.favorableWeather} ${disease.sprayAction}"
                )
                if (dScore >= 20) {
                    results += AgriculturalSearchResult(
                        id = "disease_${crop.id}_$idx",
                        category = AgriculturalSearchCategory.DISEASE,
                        title = "${crop.emoji} ${disease.name}",
                        subtitle = "${crop.name} • ${disease.agent}",
                        detailSummary = "🔍 Belirtiler: ${disease.symptoms}\n🌦️ Yayılma: ${disease.favorableWeather}",
                        actionHint = "🧪 Yapılacak İlaçlama: ${disease.sprayAction}",
                        speechText = "${crop.name} için ${disease.name}. Belirtiler: ${disease.symptoms}. Yapılacak ilaçlama: ${disease.sprayAction}",
                        score = dScore + 8, // boost specific disease matches for field diagnosis queries
                        cropId = crop.id
                    )
                }
            }
        }

        // 2. Search BKÜ Active Ingredients
        CropCatalog.bkuEntries.forEach { entry ->
            val bkuScore = scoreText(
                tokens = tokens,
                expandedTokens = expanded,
                fullNormalizedQuery = fullNorm,
                primaryField = "${entry.activeGroup} ${entry.category.label}",
                secondaryField = "${entry.targets} ${entry.crops}",
                bodyField = "${entry.weatherRule} ${entry.caution}"
            )
            if (bkuScore >= 18) {
                results += AgriculturalSearchResult(
                    id = "bku_${entry.id}",
                    category = AgriculturalSearchCategory.BKU,
                    title = "🧪 ${entry.activeGroup}",
                    subtitle = "${entry.category.label} • Ürünler: ${entry.crops}",
                    detailSummary = "🎯 Hedefler: ${entry.targets}\n🌦️ Hava Koşulu: ${entry.weatherRule}",
                    actionHint = "⚠️ Dikkat: ${entry.caution}",
                    speechText = "${entry.activeGroup}. Hedefler: ${entry.targets}. Hava koşulu: ${entry.weatherRule}. Dikkat: ${entry.caution}",
                    score = bkuScore + 4,
                    guideTopicId = GuideTopicId.BKU,
                    bkuFilterKeyword = tokens.firstOrNull() ?: ""
                )
            }
        }

        // 3. Search Agricultural Meteorology & Spraying Guide Sections
        CropCatalog.meteorologyGuideSections.forEachIndexed { idx, sec ->
            val joinedItems = sec.items.joinToString("\n• ")
            val mScore = scoreText(
                tokens = tokens,
                expandedTokens = expanded,
                fullNormalizedQuery = fullNorm,
                primaryField = sec.heading,
                secondaryField = "meteoroloji ilaclama puskurtme ruzgar don et0 sulama",
                bodyField = joinedItems
            )
            if (mScore >= 18) {
                results += AgriculturalSearchResult(
                    id = "met_$idx",
                    category = AgriculturalSearchCategory.METEOROLOGY,
                    title = sec.heading,
                    subtitle = "Tarımsal Meteoroloji & İlaçlama Kuralları",
                    detailSummary = "• $joinedItems",
                    actionHint = "Uygulama öncesi anlık rüzgâr (3–12 km/s) ve nem (%45–%80) penceresini kontrol edin.",
                    speechText = "${sec.heading}. ${sec.items.joinToString(". ")}",
                    score = mScore,
                    guideTopicId = GuideTopicId.METEOROLOGY
                )
            }
        }

        // 4. Search IPM & Plant Nutrition Sections
        CropCatalog.ipmGuideSections.forEachIndexed { idx, sec ->
            val joinedItems = sec.items.joinToString("\n• ")
            val ipmScore = scoreText(
                tokens = tokens,
                expandedTokens = expanded,
                fullNormalizedQuery = fullNorm,
                primaryField = sec.heading,
                secondaryField = "ipm entegre mucadele zarar esigi toprak gubre besleme",
                bodyField = joinedItems
            )
            if (ipmScore >= 18) {
                results += AgriculturalSearchResult(
                    id = "ipm_$idx",
                    category = AgriculturalSearchCategory.IPM,
                    title = sec.heading,
                    subtitle = "Entegre Mücadele (IPM) & Bitki Besleme",
                    detailSummary = "• $joinedItems",
                    actionHint = "Ekonomik zarar eşiği aşılmadan gereksiz kimyasal ilaçlama yapmayın.",
                    speechText = "${sec.heading}. ${sec.items.joinToString(". ")}",
                    score = ipmScore,
                    guideTopicId = GuideTopicId.IPM
                )
            }
        }

        return results
            .sortedByDescending { it.score }
            .take(15)
    }

    private fun defaultHighlights(): List<AgriculturalSearchResult> {
        val sampleCrops = listOf("Bağ", "Zeytin", "Badem", "Buğday").mapNotNull { id ->
            CropCatalog.crops.firstOrNull { it.id == id }
        }
        return sampleCrops.mapIndexed { idx, crop ->
            AgriculturalSearchResult(
                id = "default_${crop.id}",
                category = AgriculturalSearchCategory.CROP,
                title = "${crop.emoji} ${crop.name}",
                subtitle = crop.subtitle,
                detailSummary = "🦠 Başlıca Hastalıklar: ${crop.diseases}",
                actionHint = "🧪 Etken Maddeler: ${crop.activeIngredientGroups}",
                speechText = "${crop.name}. Kritik dönem: ${crop.criticalStage}. Başlıca hastalıklar: ${crop.diseases}",
                score = 100 - idx,
                cropId = crop.id
            )
        }
    }
}
