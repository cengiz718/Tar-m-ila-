package com.tarimmeteoroloji.ai.data

import android.content.Context
import com.tarimmeteoroloji.ai.model.FuelCityPrices
import com.tarimmeteoroloji.ai.model.MarketCategory
import com.tarimmeteoroloji.ai.model.MarketDashboardSnapshot
import com.tarimmeteoroloji.ai.model.MarketQuote
import com.tarimmeteoroloji.ai.model.NewsArticle
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarketAndNewsRepository(private val context: Context) {

    private val weatherRepo = WeatherRepository(context.applicationContext)

    fun loadCachedDashboard(): MarketDashboardSnapshot? {
        val raw = weatherRepo.loadMarketCacheJson() ?: return null
        return runCatching { deserializeSnapshot(raw) }.getOrNull()
    }

    suspend fun fetchLiveDashboard(): MarketDashboardSnapshot = coroutineScope {
        val fxDeferred = async { runCatching { fetchTruncgilOrTcmb() }.getOrDefault(emptyList()) }
        val turibDeferred = async { runCatching { parseTuribHtml(httpGet(URL_TURIB)) }.getOrDefault(emptyList()) }
        val gtbHomeDeferred = async { runCatching { parseGtbTickerHtml(httpGet(URL_GTB_HOME)) }.getOrDefault(emptyList()) }
        val gtbBultenDeferred = async { runCatching { parseGtbBultenHtml(httpGet(URL_GTB_BULTEN)) }.getOrDefault(emptyList()) }
        val ukonDeferred = async { runCatching { parseUkonHtml(httpGet(URL_UKON)) }.getOrDefault(emptyList()) }
        val fuelDeferred = async { runCatching { parsePetrolOfisiHtml(httpGet(URL_PETROL_OFISI)) }.getOrDefault(emptyList()) }
        val tarimRssDeferred = async {
            runCatching {
                parseRssXml(httpGet(URL_TARIMDANHABER_RSS), defaultSource = "TarımdanHaber")
            }.getOrDefault(emptyList())
        }
        val fertRssDeferred = async {
            runCatching {
                parseRssXml(
                    httpGet(URL_GNEWS_FERTILIZER),
                    defaultSource = "Tarım Piyasası",
                    forceFertilizer = true
                )
            }.getOrDefault(emptyList())
        }
        val agriNewsDeferred = async {
            runCatching {
                parseRssXml(httpGet(URL_GNEWS_AGRI), defaultSource = "Tarım & Hayvancılık")
            }.getOrDefault(emptyList())
        }
        val cropMarketNewsDeferred = async {
            runCatching {
                parseRssXml(httpGet(URL_GNEWS_CROP_PRICES), defaultSource = "Ürün Borsası")
            }.getOrDefault(emptyList())
        }

        val fxGold = fxDeferred.await()
        val turib = turibDeferred.await()
        val gtbHome = gtbHomeDeferred.await()
        val gtbBulten = gtbBultenDeferred.await()
        val ukon = ukonDeferred.await()
        val fuelCities = fuelDeferred.await()
        val tarimRss = tarimRssDeferred.await()
        val fertRss = fertRssDeferred.await()
        val agriRss = agriNewsDeferred.await()
        val cropMarketRss = cropMarketNewsDeferred.await()

        val gtbLivestocks = gtbHome.filter { it.category == MarketCategory.LIVESTOCK }
        val gtbCommodities = gtbHome.filter { it.category == MarketCategory.COMMODITY }

        val mergedCommodities = deduplicateQuotes(turib + gtbBulten + gtbCommodities)
        val mergedLivestock = deduplicateQuotes(ukon + gtbLivestocks)

        val allNewsRaw = (tarimRss + cropMarketRss + agriRss + fertRss)
            .distinctBy { AgriculturalSearchEngine.normalizeTurkish(it.title).take(52) }

        val fertilizerBulletins = allNewsRaw
            .filter { item ->
                item.isFertilizerTopic || containsFertilizerKeyword("${item.title} ${item.summary}")
            }
            .take(15)

        val newsArticles = allNewsRaw
            .filterNot { it.isFertilizerTopic && fertilizerBulletins.any { fb -> fb.id == it.id } }
            .ifEmpty { allNewsRaw }
            .take(25)

        val hasAnyLive = fxGold.isNotEmpty() ||
            mergedCommodities.isNotEmpty() ||
            mergedLivestock.isNotEmpty() ||
            fuelCities.isNotEmpty() ||
            newsArticles.isNotEmpty() ||
            fertilizerBulletins.isNotEmpty()

        if (!hasAnyLive) {
            throw IOException("Canlı piyasa ve haber kaynaklarına erişilemedi")
        }

        val nowStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        val activeSources = buildList {
            if (mergedCommodities.isNotEmpty()) add("TÜRİB & GTB")
            if (mergedLivestock.isNotEmpty()) add("UKON Et Konseyi")
            if (fuelCities.isNotEmpty()) add("Petrol Ofisi")
            if (fxGold.isNotEmpty()) add("Canlı Döviz/Altın")
            if (newsArticles.isNotEmpty() || fertilizerBulletins.isNotEmpty()) add("TarımdanHaber & Haberler")
        }.joinToString(" • ")

        val snapshot = MarketDashboardSnapshot(
            fxGoldQuotes = fxGold,
            commodityQuotes = mergedCommodities,
            livestockQuotes = mergedLivestock,
            fuelCities = fuelCities,
            fertilizerBulletins = fertilizerBulletins,
            newsArticles = newsArticles,
            fetchedAtLabel = nowStr,
            sourceSummary = activeSources,
            fromCache = false
        )

        runCatching {
            weatherRepo.saveMarketCacheJson(serializeSnapshot(snapshot))
        }
        snapshot
    }

    private fun fetchTruncgilOrTcmb(): List<MarketQuote> {
        val truncgil = runCatching { parseTruncgilJson(httpGet(URL_TRUNCGIL)) }.getOrDefault(emptyList())
        if (truncgil.isNotEmpty()) return truncgil
        return runCatching { parseTcmbXml(httpGet(URL_TCMB_XML)) }.getOrDefault(emptyList())
    }

    private fun httpGet(urlStr: String): String {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 11000
            readTimeout = 11000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36"
            )
            setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8")
        }
        return try {
            val code = conn.responseCode
            if (code !in 200..299) {
                throw IOException("HTTP $code")
            }
            BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { br ->
                val sb = StringBuilder()
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    sb.append(line).append('\n')
                }
                sb.toString()
            }
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        private const val URL_TRUNCGIL = "https://finans.truncgil.com/today.json"
        private const val URL_TCMB_XML = "https://www.tcmb.gov.tr/kurlar/today.xml"
        private const val URL_TURIB = "https://www.turib.com.tr/"
        private const val URL_GTB_HOME = "https://www.gtb.org.tr/"
        private const val URL_GTB_BULTEN = "https://www.gtb.org.tr/gunluk-bulten"
        private const val URL_UKON = "https://www.ukon.org.tr/fiyatlar"
        private const val URL_PETROL_OFISI = "https://www.petrolofisi.com.tr/akaryakit-fiyatlari"
        private const val URL_TARIMDANHABER_RSS = "https://www.tarimdanhaber.com/rss"
        private const val URL_GNEWS_FERTILIZER =
            "https://news.google.com/rss/search?q=g%C3%BCbre+fiyatlar%C4%B1+%C3%BCre+dap+20+20+tar%C4%B1m+kredi&hl=tr&gl=TR&ceid=TR:tr"
        private const val URL_GNEWS_AGRI =
            "https://news.google.com/rss/search?q=tar%C4%B1m+hayvanc%C4%B1l%C4%B1k+tmo+destekleme+t%C3%BCrkiye&hl=tr&gl=TR&ceid=TR:tr"
        private const val URL_GNEWS_CROP_PRICES =
            "https://news.google.com/rss/search?q=(arpa+OR+bu%C4%9Fday+OR+zeytin+OR+zeytinya%C4%9F%C4%B1+OR+antep+f%C4%B1st%C4%B1%C4%9F%C4%B1+OR+badem)+fiyatlar%C4%B1&hl=tr&gl=TR&ceid=TR:tr"

        private val trFormatSymbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }

        fun formatTrPrice(value: Double, decimals: Int = 2): String {
            val pattern = if (decimals == 0) "#,##0" else "#,##0.${"0".repeat(decimals)}"
            return DecimalFormat(pattern, trFormatSymbols).format(value)
        }

        fun parseTrNumber(raw: String): Double? {
            val cleaned = raw
                .replace("TL", "", ignoreCase = true)
                .replace("%", "")
                .replace(" ", "")
                .trim()
            if (cleaned.isEmpty()) return null
            val normalized = when {
                cleaned.contains(",") && cleaned.contains(".") ->
                    cleaned.replace(".", "").replace(",", ".")
                cleaned.contains(",") ->
                    cleaned.replace(",", ".")
                else -> cleaned
            }
            return normalized.toDoubleOrNull()
        }

        fun parseTruncgilJson(rawJson: String): List<MarketQuote> {
            val root = JSONObject(rawJson)
            val updated = root.optString("Update_Date", "")
            val desired = listOf(
                "USD" to "ABD Doları (USD)",
                "EUR" to "Euro (EUR)",
                "GBP" to "İngiliz Sterlini (GBP)",
                "gram-altin" to "Gram Altın",
                "ceyrek-altin" to "Çeyrek Altın",
                "yarim-altin" to "Yarım Altın",
                "tam-altin" to "Tam Altın",
                "cumhuriyet-altini" to "Cumhuriyet Altını",
                "gumus" to "Gümüş (Gram)"
            )
            return desired.mapNotNull { (key, label) ->
                val obj = root.optJSONObject(key) ?: return@mapNotNull null
                val buy = obj.optString("Alış", "").trim()
                val sell = obj.optString("Satış", "").trim()
                if (buy.isEmpty() && sell.isEmpty()) return@mapNotNull null
                val change = obj.optString("Değişim", "").trim()
                val isPos = when {
                    change.contains("-") -> false
                    change.isNotEmpty() && !change.endsWith("0,00") && !change.endsWith("0.00") -> true
                    else -> null
                }
                MarketQuote(
                    id = "fx_$key",
                    name = label,
                    category = MarketCategory.FX_GOLD,
                    primaryPrice = if (sell.isNotEmpty()) "$sell TL" else "$buy TL",
                    secondaryPrice = if (buy.isNotEmpty()) "Alış: $buy TL" else "",
                    unit = "TL",
                    changeText = change,
                    isPositive = isPos,
                    source = "Canlı Döviz/Altın",
                    detailNote = if (updated.isNotEmpty()) "Güncelleme: $updated" else ""
                )
            }
        }

        fun parseTcmbXml(rawXml: String): List<MarketQuote> {
            val result = mutableListOf<MarketQuote>()
            val wanted = mapOf(
                "USD" to "ABD Doları (USD)",
                "EUR" to "Euro (EUR)",
                "GBP" to "İngiliz Sterlini (GBP)"
            )
            val currencyRegex = Regex("<Currency[^>]*CurrencyCode=\"([A-Z]+)\"[^>]*>(.*?)</Currency>", RegexOption.DOT_MATCHES_ALL)
            for (match in currencyRegex.findAll(rawXml)) {
                val code = match.groupValues[1]
                val label = wanted[code] ?: continue
                val block = match.groupValues[2]
                val buy = Regex("<ForexBuying>(.*?)</ForexBuying>").find(block)?.groupValues?.getOrNull(1)?.trim().orEmpty()
                val sell = Regex("<ForexSelling>(.*?)</ForexSelling>").find(block)?.groupValues?.getOrNull(1)?.trim().orEmpty()
                if (buy.isNotEmpty() || sell.isNotEmpty()) {
                    result.add(
                        MarketQuote(
                            id = "tcmb_$code",
                            name = label,
                            category = MarketCategory.FX_GOLD,
                            primaryPrice = "${sell.ifEmpty { buy }} TL",
                            secondaryPrice = if (buy.isNotEmpty()) "Alış: $buy TL" else "",
                            unit = "TL",
                            source = "TCMB Resmi Kur"
                        )
                    )
                }
            }
            return result
        }

        fun parseTuribHtml(html: String): List<MarketQuote> {
            val regex = Regex(
                "ProductGroup=([^\"&<]+).*?IndexValue\">([^<]+)<.*?ProductIndexPercentage[^>]*>([^<]+)<",
                RegexOption.DOT_MATCHES_ALL
            )
            val items = mutableListOf<MarketQuote>()
            val seen = mutableSetOf<String>()
            for (match in regex.findAll(html)) {
                val rawName = decodeHtmlEntities(match.groupValues[1]).trim()
                if (rawName.isEmpty() || !seen.add(rawName)) continue
                val valStr = match.groupValues[2].trim()
                val pctStr = match.groupValues[3].trim()
                val parsedVal = parseTrNumber(valStr)
                val displayPrice = if (parsedVal != null) "${formatTrPrice(parsedVal, 2)} TL" else "$valStr TL"
                val isPos = when {
                    pctStr.startsWith("-") -> false
                    pctStr.isNotEmpty() -> true
                    else -> null
                }
                items.add(
                    MarketQuote(
                        id = "turib_${rawName.lowercase(Locale.ROOT).replace(" ", "_")}",
                        name = "$rawName (TÜRİB Endeks)",
                        category = MarketCategory.COMMODITY,
                        primaryPrice = displayPrice,
                        secondaryPrice = parsedVal?.let { "Ton: ${formatTrPrice(it * 1000.0, 0)} TL" }.orEmpty(),
                        unit = "TL / kg",
                        changeText = if (pctStr.contains("%")) pctStr else "%$pctStr",
                        isPositive = isPos,
                        source = "TÜRİB Ürün İhtisas Borsası",
                        detailNote = "Günlük TÜRİB fiyat endeksi"
                    )
                )
            }
            return items
        }

        fun parseGtbTickerHtml(html: String): List<MarketQuote> {
            val names = Regex("<span[^>]*class=\"urunfiyatadi\"[^>]*>(.*?)</span>", RegexOption.DOT_MATCHES_ALL)
                .findAll(html)
                .map { stripTags(it.groupValues[1]) }
                .toList()
            val prices = Regex("<span[^>]*class=\"urunfiyati\"[^>]*>(.*?)</span>", RegexOption.DOT_MATCHES_ALL)
                .findAll(html)
                .map { stripTags(it.groupValues[1]) }
                .toList()

            val quotes = mutableListOf<MarketQuote>()
            for (i in 0 until minOf(names.size, prices.size)) {
                val rawName = names[i]
                val rawPrice = prices[i]
                if (rawName.isBlank() || rawPrice.isBlank()) continue

                val upper = rawName.uppercase(Locale("tr", "TR"))
                when {
                    upper.contains("BUĞDAY") && !upper.contains("CBOT") -> {
                        val nums = Regex("(\\d+[.,]\\d+)").findAll(rawPrice).map { it.value }.toList()
                        val formatted = if (nums.size >= 2) "${nums[0]} – ${nums[1]} TL" else "${nums.firstOrNull() ?: rawPrice} TL"
                        quotes.add(
                            MarketQuote(
                                id = "gtb_tick_$i",
                                name = rawName,
                                category = MarketCategory.COMMODITY,
                                primaryPrice = formatted,
                                unit = "TL / kg",
                                source = "Gaziantep Ticaret Borsası",
                                detailNote = "GTB Salon Satış Fiyatı"
                            )
                        )
                    }
                    upper.contains("ARPA") || upper.contains("MISIR") && !upper.contains("CBOT") || upper.contains("MERCİMEK") -> {
                        val nums = Regex("(\\d+[.,]\\d+)").findAll(rawPrice).map { it.value }.toList()
                        val formatted = if (nums.size >= 2) "${nums[0]} – ${nums[1]} TL" else "${nums.firstOrNull() ?: rawPrice} TL"
                        quotes.add(
                            MarketQuote(
                                id = "gtb_tick_$i",
                                name = rawName,
                                category = MarketCategory.COMMODITY,
                                primaryPrice = formatted,
                                unit = "TL / kg",
                                source = "Gaziantep Ticaret Borsası",
                                detailNote = "GTB Salon Satış Fiyatı"
                            )
                        )
                    }
                    upper.contains("OLIVE OIL") || upper.contains("ZEYTİNYAĞ") -> {
                        val parenMatches = Regex("\\(([^)]+)\\)").findAll(rawPrice).map { it.groupValues[1].trim() }.toList()
                        val tlTonStr = parenMatches.firstOrNull()
                        val tlTonNum = tlTonStr?.let { it.replace(",", "").toDoubleOrNull() }
                        val tlKgDisplay = tlTonNum?.let { "${formatTrPrice(it / 1000.0, 2)} TL/kg" }.orEmpty()
                        val tlTonDisplay = tlTonNum?.let { "${formatTrPrice(it, 0)} TL/Ton" } ?: rawPrice
                        val cleanTitle = when {
                            upper.contains("EXTRA VIRGIN") -> "Naturel Sızma Zeytinyağı"
                            upper.contains("LAMPANTE") -> "Ham / Rafinajlık Zeytinyağı"
                            else -> "Sızma Zeytinyağı"
                        }
                        quotes.add(
                            MarketQuote(
                                id = "gtb_olive_$i",
                                name = cleanTitle,
                                category = MarketCategory.COMMODITY,
                                primaryPrice = tlKgDisplay.ifEmpty { tlTonDisplay },
                                secondaryPrice = tlTonDisplay,
                                unit = "TL / kg",
                                source = "POOLred / GTB Uluslararası",
                                detailNote = "Fundación del Olivar referans fiyatı"
                            )
                        )
                    }
                    upper.contains("LIVE CATTLE") || upper.contains("FEEDER CATTLE") -> {
                        val tlNums = Regex("\\(([^)]+)\\)").findAll(rawPrice).mapNotNull { m ->
                            m.groupValues[1].trim().replace(",", "").toDoubleOrNull()
                        }.toList()
                        // Raw quote is per pound (0.45359 kg), convert to TL/kg estimate if parsed
                        val perKgLow = tlNums.firstOrNull()?.let { it / 0.45359 }
                        val perKgHigh = tlNums.getOrNull(1)?.let { it / 0.45359 }
                        val display = when {
                            perKgLow != null && perKgHigh != null ->
                                "${formatTrPrice(perKgLow, 2)} – ${formatTrPrice(perKgHigh, 2)} TL"
                            perKgLow != null -> "${formatTrPrice(perKgLow, 2)} TL"
                            else -> rawPrice
                        }
                        val title = if (upper.contains("LIVE CATTLE")) {
                            "Canlı Sığır Referans (CME)"
                        } else {
                            "Besi Sığır Referans (CME)"
                        }
                        quotes.add(
                            MarketQuote(
                                id = "gtb_cattle_$i",
                                name = title,
                                category = MarketCategory.LIVESTOCK,
                                primaryPrice = display,
                                unit = "TL / kg (Canlı)",
                                source = "CME / GTB Uluslararası",
                                detailNote = "Küresel canlı hayvan vadeli göstergesi"
                            )
                        )
                    }
                }
            }
            return quotes
        }

        fun parseGtbBultenHtml(html: String): List<MarketQuote> {
            val rowRegex = Regex("<tr[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL)
            val cellRegex = Regex("<t[dh][^>]*>(.*?)</t[dh]>", RegexOption.DOT_MATCHES_ALL)
            val quotes = mutableListOf<MarketQuote>()

            val priorityProducts = listOf(
                "BADEM",
                "ANTEP FIS",
                "BUĞDAY",
                "ARPA",
                "ZEYT",
                "MERCİMEK",
                "MISIR"
            )

            val groupedBest = LinkedHashMap<String, MarketQuote>()

            for (rowMatch in rowRegex.findAll(html)) {
                val cells = cellRegex.findAll(rowMatch.groupValues[1])
                    .map { stripTags(it.groupValues[1]) }
                    .toList()
                if (cells.size < 7) continue
                val name = cells[0].trim()
                val upper = name.uppercase(Locale("tr", "TR"))
                if (priorityProducts.none { upper.contains(it) }) continue

                var minVal = parseTrNumber(cells[1]) ?: continue
                var maxVal = parseTrNumber(cells[2]) ?: minVal
                var avgVal = parseTrNumber(cells[3]) ?: minVal
                val qty = parseTrNumber(cells[4]) ?: 0.0
                val unit = cells[5].ifBlank { "KG" }
                val totalAmount = parseTrNumber(cells[6]) ?: 0.0
                val tradeType = cells.getOrNull(7).orEmpty()

                // GTB bulletin uses "1,900" for 1.900 TL when unit price >= 500 TL/kg
                if (qty > 0.0 && totalAmount > 0.0) {
                    val computedAvg = totalAmount / qty
                    if (computedAvg >= 500.0 && avgVal < 50.0) {
                        minVal *= 1000.0
                        maxVal *= 1000.0
                        avgVal *= 1000.0
                    }
                }

                val isProducerSale = tradeType.contains("MÜSTAHSİL", ignoreCase = true)
                val priceLabel = if (kotlin.math.abs(maxVal - minVal) < 0.01) {
                    "${formatTrPrice(avgVal, 2)} TL"
                } else {
                    "${formatTrPrice(minVal, 2)} – ${formatTrPrice(maxVal, 2)} TL"
                }
                val note = buildString {
                    append("Ort: ${formatTrPrice(avgVal, 2)} TL/$unit")
                    if (qty > 0) append(" • Hacim: ${formatTrPrice(qty, 0)} $unit")
                    if (tradeType.isNotEmpty()) append(" • ${tradeType.take(28)}")
                }

                val normalizedKey = name.uppercase(Locale("tr", "TR"))
                val candidate = MarketQuote(
                    id = "gtb_b_${normalizedKey.hashCode()}",
                    name = beautifyGtbName(name),
                    category = MarketCategory.COMMODITY,
                    primaryPrice = priceLabel,
                    secondaryPrice = "Ort: ${formatTrPrice(avgVal, 2)} TL/$unit",
                    unit = "TL / $unit",
                    source = "GTB Günlük Bülten",
                    detailNote = note
                )

                // Prefer Mustahsil (producer) sale or higher volume row when product appears multiple times
                if (!groupedBest.containsKey(normalizedKey) || isProducerSale) {
                    groupedBest[normalizedKey] = candidate
                }
            }

            // Order nicely: Badem, Antep Fıstığı varieties, Buğday, Arpa, others
            val ordered = groupedBest.values.sortedWith(
                compareBy<MarketQuote> { q ->
                    val u = q.name.uppercase(Locale("tr", "TR"))
                    when {
                        u.contains("BADEM") -> 0
                        u.contains("FIST") -> 1
                        u.contains("ARPA") -> 2
                        u.contains("BUĞDAY") -> 3
                        else -> 4
                    }
                }.thenBy { it.name }
            )
            quotes.addAll(ordered.take(18))
            return quotes
        }

        private fun beautifyGtbName(raw: String): String {
            return raw
                .replace("ANTEP FIS.", "Antep Fıstığı ", ignoreCase = true)
                .replace("KIR.KABUK.", "Kırmızı Kabuklu", ignoreCase = true)
                .replace("BOZ.KAVLAK", "Boz Kavlak", ignoreCase = true)
                .replace("ÇITL.KAVL.", "Çıtlak Kavlak", ignoreCase = true)
                .replace("KAVL.II.KA", "Kavlak 2. Kalite", ignoreCase = true)
                .replace("K.KAB.II.K", "Kırmızı Kabuklu 2. Kalite", ignoreCase = true)
                .replace("BADEM İÇİ(TATLI)", "Tatlı Badem İçi", ignoreCase = true)
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        fun parseUkonHtml(html: String): List<MarketQuote> {
            val rowRegex = Regex("<tr[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL)
            val cellRegex = Regex("<t[dh][^>]*>(.*?)</t[dh]>", RegexOption.DOT_MATCHES_ALL)
            val quotes = mutableListOf<MarketQuote>()

            var danaAvg = ""
            var kuzuAvg = ""
            var danaMonthChg = ""
            var kuzuMonthChg = ""
            val regionalRows = mutableListOf<Triple<String, String, String>>()

            for (rowMatch in rowRegex.findAll(html)) {
                val cells = cellRegex.findAll(rowMatch.groupValues[1])
                    .map { stripTags(it.groupValues[1]) }
                    .toList()
                if (cells.size < 3) continue
                val label = cells[0]
                val dana = cells[1]
                val kuzu = cells[2]
                when {
                    label.contains("Ortalama", ignoreCase = true) -> {
                        danaAvg = dana
                        kuzuAvg = kuzu
                    }
                    label.contains("Geçen Aya Göre", ignoreCase = true) -> {
                        danaMonthChg = dana
                        kuzuMonthChg = kuzu
                    }
                    label.contains("Bölge", ignoreCase = true) && !label.equals("Bölge", ignoreCase = true) -> {
                        regionalRows.add(Triple(label, dana, kuzu))
                    }
                }
            }

            if (danaAvg.isNotEmpty()) {
                quotes.add(
                    MarketQuote(
                        id = "ukon_dana_avg",
                        name = "Dana Bıçak Karkas (Türkiye Ortalaması)",
                        category = MarketCategory.LIVESTOCK,
                        primaryPrice = "$danaAvg TL",
                        secondaryPrice = "Aylık değişim: %$danaMonthChg",
                        unit = "TL / kg (Yağsız Karkas)",
                        changeText = if (danaMonthChg.isNotEmpty()) "%$danaMonthChg (Aylık)" else "",
                        isPositive = danaMonthChg.isNotEmpty() && !danaMonthChg.startsWith("-"),
                        source = "UKON (Ulusal Kırmızı Et Konseyi)",
                        detailNote = "Haftalık kesimhane kombina ortalaması"
                    )
                )
            }
            if (kuzuAvg.isNotEmpty()) {
                quotes.add(
                    MarketQuote(
                        id = "ukon_kuzu_avg",
                        name = "Kuzu Bıçak Karkas (Türkiye Ortalaması)",
                        category = MarketCategory.LIVESTOCK,
                        primaryPrice = "$kuzuAvg TL",
                        secondaryPrice = "Aylık değişim: %$kuzuMonthChg",
                        unit = "TL / kg (Yağsız Karkas)",
                        changeText = if (kuzuMonthChg.isNotEmpty()) "%$kuzuMonthChg (Aylık)" else "",
                        isPositive = kuzuMonthChg.isNotEmpty() && !kuzuMonthChg.startsWith("-"),
                        source = "UKON (Ulusal Kırmızı Et Konseyi)",
                        detailNote = "Haftalık kesimhane kombina ortalaması"
                    )
                )
            }
            regionalRows.forEach { (region, dana, kuzu) ->
                quotes.add(
                    MarketQuote(
                        id = "ukon_${region.hashCode()}",
                        name = "$region Et Kesim",
                        category = MarketCategory.LIVESTOCK,
                        primaryPrice = "Dana: $dana TL",
                        secondaryPrice = "Kuzu: $kuzu TL",
                        unit = "TL / kg",
                        source = "UKON Bölgesel Kesim"
                    )
                )
            }
            return quotes
        }

        fun parsePetrolOfisiHtml(html: String): List<FuelCityPrices> {
            val rowRegex = Regex(
                "<tr[^>]*class=\"price-row[^\"]*\"[^>]*data-disctrict-name=\"([^\"]+)\"[^>]*>(.*?)</tr>",
                RegexOption.DOT_MATCHES_ALL
            )
            val priceSpanRegex = Regex("<span[^>]*class=\"with-tax\"[^>]*>(.*?)</span>", RegexOption.DOT_MATCHES_ALL)
            val cities = mutableListOf<FuelCityPrices>()

            for (match in rowRegex.findAll(html)) {
                val cityName = decodeHtmlEntities(match.groupValues[1]).trim()
                val body = match.groupValues[2]
                val prices = priceSpanRegex.findAll(body)
                    .map { stripTags(it.groupValues[1]) }
                    .toList()
                // Column order verified against Petrol Ofisi <thead>:
                // [0] Kurşunsuz 95, [1] V/Max Diesel, [2] Gazyağı, [3] Kalorifer, [4] Fuel Oil, [5] PO/gaz Otogaz
                if (prices.size >= 2) {
                    cities.add(
                        FuelCityPrices(
                            cityName = cityName,
                            gasoline95 = prices.getOrNull(0).orEmpty(),
                            diesel = prices.getOrNull(1).orEmpty(),
                            lpg = prices.getOrNull(5).orEmpty(),
                            kerosene = prices.getOrNull(2).orEmpty()
                        )
                    )
                }
            }
            return cities
        }

        fun parseRssXml(
            xml: String,
            defaultSource: String,
            forceFertilizer: Boolean = false
        ): List<NewsArticle> {
            val itemRegex = Regex("<item>(.*?)</item>", RegexOption.DOT_MATCHES_ALL)
            val articles = mutableListOf<NewsArticle>()

            for ((idx, itemMatch) in itemRegex.findAll(xml).withIndex()) {
                val block = itemMatch.groupValues[1]
                val rawTitle = extractXmlTag(block, "title")
                val rawDesc = extractXmlTag(block, "description")
                val rawCat = extractXmlTag(block, "category")
                val rawSource = extractXmlTag(block, "source")
                val rawDate = extractXmlTag(block, "pubDate")

                val cleanTitle = cleanNewsText(rawTitle)
                if (cleanTitle.length < 10) continue

                var cleanSummary = cleanNewsText(rawDesc)
                if (cleanSummary.isEmpty() || cleanSummary.equals(cleanTitle, ignoreCase = true)) {
                    cleanSummary = cleanTitle
                }

                val isFert = forceFertilizer || containsFertilizerKeyword("$cleanTitle $cleanSummary $rawCat")
                val sourceLabel = cleanNewsText(rawSource).ifEmpty { defaultSource }
                val categoryLabel = cleanNewsText(rawCat).ifEmpty {
                    if (isFert) "Gübre & Girdi Piyasası" else "Tarım & Hayvancılık"
                }

                articles.add(
                    NewsArticle(
                        id = "news_${defaultSource.hashCode()}_${idx}_${cleanTitle.hashCode()}",
                        title = cleanTitle,
                        summary = cleanSummary,
                        category = categoryLabel,
                        source = sourceLabel,
                        publishedAt = formatRssDate(rawDate),
                        isFertilizerTopic = isFert
                    )
                )
            }
            return articles
        }

        private fun extractXmlTag(block: String, tag: String): String {
            val match = Regex("<$tag(?:\\s[^>]*)?>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL).find(block)
                ?: return ""
            var inner = match.groupValues[1].trim()
            if (inner.startsWith("<![CDATA[") && inner.endsWith("]]>")) {
                inner = inner.removePrefix("<![CDATA[").removeSuffix("]]>")
            }
            return inner
        }

        private fun cleanNewsText(raw: String): String {
            return stripTags(decodeHtmlEntities(raw))
                .replace(Regex("\\s*-\\s*[A-Za-zÇĞİÖŞÜçğıöşü0-9. ]+$"), "") // keep clean headline when trailing source duplicates
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun formatRssDate(rawDate: String): String {
            if (rawDate.isBlank()) return "Güncel"
            val patterns = listOf(
                "EEE, dd MMM yyyy HH:mm:ss Z",
                "EEE, dd MMM yyyy HH:mm:ss z"
            )
            for (p in patterns) {
                runCatching {
                    val parser = SimpleDateFormat(p, Locale.ENGLISH)
                    val parsed = parser.parse(rawDate.trim())
                    if (parsed != null) {
                        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(parsed)
                    }
                }
            }
            return rawDate.take(22)
        }

        private fun containsFertilizerKeyword(text: String): Boolean {
            val norm = AgriculturalSearchEngine.normalizeTurkish(text)
            return norm.contains("gubre") ||
                norm.contains("ure ") ||
                norm.contains("dap") ||
                norm.contains("amonyum") ||
                norm.contains("gubretas") ||
                norm.contains("20.20") ||
                norm.contains("mazot")
        }

        private fun stripTags(input: String): String {
            return decodeHtmlEntities(input)
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun decodeHtmlEntities(input: String): String {
            return input
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
                .replace("&#039;", "'")
                .replace("&#8217;", "’")
                .replace("&#8216;", "‘")
                .replace("&#8220;", "“")
                .replace("&#8221;", "”")
                .replace("&#8211;", "–")
                .replace("&#8212;", "—")
        }

        private fun deduplicateQuotes(quotes: List<MarketQuote>): List<MarketQuote> {
            val seen = mutableSetOf<String>()
            return quotes.filter { q ->
                val key = "${q.source}_${AgriculturalSearchEngine.normalizeTurkish(q.name)}"
                seen.add(key)
            }
        }

        fun matchFuelCity(locationName: String, cities: List<FuelCityPrices>): FuelCityPrices? {
            if (cities.isEmpty()) return null
            val normLoc = AgriculturalSearchEngine.normalizeTurkish(locationName)
            val direct = cities.firstOrNull { city ->
                val normCity = AgriculturalSearchEngine.normalizeTurkish(city.cityName)
                normLoc.contains(normCity.substringBefore("(").trim()) ||
                    normCity.contains(normLoc.substringBefore(" ").trim())
            }
            return direct ?: cities.firstOrNull {
                AgriculturalSearchEngine.normalizeTurkish(it.cityName).contains("adiyaman")
            } ?: cities.first()
        }

        fun buildSpokenMarketBriefing(
            snapshot: MarketDashboardSnapshot,
            selectedFuel: FuelCityPrices?
        ): String {
            return buildString {
                append("Güncel tarım piyasası ve haber özeti. Güncelleme zamanı: ${snapshot.fetchedAtLabel}. ")

                val topFx = snapshot.fxGoldQuotes.filter {
                    it.id.contains("USD") || it.id.contains("EUR") || it.id.contains("gram-altin") || it.id.contains("ceyrek-altin")
                }
                if (topFx.isNotEmpty()) {
                    append("Döviz ve altın piyasası: ")
                    topFx.forEach { append("${it.name} ${it.primaryPrice}. ") }
                }

                if (selectedFuel != null) {
                    append("${selectedFuel.cityName} akaryakıt fiyatları: ")
                    append("Motorin ${selectedFuel.diesel} lira bölü litre. ")
                    append("Benzin ${selectedFuel.gasoline95} lira bölü litre. ")
                    if (selectedFuel.lpg.isNotEmpty()) {
                        append("Otogaz ${selectedFuel.lpg} lira bölü litre. ")
                    }
                }

                val keyCommodities = snapshot.commodityQuotes.take(6)
                if (keyCommodities.isNotEmpty()) {
                    append("Tarım ürünleri borsa fiyatları: ")
                    keyCommodities.forEach {
                        append("${it.name}, ${it.primaryPrice} ${it.unit}. ")
                    }
                }

                val keyLivestock = snapshot.livestockQuotes.take(2)
                if (keyLivestock.isNotEmpty()) {
                    append("Kırmızı et kesim fiyatları: ")
                    keyLivestock.forEach {
                        append("${it.name}, ${it.primaryPrice} ${it.unit}. ")
                    }
                }

                if (snapshot.fertilizerBulletins.isNotEmpty()) {
                    append("Gübre piyasası gelişmeleri: ")
                    snapshot.fertilizerBulletins.take(2).forEach {
                        append("${it.title}. ")
                    }
                }

                if (snapshot.newsArticles.isNotEmpty()) {
                    append("Öne çıkan tarım ve hayvancılık haberleri: ")
                    snapshot.newsArticles.take(3).forEach {
                        append("${it.title}. ")
                    }
                }
            }
        }

        fun serializeSnapshot(snapshot: MarketDashboardSnapshot): String {
            fun quoteToJson(q: MarketQuote) = JSONObject().apply {
                put("id", q.id)
                put("name", q.name)
                put("category", q.category.name)
                put("primaryPrice", q.primaryPrice)
                put("secondaryPrice", q.secondaryPrice)
                put("unit", q.unit)
                put("changeText", q.changeText)
                if (q.isPositive != null) put("isPositive", q.isPositive)
                put("source", q.source)
                put("detailNote", q.detailNote)
            }

            fun newsToJson(n: NewsArticle) = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("summary", n.summary)
                put("category", n.category)
                put("source", n.source)
                put("publishedAt", n.publishedAt)
                put("isFertilizerTopic", n.isFertilizerTopic)
            }

            return JSONObject().apply {
                put("fetchedAtLabel", snapshot.fetchedAtLabel)
                put("sourceSummary", snapshot.sourceSummary)
                put("fxGoldQuotes", JSONArray().apply { snapshot.fxGoldQuotes.forEach { put(quoteToJson(it)) } })
                put("commodityQuotes", JSONArray().apply { snapshot.commodityQuotes.forEach { put(quoteToJson(it)) } })
                put("livestockQuotes", JSONArray().apply { snapshot.livestockQuotes.forEach { put(quoteToJson(it)) } })
                put(
                    "fuelCities",
                    JSONArray().apply {
                        snapshot.fuelCities.forEach { f ->
                            put(
                                JSONObject().apply {
                                    put("cityName", f.cityName)
                                    put("gasoline95", f.gasoline95)
                                    put("diesel", f.diesel)
                                    put("lpg", f.lpg)
                                    put("kerosene", f.kerosene)
                                }
                            )
                        }
                    }
                )
                put("fertilizerBulletins", JSONArray().apply { snapshot.fertilizerBulletins.forEach { put(newsToJson(it)) } })
                put("newsArticles", JSONArray().apply { snapshot.newsArticles.forEach { put(newsToJson(it)) } })
            }.toString()
        }

        fun deserializeSnapshot(raw: String): MarketDashboardSnapshot {
            val root = JSONObject(raw)

            fun parseQuotes(arr: JSONArray?): List<MarketQuote> {
                if (arr == null) return emptyList()
                return (0 until arr.length()).map { idx ->
                    val o = arr.getJSONObject(idx)
                    MarketQuote(
                        id = o.optString("id"),
                        name = o.optString("name"),
                        category = runCatching { MarketCategory.valueOf(o.optString("category")) }
                            .getOrDefault(MarketCategory.COMMODITY),
                        primaryPrice = o.optString("primaryPrice"),
                        secondaryPrice = o.optString("secondaryPrice"),
                        unit = o.optString("unit"),
                        changeText = o.optString("changeText"),
                        isPositive = if (o.has("isPositive")) o.getBoolean("isPositive") else null,
                        source = o.optString("source"),
                        detailNote = o.optString("detailNote")
                    )
                }
            }

            fun parseNews(arr: JSONArray?): List<NewsArticle> {
                if (arr == null) return emptyList()
                return (0 until arr.length()).map { idx ->
                    val o = arr.getJSONObject(idx)
                    NewsArticle(
                        id = o.optString("id"),
                        title = o.optString("title"),
                        summary = o.optString("summary"),
                        category = o.optString("category"),
                        source = o.optString("source"),
                        publishedAt = o.optString("publishedAt"),
                        isFertilizerTopic = o.optBoolean("isFertilizerTopic", false)
                    )
                }
            }

            val fuelArr = root.optJSONArray("fuelCities")
            val fuelList = if (fuelArr == null) emptyList() else (0 until fuelArr.length()).map { idx ->
                val o = fuelArr.getJSONObject(idx)
                FuelCityPrices(
                    cityName = o.optString("cityName"),
                    gasoline95 = o.optString("gasoline95"),
                    diesel = o.optString("diesel"),
                    lpg = o.optString("lpg"),
                    kerosene = o.optString("kerosene")
                )
            }

            return MarketDashboardSnapshot(
                fxGoldQuotes = parseQuotes(root.optJSONArray("fxGoldQuotes")),
                commodityQuotes = parseQuotes(root.optJSONArray("commodityQuotes")),
                livestockQuotes = parseQuotes(root.optJSONArray("livestockQuotes")),
                fuelCities = fuelList,
                fertilizerBulletins = parseNews(root.optJSONArray("fertilizerBulletins")),
                newsArticles = parseNews(root.optJSONArray("newsArticles")),
                fetchedAtLabel = root.optString("fetchedAtLabel"),
                sourceSummary = root.optString("sourceSummary"),
                fromCache = true
            )
        }
    }
}
