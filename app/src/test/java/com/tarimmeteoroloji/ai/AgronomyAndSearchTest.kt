package com.tarimmeteoroloji.ai

import com.tarimmeteoroloji.ai.data.AgriculturalSearchEngine
import com.tarimmeteoroloji.ai.data.AgronomyEngine
import com.tarimmeteoroloji.ai.model.SprayStatusLevel
import com.tarimmeteoroloji.ai.model.WeatherDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgronomyAndSearchTest {

    @Test
    fun sprayConditionRejectsHighWindAndRain() {
        val windy = AgronomyEngine.evaluateSprayCondition(
            temp = 20.0,
            hum = 60,
            wind = 21.0,
            curRain = 0.0,
            todayRain = 0.0,
            rainProb = 10
        )
        assertEquals(SprayStatusLevel.DANGER, windy.level)

        val rainy = AgronomyEngine.evaluateSprayCondition(
            temp = 19.0,
            hum = 75,
            wind = 5.0,
            curRain = 0.8,
            todayRain = 4.0,
            rainProb = 80
        )
        assertEquals(SprayStatusLevel.DANGER, rainy.level)

        val ideal = AgronomyEngine.evaluateSprayCondition(
            temp = 21.0,
            hum = 58,
            wind = 7.0,
            curRain = 0.0,
            todayRain = 0.0,
            rainProb = 10
        )
        assertEquals(SprayStatusLevel.OK, ideal.level)
    }

    @Test
    fun formatDayLabelHandlesValidAndShortStrings() {
        assertEquals("Bugün", AgronomyEngine.formatDayLabel("2026-11-15", 0))
        assertEquals("16.11", AgronomyEngine.formatDayLabel("2026-11-16", 1))
        assertEquals("Gün 3", AgronomyEngine.formatDayLabel("", 2))
    }

    @Test
    fun agronomicReportDetectsFrostAndWaterDeficit() {
        val days = listOf(
            WeatherDay("2026-11-15", 0, 14.0, 1.0, 0.0, 10, 8.0, 60, 4.2),
            WeatherDay("2026-11-16", 1, 15.0, 2.0, 0.0, 10, 9.0, 62, 4.5),
            WeatherDay("2026-11-17", 0, 17.0, 5.0, 1.0, 15, 7.0, 65, 5.0),
            WeatherDay("2026-11-18", 0, 18.0, 6.0, 0.0, 5, 6.0, 55, 5.1)
        )
        val report = AgronomyEngine.buildAgronomicReport(days)
        assertTrue(report.sections.any { it.contains("DON UYARISI") })
        assertTrue(report.sections.any { it.contains("SU BÜTÇESİ") })
    }

    @Test
    fun voiceSearchMatchesTurkishCropsDiseasesAndBku() {
        val grapeResults = AgriculturalSearchEngine.search("Bağ küllemesi ilacı nedir")
        assertTrue(grapeResults.isNotEmpty())
        assertTrue(grapeResults.first().title.contains("Külleme", ignoreCase = true))

        val frostResults = AgriculturalSearchEngine.search("Badem don kaç derece")
        assertTrue(frostResults.isNotEmpty())
        assertEquals("Badem", frostResults.first().cropId)

        val sulfurResults = AgriculturalSearchEngine.search("Kükürt kaç derecede atılır")
        assertNotNull(sulfurResults.firstOrNull { it.id == "bku_kukurt" })
    }
}
