package com.tarimmeteoroloji.ai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarimmeteoroloji.ai.data.AgriculturalSearchEngine
import com.tarimmeteoroloji.ai.data.MarketAndNewsRepository
import com.tarimmeteoroloji.ai.model.FuelCityPrices
import com.tarimmeteoroloji.ai.model.MarketCategory
import com.tarimmeteoroloji.ai.model.MarketDashboardSnapshot
import com.tarimmeteoroloji.ai.model.MarketQuote
import com.tarimmeteoroloji.ai.model.NewsArticle
import com.tarimmeteoroloji.ai.ui.theme.AppColors

@Composable
fun MarketNewsTab(
    snapshot: MarketDashboardSnapshot?,
    isLoading: Boolean,
    errorMessage: String?,
    selectedFuelCityName: String?,
    activeSpeechId: String?,
    onRefresh: () -> Unit,
    onOpenFuelCityPicker: () -> Unit,
    onOpenNewsArticle: (NewsArticle) -> Unit,
    onToggleSpeak: (speechId: String, text: String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(MarketCategory.ALL) }
    var filterQuery by remember { mutableStateOf("") }

    val selectedFuelCity = remember(snapshot, selectedFuelCityName) {
        val cities = snapshot?.fuelCities.orEmpty()
        if (cities.isEmpty()) null
        else if (!selectedFuelCityName.isNullOrBlank()) {
            cities.firstOrNull { it.cityName.equals(selectedFuelCityName, ignoreCase = true) }
                ?: MarketAndNewsRepository.matchFuelCity(selectedFuelCityName, cities)
        } else {
            MarketAndNewsRepository.matchFuelCity("Adıyaman", cities)
        }
    }

    val normalizedQuery = remember(filterQuery) {
        AgriculturalSearchEngine.normalizeTurkish(filterQuery)
    }

    val filteredCommodities = remember(snapshot, normalizedQuery) {
        val list = snapshot?.commodityQuotes.orEmpty()
        if (normalizedQuery.isEmpty()) list
        else list.filter {
            AgriculturalSearchEngine.normalizeTurkish("${it.name} ${it.source} ${it.detailNote}")
                .contains(normalizedQuery)
        }
    }

    val filteredLivestock = remember(snapshot, normalizedQuery) {
        val list = snapshot?.livestockQuotes.orEmpty()
        if (normalizedQuery.isEmpty()) list
        else list.filter {
            AgriculturalSearchEngine.normalizeTurkish("${it.name} ${it.source}")
                .contains(normalizedQuery)
        }
    }

    val filteredFxGold = remember(snapshot, normalizedQuery) {
        val list = snapshot?.fxGoldQuotes.orEmpty()
        if (normalizedQuery.isEmpty()) list
        else list.filter {
            AgriculturalSearchEngine.normalizeTurkish("${it.name} ${it.source}")
                .contains(normalizedQuery)
        }
    }

    val filteredFertilizer = remember(snapshot, normalizedQuery) {
        val list = snapshot?.fertilizerBulletins.orEmpty()
        if (normalizedQuery.isEmpty()) list
        else list.filter {
            AgriculturalSearchEngine.normalizeTurkish("${it.title} ${it.summary} ${it.source}")
                .contains(normalizedQuery)
        }
    }

    val filteredNews = remember(snapshot, normalizedQuery) {
        val list = snapshot?.newsArticles.orEmpty()
        if (normalizedQuery.isEmpty()) list
        else list.filter {
            AgriculturalSearchEngine.normalizeTurkish("${it.title} ${it.summary} ${it.category} ${it.source}")
                .contains(normalizedQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header & Voice Briefing Bar
        MarketCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📈 CANLI TARIM PİYASASI & HABERLER",
                        color = AppColors.Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            snapshot == null && isLoading -> "Canlı borsa, döviz, akaryakıt ve haber verileri çekiliyor..."
                            snapshot != null && snapshot.fromCache ->
                                "⚠️ Çevrimdışı önbellek (${snapshot.fetchedAtLabel})"
                            snapshot != null ->
                                "Son güncelleme: ${snapshot.fetchedAtLabel} • ${snapshot.sourceSummary}"
                            else -> "Piyasa verisi yükleniyor..."
                        },
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    color = AppColors.Primary,
                    trackColor = AppColors.PrimaryLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ $errorMessage",
                    color = AppColors.WarnText,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.WarnBg)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text(
                        text = if (isLoading) "Yenileniyor..." else "🔄 Piyasayı Yenile",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (snapshot != null) {
                    val isBriefingSpeaking = activeSpeechId == "market_briefing"
                    OutlinedButton(
                        onClick = {
                            val briefing = MarketAndNewsRepository.buildSpokenMarketBriefing(
                                snapshot,
                                selectedFuelCity
                            )
                            onToggleSpeak("market_briefing", briefing)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isBriefingSpeaking) AppColors.DangerBorder else AppColors.PrimaryBorder
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isBriefingSpeaking) AppColors.DangerBg else AppColors.PrimaryLight,
                            contentColor = if (isBriefingSpeaking) AppColors.DangerText else AppColors.Primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(
                            text = if (isBriefingSpeaking) "⏹️ Okumayı Durdur" else "🔊 Özeti Dinle",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                singleLine = true,
                placeholder = {
                    Text("Arpa, buğday, fıstık, badem, zeytin, gübre, dolar ara...", fontSize = 12.sp)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MarketCategory.entries.forEach { cat ->
                    val selected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) AppColors.Primary else AppColors.DayCardBg)
                            .border(
                                1.dp,
                                if (selected) AppColors.Primary else AppColors.Divider,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 11.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "${cat.emoji} ${cat.label}",
                            color = if (selected) Color.White else AppColors.TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (snapshot != null) {
            val showAll = selectedCategory == MarketCategory.ALL

            // 2. Döviz & Altın
            if ((showAll || selectedCategory == MarketCategory.FX_GOLD) && filteredFxGold.isNotEmpty()) {
                val fxSpeechText = remember(filteredFxGold) {
                    "Güncel döviz ve altın fiyatları. " + filteredFxGold.joinToString(". ") {
                        "${it.name} satış ${it.primaryPrice}, ${it.secondaryPrice}"
                    }
                }
                MarketCard {
                    SectionHeaderWithSpeak(
                        title = "💰 DÖVİZ & ALTIN PİYASASI",
                        speechId = "section_fx_gold",
                        activeSpeechId = activeSpeechId,
                        onSpeakClick = { onToggleSpeak("section_fx_gold", fxSpeechText) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in filteredFxGold.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val first = filteredFxGold[i]
                                FxGoldTile(quote = first, modifier = Modifier.weight(1f))
                                if (i + 1 < filteredFxGold.size) {
                                    FxGoldTile(quote = filteredFxGold[i + 1], modifier = Modifier.weight(1f))
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 3. Akaryakıt Fiyatları
            if ((showAll || selectedCategory == MarketCategory.FUEL) && selectedFuelCity != null) {
                val fuelSpeech = remember(selectedFuelCity) {
                    "${selectedFuelCity.cityName} güncel akaryakıt fiyatları. " +
                        "Kurşunsuz 95 Benzin ${selectedFuelCity.gasoline95} lira bölü litre. " +
                        "Motorin ${selectedFuelCity.diesel} lira bölü litre. " +
                        if (selectedFuelCity.lpg.isNotEmpty()) "Otogaz LPG ${selectedFuelCity.lpg} lira bölü litre." else ""
                }
                MarketCard {
                    SectionHeaderWithSpeak(
                        title = "⛽ AKARYAKIT FİYATLARI",
                        speechId = "section_fuel",
                        activeSpeechId = activeSpeechId,
                        onSpeakClick = { onToggleSpeak("section_fuel", fuelSpeech) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 ${selectedFuelCity.cityName}",
                            color = AppColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onOpenFuelCityPicker,
                            border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = AppColors.PrimaryLight,
                                contentColor = AppColors.Primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("İl Değiştir (${snapshot.fuelCities.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FuelBox(
                            label = "Motorin",
                            price = "${selectedFuelCity.diesel} TL/LT",
                            accent = AppColors.Primary,
                            modifier = Modifier.weight(1f)
                        )
                        FuelBox(
                            label = "Benzin (95)",
                            price = "${selectedFuelCity.gasoline95} TL/LT",
                            accent = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f)
                        )
                        FuelBox(
                            label = "Otogaz LPG",
                            price = if (selectedFuelCity.lpg.isNotEmpty()) "${selectedFuelCity.lpg} TL/LT" else "-",
                            accent = AppColors.WarnText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kaynak: Petrol Ofisi Güncel Pompa Tarifesi (KDV Dahil)",
                        color = AppColors.TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // 4. Tarım Ürünleri & Borsa (Buğday, Arpa, Zeytin/Zeytinyağı, Antep Fıstığı, Badem, Mısır)
            if ((showAll || selectedCategory == MarketCategory.COMMODITY) && filteredCommodities.isNotEmpty()) {
                val commoditySpeech = remember(filteredCommodities) {
                    "Tarım ürünleri borsa fiyatları. " + filteredCommodities.take(10).joinToString(". ") {
                        "${it.name}: ${it.primaryPrice} ${it.unit}"
                    }
                }
                MarketCard {
                    SectionHeaderWithSpeak(
                        title = "🌾 ÜRÜN & TİCARET BORSASI FİYATLARI",
                        speechId = "section_commodities",
                        activeSpeechId = activeSpeechId,
                        onSpeakClick = { onToggleSpeak("section_commodities", commoditySpeech) }
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Buğday, Arpa, Antep Fıstığı, Badem, Zeytinyağı, Mısır ve Mercimek canlı borsa tescil fiyatları:",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredCommodities.forEach { quote ->
                            QuoteRowCard(quote)
                        }
                    }
                }
            }

            // 5. Hayvancılık & Kırmızı Et Kesim Fiyatları
            if ((showAll || selectedCategory == MarketCategory.LIVESTOCK) && filteredLivestock.isNotEmpty()) {
                val livestockSpeech = remember(filteredLivestock) {
                    "Hayvancılık ve kırmızı et kesim fiyatları. " + filteredLivestock.take(6).joinToString(". ") {
                        "${it.name}: ${it.primaryPrice} ${it.unit}"
                    }
                }
                MarketCard {
                    SectionHeaderWithSpeak(
                        title = "🐄 HAYVANCILIK & KIRMIZI ET FİYATLARI",
                        speechId = "section_livestock",
                        activeSpeechId = activeSpeechId,
                        onSpeakClick = { onToggleSpeak("section_livestock", livestockSpeech) }
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "UKON haftalık yağsız dana ve kuzu bıçak karkas kesim ortalamaları ve canlı referanslar:",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredLivestock.forEach { quote ->
                            QuoteRowCard(quote)
                        }
                    }
                }
            }

            // 6. Gübre Fiyatları & Girdi Piyasası Bültenleri
            if ((showAll || selectedCategory == MarketCategory.FERTILIZER) && filteredFertilizer.isNotEmpty()) {
                val fertSpeech = remember(filteredFertilizer) {
                    "Gübre fiyatları ve girdi piyasası gelişmeleri. " + filteredFertilizer.take(5).joinToString(". ") {
                        "${it.title}. ${it.summary}"
                    }
                }
                MarketCard {
                    SectionHeaderWithSpeak(
                        title = "🧪 GÜBRE FİYATLARI & GİRDİ BÜLTENİ",
                        speechId = "section_fertilizer",
                        activeSpeechId = activeSpeechId,
                        onSpeakClick = { onToggleSpeak("section_fertilizer", fertSpeech) }
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Üre, DAP, 20.20.0 kompoze ve tarımsal girdi fiyat haberleri (uygulama içi okuma):",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredFertilizer.forEach { article ->
                            NewsListRow(
                                article = article,
                                activeSpeechId = activeSpeechId,
                                onOpen = { onOpenNewsArticle(article) },
                                onToggleSpeak = {
                                    onToggleSpeak(
                                        article.id,
                                        "${article.title}. ${article.summary}. Kaynak: ${article.source}."
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 7. Tarım & Hayvancılık Haberleri
            if ((showAll || selectedCategory == MarketCategory.NEWS) && filteredNews.isNotEmpty()) {
                val newsSpeech = remember(filteredNews) {
                    "Güncel tarım ve hayvancılık haberleri. " + filteredNews.take(5).joinToString(". ") {
                        "${it.title}. ${it.summary}"
                    }
                }
                MarketCard {
                    SectionHeaderWithSpeak(
                        title = "📰 TARIM & HAYVANCILIK HABERLERİ",
                        speechId = "section_news",
                        activeSpeechId = activeSpeechId,
                        onSpeakClick = { onToggleSpeak("section_news", newsSpeech) }
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Detayını uygulama içinde okumak veya sesli dinlemek için habere dokunun:",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredNews.forEach { article ->
                            NewsListRow(
                                article = article,
                                activeSpeechId = activeSpeechId,
                                onOpen = { onOpenNewsArticle(article) },
                                onToggleSpeak = {
                                    onToggleSpeak(
                                        article.id,
                                        "${article.title}. ${article.summary}. Kaynak: ${article.source}."
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MarketCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.CardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            content = content
        )
    }
}

@Composable
private fun SectionHeaderWithSpeak(
    title: String,
    speechId: String,
    activeSpeechId: String?,
    onSpeakClick: () -> Unit
) {
    val isSpeaking = activeSpeechId == speechId
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AppColors.Primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isSpeaking) "⏹️ Durdur" else "🔊 Dinle",
            color = if (isSpeaking) AppColors.DangerText else AppColors.Primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSpeaking) AppColors.DangerBg else AppColors.PrimaryLight)
                .border(
                    1.dp,
                    if (isSpeaking) AppColors.DangerBorder else AppColors.PrimaryBorder,
                    RoundedCornerShape(8.dp)
                )
                .clickable { onSpeakClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FuelBox(
    label: String,
    price: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DayCardBg)
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = AppColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = price,
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuoteRowCard(quote: MarketQuote) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DayCardBg)
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
            .padding(11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.name,
                    color = AppColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = quote.source,
                    color = AppColors.TextSecondary,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = quote.primaryPrice,
                    color = AppColors.Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = quote.unit,
                    color = AppColors.TextMeta,
                    fontSize = 10.sp
                )
            }
        }
        if (quote.secondaryPrice.isNotEmpty() || quote.changeText.isNotEmpty() || quote.detailNote.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = AppColors.Divider)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quote.detailNote.ifEmpty { quote.secondaryPrice },
                    color = AppColors.TextDetail,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                if (quote.changeText.isNotEmpty()) {
                    val chgColor = when (quote.isPositive) {
                        true -> AppColors.Primary
                        false -> AppColors.DangerText
                        null -> AppColors.TextSecondary
                    }
                    Text(
                        text = quote.changeText,
                        color = chgColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsListRow(
    article: NewsArticle,
    activeSpeechId: String?,
    onOpen: () -> Unit,
    onToggleSpeak: () -> Unit
) {
    val isSpeaking = activeSpeechId == article.id
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DayCardBg)
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
            .clickable { onOpen() }
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${article.category} • ${article.publishedAt}",
                color = AppColors.Primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isSpeaking) "⏹️ Durdur" else "🔊 Oku",
                color = if (isSpeaking) AppColors.DangerText else AppColors.Primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSpeaking) AppColors.DangerBg else AppColors.PrimaryLight)
                    .clickable { onToggleSpeak() }
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
        Text(
            text = article.title,
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp
        )
        Text(
            text = article.summary,
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kaynak: ${article.source}",
                color = AppColors.TextMeta,
                fontSize = 10.sp
            )
            Text(
                text = "Haberi Aç →",
                color = AppColors.Primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FxGoldTile(quote: MarketQuote, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DayCardBg)
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            text = quote.name,
            color = AppColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = quote.primaryPrice,
            color = AppColors.Primary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        if (quote.secondaryPrice.isNotEmpty()) {
            Text(
                text = quote.secondaryPrice,
                color = AppColors.TextMeta,
                fontSize = 10.sp
            )
        }
        if (quote.changeText.isNotEmpty()) {
            val chgColor = when (quote.isPositive) {
                true -> AppColors.Primary
                false -> AppColors.DangerText
                null -> AppColors.TextSecondary
            }
            Text(
                text = "Değişim: ${quote.changeText}",
                color = chgColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
