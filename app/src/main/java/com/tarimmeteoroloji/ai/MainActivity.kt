package com.tarimmeteoroloji.ai

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tarimmeteoroloji.ai.data.AgriculturalSearchEngine
import com.tarimmeteoroloji.ai.data.AgronomyEngine
import com.tarimmeteoroloji.ai.data.CropCatalog
import com.tarimmeteoroloji.ai.data.MarketAndNewsRepository
import com.tarimmeteoroloji.ai.data.PlantDiagnosisEngine
import com.tarimmeteoroloji.ai.data.VoiceRecognitionController
import com.tarimmeteoroloji.ai.data.VoiceSearchCallbacks
import com.tarimmeteoroloji.ai.data.WeatherRepository
import com.tarimmeteoroloji.ai.model.CropInfo
import com.tarimmeteoroloji.ai.model.GuideTopicId
import com.tarimmeteoroloji.ai.model.MarketDashboardSnapshot
import com.tarimmeteoroloji.ai.model.NewsArticle
import com.tarimmeteoroloji.ai.model.PlantDiagnosisResult
import com.tarimmeteoroloji.ai.model.SprayStatusLevel
import com.tarimmeteoroloji.ai.model.WeatherSnapshot
import com.tarimmeteoroloji.ai.ui.components.AgronomyGuideDialog
import com.tarimmeteoroloji.ai.ui.components.CityPickerDialog
import com.tarimmeteoroloji.ai.ui.components.CropDetailDialog
import com.tarimmeteoroloji.ai.ui.components.CropHeroIllustration
import com.tarimmeteoroloji.ai.ui.components.FuelCityPickerDialog
import com.tarimmeteoroloji.ai.ui.components.GeocodeSearchDialog
import com.tarimmeteoroloji.ai.ui.components.MarketNewsTab
import com.tarimmeteoroloji.ai.ui.components.NewsArticleDialog
import com.tarimmeteoroloji.ai.ui.components.PlantDiagnosisDialog
import com.tarimmeteoroloji.ai.ui.components.VoiceSearchDialog
import com.tarimmeteoroloji.ai.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TarimMeteorolojiApp()
        }
    }
}

private enum class AppTab(val emoji: String, val label: String) {
    HOME("🌤️", "Ana Ekran"),
    MARKETS("📈", "Piyasa"),
    CROPS("🌱", "Ürünler"),
    SETTINGS("⚙️", "Ayarlar")
}

private enum class PendingCameraMode {
    NONE,
    PHOTO,
    VIDEO
}

