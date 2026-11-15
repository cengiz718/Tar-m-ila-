package com.tarimmeteoroloji.ai

import com.tarimmeteoroloji.ai.data.MarketAndNewsRepository
import com.tarimmeteoroloji.ai.data.VoiceRecognitionController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketAndSpeechTest {

    @Test
    fun parseTcmbXmlAndGtbTickerWorkCleanly() {
        val tcmbXml = """
            <Tarih_Date>
              <Currency CurrencyCode="USD">
                <ForexBuying>50.35</ForexBuying>
                <ForexSelling>50.38</ForexSelling>
              </Currency>
              <Currency CurrencyCode="EUR">
                <ForexBuying>55.55</ForexBuying>
                <ForexSelling>55.59</ForexSelling>
              </Currency>
            </Tarih_Date>
        """.trimIndent()
        val fx = MarketAndNewsRepository.parseTcmbXml(tcmbXml)
        assertEquals(2, fx.size)
        assertEquals("50.38 TL", fx[0].primaryPrice)

        val gtbTicker = """
            <span class="urunfiyatadi">SERT BUĞDAY (GÜNEYDOĞU)</span>
            <span class="urunfiyati">19.500 19.700</span>
            <span class="urunfiyatadi">Fundación del Olivar Extra Virgin Olive Oil (Naturel Sızma Zeytinyağı)</span>
            <span class="urunfiyati">3184.15( 177469.56)</span>
        """.trimIndent()
        val quotes = MarketAndNewsRepository.parseGtbTickerHtml(gtbTicker)
        assertEquals(2, quotes.size)
        assertTrue(quotes[1].name.contains("Zeytinyağı"))
        assertTrue(quotes[1].primaryPrice.contains("177,47"))
    }

    @Test
    fun parsePetrolOfisiHtmlExtracts81CityFuelColumns() {
        val sample = """
            <table><tbody>
            <tr class="price-row district-00200" data-disctrict-id="00200" data-disctrict-name="ADIYAMAN">
              <td>ADIYAMAN</td>
              <td><span class="with-tax">72.45</span></td>
              <td><span class="with-tax">96.80</span></td>
              <td><span class="with-tax">92.10</span></td>
              <td><span class="with-tax">65.00</span></td>
              <td><span class="with-tax">55.00</span></td>
              <td><span class="with-tax">34.90</span></td>
            </tr>
            </tbody></table>
        """.trimIndent()
        val cities = MarketAndNewsRepository.parsePetrolOfisiHtml(sample)
        assertEquals(1, cities.size)
        val adiyaman = cities.first()
        assertEquals("ADIYAMAN", adiyaman.cityName)
        assertEquals("72.45", adiyaman.gasoline95)
        assertEquals("96.80", adiyaman.diesel)
        assertEquals("34.90", adiyaman.lpg)

        val matched = MarketAndNewsRepository.matchFuelCity("Adıyaman Merkez", cities)
        assertNotNull(matched)
        assertEquals("ADIYAMAN", matched?.cityName)
    }

    @Test
    fun parseGtbBultenNormalizesThousandCommaPrices() {
        val html = """
            <table>
              <tr>
                <td>BADEM İÇİ(TATLI)</td><td>566,000</td><td>600,000</td><td>578,061</td>
                <td>2.901</td><td>KG</td><td>1.676.878,000</td><td>HTS HAZIR TACİR SATIŞI</td>
              </tr>
              <tr>
                <td>ANTEP FIS.BOZ İÇ</td><td>1,900</td><td>2,000</td><td>1,928</td>
                <td>140</td><td>KG</td><td>270.000,000</td><td>HTS HAZIR TACİR SATIŞI</td>
              </tr>
            </table>
        """.trimIndent()
        val quotes = MarketAndNewsRepository.parseGtbBultenHtml(html)
        assertEquals(2, quotes.size)
        val badem = quotes.first { it.name.contains("Badem") }
        assertTrue(badem.primaryPrice.contains("566"))
        val fistik = quotes.first { it.name.contains("Antep") }
        assertTrue(fistik.primaryPrice.contains("1.900"))
    }

    @Test
    fun parseTuribAndUkonHtmlWorkCleanly() {
        val turibHtml = """
            <a href="/Index?ProductGroup=Arpa"><span class="IndexValue">15.09</span><span class="ProductIndexPercentage">0.52</span></a>
        """.trimIndent()
        val turib = MarketAndNewsRepository.parseTuribHtml(turibHtml)
        assertEquals(1, turib.size)
        assertTrue(turib.first().name.contains("Arpa"))

        val ukonHtml = """
            <table>
              <tr><td>Ortalama</td><td>598,11</td><td>611,57</td></tr>
              <tr><td>Geçen Aya Göre Değişim (%)</td><td>2,9</td><td>-0,9</td></tr>
            </table>
        """.trimIndent()
        val ukon = MarketAndNewsRepository.parseUkonHtml(ukonHtml)
        assertEquals(2, ukon.size)
        assertEquals("598,11 TL", ukon[0].primaryPrice)
    }

    @Test
    fun parseRssAndChunkSpeechWorkCleanly() {
        val rss = """
            <rss><channel>
              <item>
                <title><![CDATA[Üre gübresine güncel fiyat ayarlaması yapıldı]]></title>
                <description><![CDATA[Tarım Kredi ve bayilerde yeni gübre fiyatları açıklandı.]]></description>
                <category>Tarım</category>
                <pubDate>Sun, 15 Nov 2026 17:50:00 +0300</pubDate>
              </item>
            </channel></rss>
        """.trimIndent()
        val articles = MarketAndNewsRepository.parseRssXml(rss, "TarımdanHaber")
        assertEquals(1, articles.size)
        assertTrue(articles.first().isFertilizerTopic)

        val longText = List(40) { "Bağda külleme mücadelesi sabah erken saatlerde yapılmalıdır." }.joinToString(" ")
        val cleaned = VoiceRecognitionController.cleanSpeechText(longText)
        val chunks = VoiceRecognitionController.splitSpeechChunks(cleaned, maxChunkLen = 400)
        assertTrue(chunks.size >= 4)
        assertTrue(chunks.all { it.length <= 400 })
    }
}
