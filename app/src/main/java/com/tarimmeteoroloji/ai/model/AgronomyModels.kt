package com.tarimmeteoroloji.ai.model

import android.graphics.Bitmap

data class CropDiseaseDetail(
    val name: String,
    val agent: String,
    val visualSymptomKind: SymptomPattern,
    val visualBadge: String,
    val symptoms: String,
    val favorableWeather: String,
    val sprayAction: String
)

data class CropPhenologyStep(
    val stage: String,
    val timing: String,
    val focus: String
)

data class CropInfo(
    val id: String,
    val emoji: String,
    val name: String,
    val colorHex: Long,
    val subtitle: String,
    val climateAndSoil: String,
    val frostAndTempThresholds: String,
    val waterNeedsAndIrrigation: String,
    val fertilizationProgram: String,
    val harvestAndStorage: String,
    val criticalStage: String,
    val diseases: String,
    val symptoms: String,
    val management: String,
    val activeIngredientGroups: String,
    val monitoringThreshold: String,
    val irrigationAndNutrition: String,
    val phenologySteps: List<CropPhenologyStep>,
    val diseaseDetails: List<CropDiseaseDetail>,
    val tempRiskMin: Double,
    val tempRiskMax: Double,
    val humidityThreshold: Int
)

data class CityPreset(
    val name: String,
    val lat: Double,
    val lon: Double
)

enum class GuideTopicId {
    METEOROLOGY,
    BKU,
    IPM
}

data class GuideTopicButton(
    val id: GuideTopicId,
    val title: String,
    val subtitle: String
)

enum class BkuCategory(val label: String) {
    ALL("Tümü"),
    FUNGICIDE("Fungisit"),
    INSECTICIDE("İnsektisit / Akarisit"),
    HERBICIDE("Herbisit"),
    BIOLOGICAL("Biyolojik / Organik")
}

data class BkuEntry(
    val id: String,
    val category: BkuCategory,
    val activeGroup: String,
    val targets: String,
    val crops: String,
    val weatherRule: String,
    val caution: String
)

data class GuideSection(
    val heading: String,
    val items: List<String>
)

data class CurrentWeather(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val precipitation: Double,
    val weatherCode: Int
)

data class WeatherDay(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationSum: Double,
    val precipitationProbMax: Int,
    val windSpeedMax: Double,
    val humidityMax: Int,
    val et0: Double
)

data class WeatherSnapshot(
    val current: CurrentWeather?,
    val daily: List<WeatherDay>
)

enum class SprayStatusLevel {
    OK,
    WARN,
    DANGER
}

data class SprayEvaluation(
    val level: SprayStatusLevel,
    val title: String,
    val detail: String
)

data class AgronomicReport(
    val sections: List<String>,
    val avgTemp: Double,
    val maxHum: Double,
    val totalRain: Double
)

enum class SymptomPattern(val label: String, val description: String) {
    HEALTHY("Sağlıklı Yeşil Doku", "Belirgin hastalık lekesi baskın değil"),
    POWDERY_WHITE("Beyaz Unsu / Küflü Kaplama", "Külleme, mildiyö miselyumu veya kurşuni küf belirtisi"),
    YELLOW_RUST("Sarı / Turuncu Pas ve Kloroz", "Pas püstülleri, yaprak sararması veya yağ lekesi"),
    NECROTIC_SPOT("Koyu Kahverengi / Siyah Nekrotik Leke", "Karaleke, antraknoz, halkalı leke veya yanıklık belirtisi")
}

data class DiseaseProfile(
    val id: String,
    val cropIds: List<String>,
    val pattern: SymptomPattern,
    val diseaseName: String,
    val agentType: String,
    val visualSigns: String,
    val sprayRecommendation: String,
    val activeIngredients: String,
    val timingAndWeatherNote: String,
    val culturalMeasures: String,
    val preferredTempMin: Double,
    val preferredTempMax: Double,
    val minHumidity: Int
)

data class PlantDiagnosisResult(
    val isVideo: Boolean,
    val framesAnalyzed: Int,
    val previewBitmap: Bitmap?,
    val selectedCropName: String,
    val healthyGreenPct: Int,
    val powderyWhitePct: Int,
    val yellowRustPct: Int,
    val necroticSpotPct: Int,
    val affectedPct: Int,
    val severityLabel: String,
    val dominantPattern: SymptomPattern,
    val primaryMatch: DiseaseProfile,
    val secondaryMatch: DiseaseProfile?,
    val confidencePct: Int,
    val weatherTimingAdvice: String
)

enum class AgriculturalSearchCategory(val label: String, val emoji: String) {
    DISEASE("Hastalık & İlaçlama", "🦠"),
    CROP("Ürün Rehberi", "🌱"),
    BKU("Etken Madde (BKÜ)", "🧪"),
    METEOROLOGY("Meteoroloji & İlaçlama", "☁️"),
    IPM("Entegre Mücadele (IPM)", "🌿")
}

data class AgriculturalSearchResult(
    val id: String,
    val category: AgriculturalSearchCategory,
    val title: String,
    val subtitle: String,
    val detailSummary: String,
    val actionHint: String,
    val speechText: String,
    val score: Int,
    val cropId: String? = null,
    val guideTopicId: GuideTopicId? = null,
    val bkuFilterKeyword: String? = null
)

enum class MarketCategory(val label: String, val emoji: String) {
    ALL("Tümü", "📊"),
    COMMODITY("Ürün & Borsa", "🌾"),
    LIVESTOCK("Hayvancılık & Et", "🐄"),
    FUEL("Akaryakıt", "⛽"),
    FX_GOLD("Döviz & Altın", "💰"),
    FERTILIZER("Gübre & Girdi", "🧪"),
    NEWS("Haberler", "📰")
}

data class MarketQuote(
    val id: String,
    val name: String,
    val category: MarketCategory,
    val primaryPrice: String,
    val secondaryPrice: String = "",
    val unit: String,
    val changeText: String = "",
    val isPositive: Boolean? = null,
    val source: String,
    val detailNote: String = ""
)

data class FuelCityPrices(
    val cityName: String,
    val gasoline95: String,
    val diesel: String,
    val lpg: String,
    val kerosene: String = ""
)

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val source: String,
    val publishedAt: String,
    val isFertilizerTopic: Boolean = false
)

data class MarketDashboardSnapshot(
    val fxGoldQuotes: List<MarketQuote>,
    val commodityQuotes: List<MarketQuote>,
    val livestockQuotes: List<MarketQuote>,
    val fuelCities: List<FuelCityPrices>,
    val fertilizerBulletins: List<NewsArticle>,
    val newsArticles: List<NewsArticle>,
    val fetchedAtLabel: String,
    val sourceSummary: String,
    val fromCache: Boolean = false
)