@Composable
fun TarimMeteorolojiApp() {
    val context = LocalContext.current
    val repo = remember { WeatherRepository(context.applicationContext) }
    val marketRepo = remember { MarketAndNewsRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    val savedInit = remember { repo.loadSavedLocation() }

    var userFontScale by remember { mutableFloatStateOf(repo.loadFontScale()) }
    var activeTab by remember { mutableStateOf(AppTab.HOME) }

    var lat by remember { mutableStateOf(savedInit.lat) }
    var lon by remember { mutableStateOf(savedInit.lon) }
    var locationName by remember { mutableStateOf(savedInit.locationName) }
    var weatherSnapshot by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var updatedText by remember { mutableStateOf("Room önbelleği kontrol ediliyor...") }
    var roomCacheSummary by remember { mutableStateOf("Room Veritabanı: Kontrol ediliyor...") }
    var isLoading by remember { mutableStateOf(false) }

    var showCityPicker by remember { mutableStateOf(false) }
    var showGeocodeSearch by remember { mutableStateOf(false) }
    var selectedCrop by remember { mutableStateOf<CropInfo?>(null) }
    var activeGuideTopic by remember { mutableStateOf<GuideTopicId?>(null) }
    var bkuFilterText by remember { mutableStateOf("") }
    var selectedForecastDayIndex by remember { mutableIntStateOf(0) }

    // Market & Agricultural News State
    var marketSnapshot by remember { mutableStateOf<MarketDashboardSnapshot?>(null) }
    var isMarketLoading by remember { mutableStateOf(false) }
    var marketError by remember { mutableStateOf<String?>(null) }
    var selectedFuelCityName by remember { mutableStateOf(repo.loadSelectedFuelCity()) }
    var showFuelCityPicker by remember { mutableStateOf(false) }
    var selectedNewsArticle by remember { mutableStateOf<NewsArticle?>(null) }

    // Photo / Video Plant Disease Diagnosis State
    var showDiagnosisDialog by remember { mutableStateOf(false) }
    var diagnosisCropId by remember { mutableStateOf<String?>(null) }
    var isAnalyzingMedia by remember { mutableStateOf(false) }
    var diagnosisError by remember { mutableStateOf<String?>(null) }
    var diagnosisResult by remember { mutableStateOf<PlantDiagnosisResult?>(null) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraMode by remember { mutableStateOf(PendingCameraMode.NONE) }

    // Hands-Free Voice Search & Global TTS Voice Readout State
    val voiceController = remember { VoiceRecognitionController(context.applicationContext) }
    var activeSpeechId by remember { mutableStateOf<String?>(null) }
    var speechRate by remember { mutableFloatStateOf(repo.loadSpeechRate()) }
    DisposableEffect(voiceController) {
        voiceController.setSpeechRate(speechRate)
        voiceController.setOnSpeakingStateChangeListener { speaking, sid ->
            activeSpeechId = if (speaking) sid else null
        }
        onDispose { voiceController.shutdown() }
    }
    fun toggleSpeak(speechId: String, text: String) {
        voiceController.toggleSpeak(speechId, text)
    }

    var showVoiceSearchDialog by remember { mutableStateOf(false) }
    var voiceSearchQuery by remember { mutableStateOf("") }
    var isVoiceListening by remember { mutableStateOf(false) }
    var voicePartialTranscript by remember { mutableStateOf("") }
    var voiceStatusText by remember { mutableStateOf("Mikrofona dokunun ve tarımsal sorunuzu söyleyin.") }
    var voiceRmsLevel by remember { mutableFloatStateOf(0f) }
    var autoSpeakVoiceResult by remember { mutableStateOf(repo.loadAutoSpeakVoiceSearch()) }
    val voiceResults = remember(voiceSearchQuery) {
        AgriculturalSearchEngine.search(voiceSearchQuery)
    }

    fun handleRecognizedVoiceQuery(spoken: String) {
        val cleaned = spoken.trim()
        voiceSearchQuery = cleaned
        voicePartialTranscript = ""
        voiceStatusText = "Algılanan: “$cleaned”"
        if (autoSpeakVoiceResult && cleaned.isNotEmpty()) {
            val top = AgriculturalSearchEngine.search(cleaned).firstOrNull()
            if (top != null) {
                voiceController.speak(top.speechText)
            } else {
                voiceController.speak("$cleaned için bilgi bankasında doğrudan eşleşme bulunamadı.")
            }
        }
    }

    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        isVoiceListening = false
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val matches = activityResult.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                .orEmpty()
            val best = matches.firstOrNull()?.trim().orEmpty()
            if (best.isNotEmpty()) {
                handleRecognizedVoiceQuery(best)
            }
        }
    }

    fun startInlineVoiceListening() {
        voiceController.startListening(object : VoiceSearchCallbacks {
            override fun onReadyForSpeech() {
                isVoiceListening = true
                voicePartialTranscript = ""
                voiceStatusText = "Mikrofon açık, Türkçe konuşun..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                voiceRmsLevel = rmsdB
            }

            override fun onPartialText(text: String) {
                voicePartialTranscript = text
            }

            override fun onFinalText(text: String) {
                isVoiceListening = false
                voiceRmsLevel = 0f
                handleRecognizedVoiceQuery(text)
            }

            override fun onError(message: String, shouldOfferSystemIntentFallback: Boolean) {
                isVoiceListening = false
                voiceRmsLevel = 0f
                voiceStatusText = message
                if (shouldOfferSystemIntentFallback) {
                    try {
                        speechIntentLauncher.launch(voiceController.buildRecognizerIntent())
                    } catch (_: Exception) {
                        voiceStatusText = "$message Arama kutusuna yazarak veya örnek butonlarla arayabilirsiniz."
                    }
                }
            }
        })
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        showVoiceSearchDialog = true
        if (granted) {
            startInlineVoiceListening()
        } else {
            voiceStatusText = "Mikrofon izni verilmedi. Yazarak veya örnek başlıklara dokunarak arayabilirsiniz."
        }
    }

    fun launchHandsFreeVoiceSearch() {
        showVoiceSearchDialog = true
        val micOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (micOk) {
            startInlineVoiceListening()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun refreshRoomSummary() {
        scope.launch {
            roomCacheSummary = withContext(Dispatchers.IO) {
                repo.getRoomCacheSummary()
            }
        }
    }

    fun fetchMarkets(forceRefresh: Boolean = false) {
        if (isMarketLoading && !forceRefresh) return
        scope.launch {
            if (!repo.isNetworkAvailable()) {
                val cached = withContext(Dispatchers.IO) { marketRepo.loadCachedDashboard() }
                if (cached != null) {
                    marketSnapshot = cached
                    marketError = null
                } else {
                    marketError = "Çevrimdışı moddasınız; canlı piyasa verisi için internet bağlantısı gerekli."
                }
                return@launch
            }
            isMarketLoading = true
            marketError = null
            try {
                val live = withContext(Dispatchers.IO) { marketRepo.fetchLiveDashboard() }
                marketSnapshot = live
                if (selectedFuelCityName.isNullOrBlank() && live.fuelCities.isNotEmpty()) {
                    selectedFuelCityName = MarketAndNewsRepository.matchFuelCity(
                        locationName,
                        live.fuelCities
                    )?.cityName
                }
            } catch (_: Exception) {
                val cached = withContext(Dispatchers.IO) { marketRepo.loadCachedDashboard() }
                if (cached != null) {
                    marketSnapshot = cached
                    marketError = "Canlı kaynaklara geçici erişilemedi; son kaydedilen veriler gösteriliyor."
                } else {
                    marketError = "Piyasa ve haber kaynaklarına şu anda erişilemedi."
                }
            } finally {
                isMarketLoading = false
            }
        }
    }

    fun fetchWeatherFor(targetLat: Double, targetLon: Double, targetLocName: String = locationName) {
        scope.launch {
            if (!repo.isNetworkAvailable()) {
                val cached = withContext(Dispatchers.IO) {
                    repo.loadRoomCachedWeather(targetLat, targetLon)
                }
                if (cached != null) {
                    locationName = cached.locationName
                    weatherSnapshot = cached.snapshot
                    updatedText = "⚠️ Çevrimdışı Room önbelleği (7 gün geçerli • ${cached.cachedTime})"
                } else {
                    updatedText = "⚠️ Çevrimdışı mod (7 günlük geçerli kayıt bulunamadı)"
                }
                refreshRoomSummary()
                return@launch
            }

            isLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    repo.fetchAndCacheForecast(targetLat, targetLon, targetLocName)
                }
                weatherSnapshot = result.snapshot
                updatedText = "Son güncelleme: ${result.cachedTime} (7 gün Room önbellekte)"
                roomCacheSummary = withContext(Dispatchers.IO) { repo.getRoomCacheSummary() }
            } catch (_: Exception) {
                val fallback = withContext(Dispatchers.IO) {
                    repo.loadRoomCachedWeather(targetLat, targetLon)
                }
                if (fallback != null) {
                    locationName = fallback.locationName
                    weatherSnapshot = fallback.snapshot
                    updatedText = "⚠️ Sunucuya ulaşılamadı • Room önbelleği (${fallback.cachedTime})"
                } else {
                    updatedText = "⚠️ Sunucuya ulaşılamadı."
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun resolveGpsAndFetch() {
        var gotImmediateFix = false
        repo.resolveDeviceCoordinates { newLat, newLon ->
            gotImmediateFix = true
            lat = newLat
            lon = newLon
            val coordLabel = String.format(Locale.US, "%.4f, %.4f", newLat, newLon)
            locationName = coordLabel
            repo.saveLocation(newLat, newLon, coordLabel)
            fetchWeatherFor(newLat, newLon, coordLabel)

            scope.launch {
                val resolved = withContext(Dispatchers.IO) {
                    repo.reverseGeocode(newLat, newLon)
                }
                if (!resolved.isNullOrBlank()) {
                    locationName = resolved
                    repo.saveLocation(newLat, newLon, resolved)
                }
            }
        }
        if (!gotImmediateFix) {
            fetchWeatherFor(lat, lon, locationName)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            resolveGpsAndFetch()
        } else {
            fetchWeatherFor(lat, lon, locationName)
        }
    }

    fun requestLocationAndFetch() {
        val fineOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineOk && !coarseOk) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            resolveGpsAndFetch()
        }
    }

    LaunchedEffect(Unit) {
        val cached = withContext(Dispatchers.IO) {
            repo.loadRoomCachedWeather(lat, lon)
        }
        if (cached != null) {
            weatherSnapshot = cached.snapshot
            updatedText = "Room 7-günlük önbellek (${cached.cachedTime})"
        }
        val cachedMarket = withContext(Dispatchers.IO) {
            marketRepo.loadCachedDashboard()
        }
        if (cachedMarket != null) {
            marketSnapshot = cachedMarket
        }
        refreshRoomSummary()
        requestLocationAndFetch()
        fetchMarkets()
    }

    val sprayEval = remember(weatherSnapshot) {
        val snap = weatherSnapshot ?: return@remember null
        val cur = snap.current ?: return@remember null
        val today = snap.daily.firstOrNull()
        AgronomyEngine.evaluateSprayCondition(
            temp = cur.temperature,
            hum = cur.humidity,
            wind = cur.windSpeed,
            curRain = cur.precipitation,
            todayRain = today?.precipitationSum ?: 0.0,
            rainProb = today?.precipitationProbMax ?: 0
        )
    }

    val agronomicReport = remember(weatherSnapshot) {
        val snap = weatherSnapshot ?: return@remember null
        AgronomyEngine.buildAgronomicReport(snap.daily)
    }

    fun runPlantAnalysis(uri: Uri, isVideo: Boolean) {
        isAnalyzingMedia = true
        diagnosisError = null
        showDiagnosisDialog = true
        scope.launch {
            try {
                val res = withContext(Dispatchers.Default) {
                    PlantDiagnosisEngine.analyzeMedia(
                        context = context.applicationContext,
                        uri = uri,
                        isVideo = isVideo,
                        selectedCropId = diagnosisCropId,
                        weather = weatherSnapshot,
                        sprayEval = sprayEval
                    )
                }
                diagnosisResult = res
            } catch (_: Exception) {
                diagnosisError = "Görüntü veya video analiz edilemedi. Lütfen net ve aydınlık bir yaprak/meyve görüntüsü seçip tekrar deneyin."
                Toast.makeText(
                    context,
                    "Görüntü veya video analiz edilemedi.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isAnalyzingMedia = false
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingPhotoUri
        if (success && uri != null) {
            runPlantAnalysis(uri, isVideo = false)
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val uri = pendingVideoUri
        if (success && uri != null) {
            runPlantAnalysis(uri, isVideo = true)
        }
    }

    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri).orEmpty()
            val isVid = mime.startsWith("video/")
            runPlantAnalysis(uri, isVideo = isVid)
        }
    }

    fun createCacheMediaUri(fileName: String): Uri {
        val file = File(context.cacheDir, fileName)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun launchCameraPhoto() {
        val uri = createCacheMediaUri("bitki_foto_${System.currentTimeMillis()}.jpg")
        pendingPhotoUri = uri
        takePictureLauncher.launch(uri)
    }

    fun launchCameraVideo() {
        val uri = createCacheMediaUri("bitki_video_${System.currentTimeMillis()}.mp4")
        pendingVideoUri = uri
        captureVideoLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val mode = pendingCameraMode
        pendingCameraMode = PendingCameraMode.NONE
        if (granted) {
            when (mode) {
                PendingCameraMode.PHOTO -> launchCameraPhoto()
                PendingCameraMode.VIDEO -> launchCameraVideo()
                PendingCameraMode.NONE -> Unit
            }
        } else {
            Toast.makeText(
                context,
                "Kamera izni verilmedi. Galeriden fotoğraf veya video seçebilirsiniz.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun ensureCameraAndLaunch(mode: PendingCameraMode) {
        val camOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (camOk) {
            when (mode) {
                PendingCameraMode.PHOTO -> launchCameraPhoto()
                PendingCameraMode.VIDEO -> launchCameraVideo()
                PendingCameraMode.NONE -> Unit
            }
        } else {
            pendingCameraMode = mode
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity, userFontScale) {
        Density(density = baseDensity.density, fontScale = userFontScale)
    }

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        Scaffold(
            containerColor = AppColors.BgPage,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Primary)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌾 TARIM METEOROLOJİ AI",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (activeSpeechId != null) {
                            Text(
                                text = "⏹️ Durdur",
                                color = AppColors.DangerText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppColors.DangerBg)
                                    .clickable { voiceController.stopSpeaking() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "🎙️ Sesli Ara",
                            color = AppColors.Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.PrimaryBorder)
                                .clickable { launchHandsFreeVoiceSearch() }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Akıllı İlaçlama • Canlı Piyasa & Haber • Sesli Okuma",
                        color = AppColors.PrimarySubtleText,
                        fontSize = 11.sp
                    )
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            color = AppColors.PrimaryBorder,
                            trackColor = AppColors.PrimaryDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = AppColors.Surface,
                    tonalElevation = 4.dp
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = activeTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { activeTab = tab },
                            icon = {
                                Text(text = tab.emoji, fontSize = 18.sp)
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.Primary,
                                selectedTextColor = AppColors.Primary,
                                indicatorColor = AppColors.PrimaryLight,
                                unselectedIconColor = AppColors.TextSecondary,
                                unselectedTextColor = AppColors.TextSecondary
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    AppTab.HOME -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 1. Konum & Anlık Durum Kartı
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📍 $locationName",
                                            color = AppColors.TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = updatedText,
                                            color = AppColors.TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val cur = weatherSnapshot?.current
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (cur != null) {
                                                "${AgronomyEngine.weatherIcon(cur.weatherCode)} ${cur.temperature.roundToInt()}°C"
                                            } else {
                                                "--°C"
                                            },
                                            color = AppColors.Primary,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (cur != null) {
                                            Text(
                                                text = AgronomyEngine.weatherLabel(cur.weatherCode),
                                                color = AppColors.TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                val cur = weatherSnapshot?.current
                                Text(
                                    text = if (cur != null) {
                                        String.format(
                                            Locale.US,
                                            "Nem: %%%d   •   Rüzgâr: %.0f km/s   •   Anlık Yağış: %.1f mm",
                                            cur.humidity,
                                            cur.windSpeed,
                                            cur.precipitation
                                        )
                                    } else {
                                        "Nem: --%  •  Rüzgâr: -- km/s  •  Yağış: -- mm"
                                    },
                                    color = AppColors.TextMeta,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { requestLocationAndFetch() },
                                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text("📍 GPS Yenile", color = Color.White, fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { showCityPicker = true },
                                            border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = AppColors.PrimaryLight,
                                                contentColor = AppColors.Primary
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text("🗺️ İl Seç", fontSize = 11.sp)
                                        }
                                        val weatherCur = weatherSnapshot?.current
                                        if (weatherCur != null) {
                                            val weatherSpeechId = "home_weather_spray"
                                            val isWeatherSpeaking = activeSpeechId == weatherSpeechId
                                            val weatherSpeechText = buildString {
                                                append("$locationName güncel tarımsal hava durumu. ")
                                                append("Sıcaklık ${weatherCur.temperature.roundToInt()} derece, ")
                                                append("${AgronomyEngine.weatherLabel(weatherCur.weatherCode)}. ")
                                                append("Nem yüzde ${weatherCur.humidity}, rüzgâr saatte ${weatherCur.windSpeed.roundToInt()} kilometre, ")
                                                append("anlık yağış ${String.format(Locale.US, "%.1f", weatherCur.precipitation)} milimetre. ")
                                                sprayEval?.let {
                                                    append("İlaçlama durumu: ${it.title}. ${it.detail}")
                                                }
                                            }
                                            OutlinedButton(
                                                onClick = { toggleSpeak(weatherSpeechId, weatherSpeechText) },
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isWeatherSpeaking) AppColors.DangerBorder else AppColors.PrimaryBorder
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (isWeatherSpeaking) AppColors.DangerBg else AppColors.PrimaryLight,
                                                    contentColor = if (isWeatherSpeaking) AppColors.DangerText else AppColors.Primary
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.height(40.dp)
                                            ) {
                                                Text(
                                                    text = if (isWeatherSpeaking) "⏹️" else "🔊 Oku",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                            // 2. Foto / Video ile Bitki Hastalığı & İlaçlama Teşhisi Kartı
                            AppCard {
                                Text(
                                    text = "📸 FOTO / VİDEO İLE HASTALIK & İLAÇLAMA TEŞHİSİ",
                                    color = AppColors.Primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Yaprak veya meyve fotoğrafı/videosu çekerek olası hastalığı ve hava durumuna göre yapılacak ilaçlama bilgisini öğrenin:",
                                    color = AppColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            showDiagnosisDialog = true
                                            ensureCameraAndLaunch(PendingCameraMode.PHOTO)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Text("📷 Foto", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = {
                                            showDiagnosisDialog = true
                                            ensureCameraAndLaunch(PendingCameraMode.VIDEO)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Text("🎥 Video", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    OutlinedButton(
                                        onClick = { showDiagnosisDialog = true },
                                        border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = AppColors.PrimaryLight,
                                            contentColor = AppColors.Primary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Text("🖼️ Galeri", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (diagnosisResult != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val last = diagnosisResult!!
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AppColors.PrimaryLight)
                                            .border(1.dp, AppColors.PrimaryBorder, RoundedCornerShape(10.dp))
                                            .clickable { showDiagnosisDialog = true }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Son Teşhis: ${last.primaryMatch.diseaseName}",
                                            color = AppColors.Primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Detay →",
                                            color = AppColors.Primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 3. İlaçlama Uygunluk Göstergesi Kartı
                            AppCard {
                                Text(
                                    text = "🚜 ANLIK İLAÇLAMA UYGUNLUK DURUMU",
                                    color = AppColors.Primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val (bgColor, borderColor, titleColor) = when (sprayEval?.level) {
                                    SprayStatusLevel.DANGER -> Triple(AppColors.DangerBg, AppColors.DangerBorder, AppColors.DangerText)
                                    SprayStatusLevel.WARN -> Triple(AppColors.WarnBg, AppColors.WarnBorder, AppColors.WarnText)
                                    SprayStatusLevel.OK -> Triple(AppColors.PrimaryLight, AppColors.PrimaryBorder, AppColors.Primary)
                                    null -> Triple(AppColors.PrimaryLight, AppColors.GuideButtonBorder, AppColors.Primary)
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = sprayEval?.title ?: "Hesaplanıyor...",
                                        color = titleColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sprayEval?.detail
                                            ?: "Rüzgâr, sıcaklık, nem ve yağış koşulları analiz ediliyor.",
                                        color = AppColors.TextDetail,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            // 4. 7 Günlük Hava Tahmini Kartı
                            AppCard {
                                Text(
                                    text = "🌦️ 7 GÜNLÜK TARIMSAL HAVA TAHMİNİ",
                                    color = AppColors.Primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Gün detayını ve günlük ilaçlama durumunu görmek için güne dokunun:",
                                    color = AppColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val days = weatherSnapshot?.daily.orEmpty()
                                if (days.isEmpty()) {
                                    Text(
                                        text = "7 günlük tahmin verisi yükleniyor...",
                                        color = AppColors.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    val safeIndex = selectedForecastDayIndex.coerceIn(0, days.lastIndex)
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        days.forEachIndexed { i, day ->
                                            val label = AgronomyEngine.formatDayLabel(day.date, i)
                                            val daySpray = AgronomyEngine.evaluateDailySprayStatus(day)
                                            val isSelected = safeIndex == i
                                            val badgeColor = when (daySpray.level) {
                                                SprayStatusLevel.OK -> AppColors.Primary
                                                SprayStatusLevel.WARN -> AppColors.WarnText
                                                SprayStatusLevel.DANGER -> AppColors.DangerText
                                            }
                                            val badgeBg = when (daySpray.level) {
                                                SprayStatusLevel.OK -> AppColors.PrimaryLight
                                                SprayStatusLevel.WARN -> AppColors.WarnBg
                                                SprayStatusLevel.DANGER -> AppColors.DangerBg
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .width(120.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> AppColors.PrimaryLight
                                                            i == 0 -> Color(0xFFF0F8F1)
                                                            else -> AppColors.DayCardBg
                                                        }
                                                    )
                                                    .border(
                                                        if (isSelected) 1.5.dp else 1.dp,
                                                        if (isSelected) AppColors.Primary else AppColors.Divider,
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable { selectedForecastDayIndex = i }
                                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(label, color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(AgronomyEngine.weatherIcon(day.weatherCode), fontSize = 22.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "${day.tempMax.roundToInt()}° / ${day.tempMin.roundToInt()}°",
                                                    color = AppColors.TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    String.format(Locale.US, "💧 %.1f mm", day.precipitationSum),
                                                    color = AppColors.RainBlue,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    "💨 ${day.windSpeedMax.roundToInt()} km/s",
                                                    color = AppColors.TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = daySpray.title,
                                                    color = badgeColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(badgeBg)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    val chosen = days[safeIndex]
                                    val chosenLabel = AgronomyEngine.formatDayLabel(chosen.date, safeIndex)
                                    val chosenSpray = AgronomyEngine.evaluateDailySprayStatus(chosen)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AppColors.DayCardBg)
                                            .border(1.dp, AppColors.Divider, RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "📅 $chosenLabel • ${AgronomyEngine.weatherLabel(chosen.weatherCode)} (${chosen.tempMin.roundToInt()}°C – ${chosen.tempMax.roundToInt()}°C)",
                                            color = AppColors.Primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format(
                                                Locale.US,
                                                "Yağış: %.1f mm (Olasılık %%%d) • Maks. Nem: %%%d • ET₀: %.1f mm",
                                                chosen.precipitationSum,
                                                chosen.precipitationProbMax,
                                                chosen.humidityMax,
                                                chosen.et0
                                            ),
                                            color = AppColors.TextMeta,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${chosenSpray.title}: ${chosenSpray.detail}",
                                            color = AppColors.TextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            // 5. Yapay Zeka Karar Desteği Kartı
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🤖 YAPAY ZEKA TARIMSAL RİSK ANALİZİ",
                                        color = AppColors.Primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val sections = agronomicReport?.sections
                                    if (!sections.isNullOrEmpty()) {
                                        val aiSpeechId = "home_ai_report"
                                        val isAiSpeaking = activeSpeechId == aiSpeechId
                                        Text(
                                            text = if (isAiSpeaking) "⏹️ Durdur" else "🔊 Dinle",
                                            color = if (isAiSpeaking) AppColors.DangerText else AppColors.Primary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isAiSpeaking) AppColors.DangerBg else AppColors.PrimaryLight)
                                                .clickable {
                                                    toggleSpeak(
                                                        aiSpeechId,
                                                        "Yapay zeka 7 günlük tarımsal risk analizi. " + sections.joinToString(". ")
                                                    )
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val sections = agronomicReport?.sections
                                if (sections.isNullOrEmpty()) {
                                    Text(
                                        text = "Meteorolojik veriler bekleniyor...",
                                        color = AppColors.TextPrimary,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        sections.forEach { sec ->
                                            Text(
                                                text = sec,
                                                color = AppColors.TextPrimary,
                                                fontSize = 13.sp,
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // 5b. Canlı Piyasa & Tarım Haberleri Özet Kartı
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📈 CANLI PİYASA & TARIM HABERLERİ",
                                        color = AppColors.Primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Tümünü Aç →",
                                        color = AppColors.Primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { activeTab = AppTab.MARKETS }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val snap = marketSnapshot
                                if (snap == null) {
                                    Text(
                                        text = "Arpa, buğday, zeytin, Antep fıstığı, badem, kırmızı et, döviz, altın, akaryakıt, gübre fiyatları ve tarım haberleri yükleniyor...",
                                        color = AppColors.TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                } else {
                                    val usd = snap.fxGoldQuotes.firstOrNull { it.id.contains("USD") }
                                    val gram = snap.fxGoldQuotes.firstOrNull { it.id.contains("gram-altin") }
                                    val fuel = MarketAndNewsRepository.matchFuelCity(
                                        selectedFuelCityName ?: locationName,
                                        snap.fuelCities
                                    )
                                    val grainSample = snap.commodityQuotes.firstOrNull()
                                    val meatSample = snap.livestockQuotes.firstOrNull()
                                    Text(
                                        text = buildString {
                                            if (usd != null) append("💵 Dolar: ${usd.primaryPrice}  ")
                                            if (gram != null) append("🪙 Gram Altın: ${gram.primaryPrice}\n")
                                            if (fuel != null) append("⛽ ${fuel.cityName}: Motorin ${fuel.diesel} TL • Benzin ${fuel.gasoline95} TL\n")
                                            if (grainSample != null) append("🌾 ${grainSample.name}: ${grainSample.primaryPrice} ${grainSample.unit}\n")
                                            if (meatSample != null) append("🐄 ${meatSample.name}: ${meatSample.primaryPrice} ${meatSample.unit}")
                                        }.trim(),
                                        color = AppColors.TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                    if (snap.newsArticles.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "📰 Son Haber: ${snap.newsArticles.first().title}",
                                            color = AppColors.Primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.clickable { activeTab = AppTab.MARKETS }
                                        )
                                    }
                                }
                            }

                            // 6. Ürün ve Hastalık Rehberi Önizleme Kartı
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🌱 ÜRÜN BAZLI HASTALIK & RİSK",
                                        color = AppColors.Primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Görselli Atlas →",
                                        color = AppColors.Primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { activeTab = AppTab.CROPS }
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Görselleri, fenoloji takvimini ve detaylı ilaçlama rehberini görmek için ürüne dokunun:",
                                    color = AppColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val crops = CropCatalog.crops
                                    for (i in crops.indices step 2) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CropButton(
                                                crop = crops[i],
                                                onClick = { selectedCrop = crops[i] },
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (i + 1 < crops.size) {
                                                CropButton(
                                                    crop = crops[i + 1],
                                                    onClick = { selectedCrop = crops[i + 1] },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            // 7. Gömülü Tarımsal Bilgi Bankası & Sesli Arama
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📚 TARIMSAL BİLGİ BANKASI",
                                        color = AppColors.Primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "🎙️ Sesle Ara",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.Primary)
                                            .clickable { launchHandsFreeVoiceSearch() }
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tüm meteoroloji, ilaçlama güvenliği ve mücadele bilgileri uygulama içine gömülüdür (harici siteye yönlendirmez):",
                                    color = AppColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { launchHandsFreeVoiceSearch() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                ) {
                                    Text(
                                        text = "🎙️ Tarlada Eller Serbest Sesli Tarım Arama",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CropCatalog.embeddedGuideButtons.forEach { guide ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(AppColors.GuideButtonBg)
                                                .border(1.dp, AppColors.GuideButtonBorder, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    bkuFilterText = ""
                                                    activeGuideTopic = guide.id
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = guide.title,
                                                color = AppColors.Primary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = guide.subtitle,
                                                color = AppColors.TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⚠️ Yasal Uyarı: Bu uygulama meteorolojik ve zirai karar desteği sunar. İlaç seçimi, doz ve hasat aralığı için mutlaka ürün etiketindeki ruhsat bilgilerini esas alınız.",
                                    color = AppColors.TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    AppTab.MARKETS -> {
                        MarketNewsTab(
                            snapshot = marketSnapshot,
                            isLoading = isMarketLoading,
                            errorMessage = marketError,
                            selectedFuelCityName = selectedFuelCityName,
                            activeSpeechId = activeSpeechId,
                            onRefresh = { fetchMarkets(forceRefresh = true) },
                            onOpenFuelCityPicker = { showFuelCityPicker = true },
                            onOpenNewsArticle = { article -> selectedNewsArticle = article },
                            onToggleSpeak = { sid, text -> toggleSpeak(sid, text) }
                        )
                    }

                    AppTab.CROPS -> {
                        CropsAtlasTab(
                            avgTemp = agronomicReport?.avgTemp ?: 20.0,
                            maxHum = agronomicReport?.maxHum ?: 65.0,
                            totalRain = agronomicReport?.totalRain ?: 0.0,
                            onSelectCrop = { selectedCrop = it },
                            onOpenVoiceSearch = { launchHandsFreeVoiceSearch() }
                        )
                    }

                    AppTab.SETTINGS -> {
                        SettingsTab(
                            userFontScale = userFontScale,
                            onFontScaleChange = { newScale ->
                                userFontScale = newScale
                                repo.saveFontScale(newScale)
                            },
                            autoSpeakVoiceResult = autoSpeakVoiceResult,
                            onToggleAutoSpeakVoice = { enabled ->
                                autoSpeakVoiceResult = enabled
                                repo.saveAutoSpeakVoiceSearch(enabled)
                            },
                            speechRate = speechRate,
                            onSpeechRateChange = { newRate ->
                                speechRate = newRate
                                repo.saveSpeechRate(newRate)
                                voiceController.setSpeechRate(newRate)
                            },
                            activeSpeechId = activeSpeechId,
                            onTestVoiceReadout = {
                                toggleSpeak(
                                    "settings_voice_test",
                                    "Tarım Meteoroloji yapay zeka sesli okuma testi. Bugün hava ilaçlama, piyasa ve ürün takibi için sesli bilgilendirme aktif."
                                )
                            },
                            defaultDiagnosisCropId = diagnosisCropId,
                            onSelectDefaultDiagnosisCrop = { diagnosisCropId = it },
                            updatedText = updatedText,
                            roomCacheSummary = roomCacheSummary,
                            onClearCache = {
                                scope.launch {
                                    withContext(Dispatchers.IO) { repo.clearWeatherCache() }
                                    weatherSnapshot = null
                                    roomCacheSummary = withContext(Dispatchers.IO) {
                                        repo.getRoomCacheSummary()
                                    }
                                    Toast.makeText(
                                        context,
                                        "Room önbelleği temizlendi. Güncel veri çekiliyor...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    fetchWeatherFor(lat, lon, locationName)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCityPicker) {
        CityPickerDialog(
            onDismiss = { showCityPicker = false },
            onSelectCity = { city ->
                lat = city.lat
                lon = city.lon
                locationName = city.name
                repo.saveLocation(lat, lon, locationName)
                fetchWeatherFor(lat, lon, city.name)
            },
            onOpenSearch = { showGeocodeSearch = true }
        )
    }

    if (showGeocodeSearch) {
        GeocodeSearchDialog(
            onDismiss = { showGeocodeSearch = false },
            onSearch = { query ->
                isLoading = true
                scope.launch {
                    try {
                        val found = withContext(Dispatchers.IO) {
                            repo.searchLocationByName(query)
                        }
                        if (found != null) {
                            lat = found.lat
                            lon = found.lon
                            locationName = found.locationName
                            repo.saveLocation(lat, lon, locationName)
                            fetchWeatherFor(lat, lon, found.locationName)
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Konum bulunamadı.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (_: Exception) {
                        isLoading = false
                        Toast.makeText(
                            context,
                            "Arama sırasında bağlantı hatası oluştu.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    val crop = selectedCrop
    if (crop != null) {
        CropDetailDialog(
            crop = crop,
            avgTemp = agronomicReport?.avgTemp ?: 20.0,
            maxHum = agronomicReport?.maxHum ?: 65.0,
            totalRain = agronomicReport?.totalRain ?: 0.0,
            activeSpeechId = activeSpeechId,
            onToggleSpeak = { sid, text -> toggleSpeak(sid, text) },
            onOpenBkuGuide = { keyword ->
                bkuFilterText = keyword
                activeGuideTopic = GuideTopicId.BKU
            },
            onOpenDiagnosisForCrop = { cropId ->
                diagnosisCropId = cropId
                showDiagnosisDialog = true
            },
            onDismiss = { selectedCrop = null }
        )
    }

    val topic = activeGuideTopic
    if (topic != null) {
        AgronomyGuideDialog(
            topic = topic,
            initialFilterText = bkuFilterText,
            activeSpeechId = activeSpeechId,
            onToggleSpeak = { sid, text -> toggleSpeak(sid, text) },
            onSelectTopic = { activeGuideTopic = it },
            onStartVoiceSearch = { launchHandsFreeVoiceSearch() },
            onDismiss = { activeGuideTopic = null }
        )
    }

    if (showVoiceSearchDialog) {
        VoiceSearchDialog(
            query = voiceSearchQuery,
            onQueryChange = { newQuery ->
                voiceSearchQuery = newQuery
            },
            isListening = isVoiceListening,
            partialTranscript = voicePartialTranscript,
            voiceStatusText = voiceStatusText,
            rmsLevel = voiceRmsLevel,
            autoSpeakEnabled = autoSpeakVoiceResult,
            onToggleAutoSpeak = { enabled ->
                autoSpeakVoiceResult = enabled
                repo.saveAutoSpeakVoiceSearch(enabled)
            },
            results = voiceResults,
            onStartListening = {
                val micOk = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (micOk) {
                    startInlineVoiceListening()
                } else {
                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onStopListening = {
                voiceController.stopListening()
                isVoiceListening = false
                voiceRmsLevel = 0f
            },
            onSpeakText = { text -> voiceController.speak(text) },
            onStopSpeaking = { voiceController.stopSpeaking() },
            onOpenResult = { result ->
                voiceController.stopListening()
                voiceController.stopSpeaking()
                isVoiceListening = false
                showVoiceSearchDialog = false
                when {
                    result.cropId != null -> {
                        selectedCrop = CropCatalog.crops.firstOrNull { it.id == result.cropId }
                    }
                    result.guideTopicId != null -> {
                        bkuFilterText = result.bkuFilterKeyword.orEmpty()
                        activeGuideTopic = result.guideTopicId
                    }
                }
            },
            onDismiss = {
                voiceController.stopListening()
                voiceController.stopSpeaking()
                isVoiceListening = false
                showVoiceSearchDialog = false
            }
        )
    }

    if (showDiagnosisDialog) {
        PlantDiagnosisDialog(
            selectedCropId = diagnosisCropId,
            onSelectCropId = { diagnosisCropId = it },
            isAnalyzing = isAnalyzingMedia,
            diagnosisError = diagnosisError,
            result = diagnosisResult,
            activeSpeechId = activeSpeechId,
            onToggleSpeak = { sid, text -> toggleSpeak(sid, text) },
            onTakePhoto = { ensureCameraAndLaunch(PendingCameraMode.PHOTO) },
            onRecordVideo = { ensureCameraAndLaunch(PendingCameraMode.VIDEO) },
            onPickFromGallery = {
                pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onOpenBkuGuide = { keyword ->
                bkuFilterText = keyword
                activeGuideTopic = GuideTopicId.BKU
            },
            onDismiss = { showDiagnosisDialog = false }
        )
    }

    val openArticle = selectedNewsArticle
    if (openArticle != null) {
        NewsArticleDialog(
            article = openArticle,
            activeSpeechId = activeSpeechId,
            onToggleSpeak = { sid, text -> toggleSpeak(sid, text) },
            onDismiss = { selectedNewsArticle = null }
        )
    }

    if (showFuelCityPicker) {
        FuelCityPickerDialog(
            cities = marketSnapshot?.fuelCities.orEmpty(),
            selectedCityName = selectedFuelCityName,
            onSelectCity = { city ->
                selectedFuelCityName = city.cityName
                repo.saveSelectedFuelCity(city.cityName)
            },
            onDismiss = { showFuelCityPicker = false }
        )
    }
}

@Composable
private fun CropsAtlasTab(
    avgTemp: Double,
    maxHum: Double,
    totalRain: Double,
    onSelectCrop: (CropInfo) -> Unit,
    onOpenVoiceSearch: () -> Unit
) {
    var atlasQuery by remember { mutableStateOf("") }
    val filteredCrops = remember(atlasQuery) {
        val q = AgriculturalSearchEngine.normalizeTurkish(atlasQuery)
        if (q.isEmpty()) {
            CropCatalog.crops
        } else {
            CropCatalog.crops.filter { crop ->
                val searchable = AgriculturalSearchEngine.normalizeTurkish(
                    "${crop.id} ${crop.name} ${crop.subtitle} ${crop.diseases} ${crop.activeIngredientGroups}"
                )
                searchable.contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppCard {
            Text(
                text = "🌱 GÖRSELLİ ÜRÜN & BİTKİ KORUMA ATLASI",
                color = AppColors.Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bağ, Zeytin, Badem, Buğday, Arpa, Nohut, Nar, Elma ve Sebze için detaylı iklim, sulama, gübreleme, fenoloji takvimi ve görselli hastalık atlasını inceleyin.",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = atlasQuery,
                    onValueChange = { atlasQuery = it },
                    singleLine = true,
                    placeholder = {
                        Text("Ürün veya hastalık filtrele...", fontSize = 12.sp)
                    },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onOpenVoiceSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("🎙️ Ses", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (filteredCrops.isEmpty()) {
            AppCard {
                Text(
                    text = "“$atlasQuery” ile eşleşen ürün bulunamadı.",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        filteredCrops.forEach { crop ->
            val riskText = AgronomyEngine.evaluateCropRisk(crop, avgTemp, maxHum, totalRain)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Surface,
                border = BorderStroke(1.dp, AppColors.CardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clickable { onSelectCrop(crop) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CropHeroIllustration(
                            cropId = crop.id,
                            accentColor = Color(crop.colorHex),
                            modifier = Modifier
                                .width(104.dp)
                                .height(88.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${crop.emoji} ${crop.name}",
                                color = Color(crop.colorHex),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = crop.subtitle,
                                color = AppColors.TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = riskText,
                                color = AppColors.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "⏳ Kritik Dönem: ${crop.criticalStage}",
                        color = AppColors.TextPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🦠 Başlıca Hastalıklar: ${crop.diseases}",
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${crop.diseaseDetails.size} Görselli Hastalık Kartı • ${crop.phenologySteps.size} Fenoloji Aşaması",
                            color = AppColors.Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Detayları Aç →",
                            color = AppColors.Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsTab(
    userFontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    autoSpeakVoiceResult: Boolean,
    onToggleAutoSpeakVoice: (Boolean) -> Unit,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    activeSpeechId: String?,
    onTestVoiceReadout: () -> Unit,
    defaultDiagnosisCropId: String?,
    onSelectDefaultDiagnosisCrop: (String?) -> Unit,
    updatedText: String,
    roomCacheSummary: String,
    onClearCache: () -> Unit
) {
    val presets = listOf(
        0.85f to "Küçük (%85)",
        1.00f to "Normal (%100)",
        1.20f to "Büyük (%120)",
        1.40f to "Çok Büyük (%140)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Yazı Boyutu Ayarı
        AppCard {
            Text(
                text = "🔠 YAZI BOYUTU AYARI",
                color = AppColors.Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Uygulamadaki tüm başlık, hava tahmini, ürün bilgisi ve ilaçlama rehberi metinlerinin okunaklılığını ayarlayın:",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mevcut Ölçek:",
                    color = AppColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "%${(userFontScale * 100).roundToInt()}",
                    color = AppColors.Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = userFontScale,
                onValueChange = { onFontScaleChange(it) },
                valueRange = 0.85f..1.40f,
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.Primary,
                    activeTrackColor = AppColors.Primary,
                    inactiveTrackColor = AppColors.PrimaryBorder
                )
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (scaleVal, label) ->
                    val active = kotlin.math.abs(userFontScale - scaleVal) < 0.04f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) AppColors.Primary else AppColors.PrimaryLight)
                            .border(
                                1.dp,
                                if (active) AppColors.Primary else AppColors.PrimaryBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onFontScaleChange(scaleVal) }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.White else AppColors.Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.DayCardBg)
                    .border(1.dp, AppColors.Divider, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "👁️ Canlı Yazı Önizlemesi",
                    color = AppColors.Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bağda külleme ve mildiyö takibi için sabah serinliğinde ilaçlama yapın; rüzgâr 15 km/s üzerindeyse püskürtmeyi erteleyin.",
                    color = AppColors.TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. Sesli Arama & Sesli Okuma Tercihi
        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔊 SESLİ OKUMA & ELLER SERBEST ARAMA",
                        color = AppColors.Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Tarlada sesli arama yaptığınızda sonucu otomatik Türkçe sesli okur; ayrıca tüm ekranlardaki 🔊 Dinle butonları ile rapor ve haberleri dinleyebilirsiniz:",
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = autoSpeakVoiceResult,
                    onCheckedChange = onToggleAutoSpeakVoice,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppColors.Primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sesli Okuma Hızı:",
                    color = AppColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format(Locale.US, "%.2fx", speechRate)}",
                    color = AppColors.Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.85f to "Yavaş (0.85x)", 1.0f to "Normal (1.0x)", 1.2f to "Hızlı (1.2x)").forEach { (rateVal, rateLabel) ->
                    val active = kotlin.math.abs(speechRate - rateVal) < 0.05f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) AppColors.Primary else AppColors.PrimaryLight)
                            .border(1.dp, if (active) AppColors.Primary else AppColors.PrimaryBorder, RoundedCornerShape(8.dp))
                            .clickable { onSpeechRateChange(rateVal) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = rateLabel,
                            color = if (active) Color.White else AppColors.Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            val isTestingSpeech = activeSpeechId == "settings_voice_test"
            OutlinedButton(
                onClick = onTestVoiceReadout,
                border = BorderStroke(
                    1.dp,
                    if (isTestingSpeech) AppColors.DangerBorder else AppColors.PrimaryBorder
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isTestingSpeech) AppColors.DangerBg else AppColors.PrimaryLight,
                    contentColor = if (isTestingSpeech) AppColors.DangerText else AppColors.Primary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isTestingSpeech) "⏹️ Test Okumasını Durdur" else "🔊 Örnek Sesli Okuma Testi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. Varsayılan Teşhis Ürünü
        AppCard {
            Text(
                text = "📸 VARSAYILAN KAMERA TEŞHİS ÜRÜNÜ",
                color = AppColors.Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Foto veya video çekerken otomatik seçili gelecek ürün türünü belirleyin:",
                color = AppColors.TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val allActive = defaultDiagnosisCropId == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (allActive) AppColors.Primary else AppColors.DayCardBg)
                        .border(1.dp, if (allActive) AppColors.Primary else AppColors.Divider, RoundedCornerShape(8.dp))
                        .clickable { onSelectDefaultDiagnosisCrop(null) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🌱 Genel",
                        color = if (allActive) Color.White else AppColors.TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                CropCatalog.crops.forEach { crop ->
                    val active = defaultDiagnosisCropId == crop.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) Color(crop.colorHex) else AppColors.DayCardBg)
                            .border(1.dp, if (active) Color(crop.colorHex) else AppColors.Divider, RoundedCornerShape(8.dp))
                            .clickable { onSelectDefaultDiagnosisCrop(crop.id) }
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
        }

        // 3. Çevrimdışı Önbellek & Uygulama Hakkında
        AppCard {
            Text(
                text = "💾 ROOM 7 GÜNLÜK ÇEVRİMDIŞI ÖNBELLEK",
                color = AppColors.Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = roomCacheSummary,
                color = AppColors.Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Son veri durumu: $updatedText",
                color = AppColors.TextPrimary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClearCache,
                border = BorderStroke(1.dp, AppColors.PrimaryBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppColors.PrimaryLight,
                    contentColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Önbelleği Temizle ve Yenile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "🌾 Tarım Meteoroloji AI v2.0 PRO\nProgram Tasarımı: Mehmet Cengiz Akdağ\nTüm ürün rehberleri, hastalık görselleri, foto/video doku analizi, sesli okuma ve ilaçlama bilgi bankası cihaz içinde çevrimdışı çalışır; canlı piyasa/haber modülü harici tarayıcıya yönlendirme yapmadan uygulama içinde sunulur.",
                color = AppColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AppCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun CropButton(
    crop: CropInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(crop.colorHex)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(46.dp)
    ) {
        Text(
            text = "${crop.emoji} ${crop.name}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
