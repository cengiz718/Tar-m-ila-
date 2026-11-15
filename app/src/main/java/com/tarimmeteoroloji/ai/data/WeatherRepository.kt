package com.tarimmeteoroloji.ai.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Looper
import androidx.core.content.edit
import com.tarimmeteoroloji.ai.data.local.AppWeatherDatabase
import com.tarimmeteoroloji.ai.data.local.DailyForecastEntity
import com.tarimmeteoroloji.ai.data.local.WeatherForecastCacheEntity
import com.tarimmeteoroloji.ai.model.CurrentWeather
import com.tarimmeteoroloji.ai.model.WeatherDay
import com.tarimmeteoroloji.ai.model.WeatherSnapshot
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedLocationState(
    val lat: Double,
    val lon: Double,
    val locationName: String
)

data class CachedWeatherResult(
    val snapshot: WeatherSnapshot,
    val cachedTime: String,
    val locationName: String,
    val fromRoomCache: Boolean,
    val expiresAtMillis: Long
)

class WeatherRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dao = AppWeatherDatabase.getInstance(context).weatherCacheDao()

    fun loadSavedLocation(): SavedLocationState {
        val latBits = prefs.getLong(KEY_LAT, java.lang.Double.doubleToRawLongBits(DEFAULT_LAT))
        val lonBits = prefs.getLong(KEY_LON, java.lang.Double.doubleToRawLongBits(DEFAULT_LON))
        val name = prefs.getString(KEY_LOC_NAME, DEFAULT_LOC_NAME) ?: DEFAULT_LOC_NAME
        return SavedLocationState(
            lat = java.lang.Double.longBitsToDouble(latBits),
            lon = java.lang.Double.longBitsToDouble(lonBits),
            locationName = name
        )
    }

    fun saveLocation(lat: Double, lon: Double, locationName: String) {
        prefs.edit {
            putLong(KEY_LAT, java.lang.Double.doubleToRawLongBits(lat))
            putLong(KEY_LON, java.lang.Double.doubleToRawLongBits(lon))
            putString(KEY_LOC_NAME, locationName)
        }
    }

    private fun buildLocationKey(lat: Double, lon: Double): String {
        return String.format(Locale.US, "%.2f_%.2f", lat, lon)
    }

    suspend fun loadRoomCachedWeather(lat: Double, lon: Double): CachedWeatherResult? {
        val now = System.currentTimeMillis()
        dao.purgeExpired(now)

        val exactKey = buildLocationKey(lat, lon)
        val cacheEntry = dao.getValidForecastCache(exactKey, now)
            ?: dao.getLatestValidForecastCache(now)
            ?: return null

        val dailyEntities = dao.getDailyForecasts(cacheEntry.locationKey)
        val snapshot = if (dailyEntities.isNotEmpty()) {
            val current = if (cacheEntry.currentTemp != null &&
                cacheEntry.currentHumidity != null &&
                cacheEntry.currentWindSpeed != null &&
                cacheEntry.currentPrecipitation != null &&
                cacheEntry.currentWeatherCode != null
            ) {
                CurrentWeather(
                    temperature = cacheEntry.currentTemp,
                    humidity = cacheEntry.currentHumidity,
                    windSpeed = cacheEntry.currentWindSpeed,
                    precipitation = cacheEntry.currentPrecipitation,
                    weatherCode = cacheEntry.currentWeatherCode
                )
            } else {
                runCatching { AgronomyEngine.parseWeatherSnapshot(cacheEntry.rawJson).current }.getOrNull()
            }

            WeatherSnapshot(
                current = current,
                daily = dailyEntities.map { d ->
                    WeatherDay(
                        date = d.forecastDate,
                        weatherCode = d.weatherCode,
                        tempMax = d.tempMax,
                        tempMin = d.tempMin,
                        precipitationSum = d.precipitationSum,
                        precipitationProbMax = d.precipitationProbMax,
                        windSpeedMax = d.windSpeedMax,
                        humidityMax = d.humidityMax,
                        et0 = d.et0
                    )
                }
            )
        } else {
            runCatching { AgronomyEngine.parseWeatherSnapshot(cacheEntry.rawJson) }.getOrNull()
                ?: return null
        }

        return CachedWeatherResult(
            snapshot = snapshot,
            cachedTime = cacheEntry.cachedTimeLabel,
            locationName = cacheEntry.locationName,
            fromRoomCache = true,
            expiresAtMillis = cacheEntry.expiresAtMillis
        )
    }

    suspend fun fetchAndCacheForecast(
        lat: Double,
        lon: Double,
        locationName: String
    ): CachedWeatherResult {
        val apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,weather_code" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,relative_humidity_2m_max,et0_fao_evapotranspiration" +
            "&timezone=auto&forecast_days=7"

        val response = httpGet(apiUrl)
        val snapshot = AgronomyEngine.parseWeatherSnapshot(response)
        val nowMillis = System.currentTimeMillis()
        val expiresAt = nowMillis + SEVEN_DAYS_MILLIS
        val nowStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(nowMillis))
        val locKey = buildLocationKey(lat, lon)

        val cur = snapshot.current
        val cacheEntity = WeatherForecastCacheEntity(
            locationKey = locKey,
            locationName = locationName,
            latitude = lat,
            longitude = lon,
            rawJson = response,
            currentTemp = cur?.temperature,
            currentHumidity = cur?.humidity,
            currentWindSpeed = cur?.windSpeed,
            currentPrecipitation = cur?.precipitation,
            currentWeatherCode = cur?.weatherCode,
            cachedTimeLabel = nowStr,
            fetchedAtMillis = nowMillis,
            expiresAtMillis = expiresAt
        )

        val dayEntities = snapshot.daily.map { day ->
            DailyForecastEntity(
                locationKey = locKey,
                forecastDate = day.date,
                weatherCode = day.weatherCode,
                tempMax = day.tempMax,
                tempMin = day.tempMin,
                precipitationSum = day.precipitationSum,
                precipitationProbMax = day.precipitationProbMax,
                windSpeedMax = day.windSpeedMax,
                humidityMax = day.humidityMax,
                et0 = day.et0,
                fetchedAtMillis = nowMillis
            )
        }

        dao.purgeExpired(nowMillis)
        dao.saveCompleteForecast(cacheEntity, dayEntities)

        return CachedWeatherResult(
            snapshot = snapshot,
            cachedTime = nowStr,
            locationName = locationName,
            fromRoomCache = false,
            expiresAtMillis = expiresAt
        )
    }

    suspend fun getRoomCacheSummary(): String {
        val now = System.currentTimeMillis()
        dao.purgeExpired(now)
        val count = dao.countValidLocations(now)
        val latest = dao.getLatestValidForecastCache(now)
        return if (count > 0 && latest != null) {
            "Room Veritabanı: $count konum (7 günlük önbellek aktif • Son: ${latest.cachedTimeLabel})"
        } else {
            "Room Veritabanı: Henüz önbelleğe alınmış veri yok"
        }
    }

    suspend fun clearWeatherCache() {
        dao.clearAll()
        prefs.edit {
            remove(KEY_LEGACY_CACHED_WEATHER)
            remove(KEY_LEGACY_CACHED_TIME)
        }
    }

    fun loadFontScale(): Float {
        return prefs.getFloat(KEY_FONT_SCALE, 1.0f).coerceIn(0.85f, 1.45f)
    }

    fun saveFontScale(scale: Float) {
        prefs.edit {
            putFloat(KEY_FONT_SCALE, scale.coerceIn(0.85f, 1.45f))
        }
    }

    fun loadAutoSpeakVoiceSearch(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SPEAK, true)
    }

    fun saveAutoSpeakVoiceSearch(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_AUTO_SPEAK, enabled)
        }
    }

    fun loadSpeechRate(): Float {
        return prefs.getFloat(KEY_SPEECH_RATE, 1.0f).coerceIn(0.75f, 1.35f)
    }

    fun saveSpeechRate(rate: Float) {
        prefs.edit {
            putFloat(KEY_SPEECH_RATE, rate.coerceIn(0.75f, 1.35f))
        }
    }

    fun loadSelectedFuelCity(): String? {
        return prefs.getString(KEY_FUEL_CITY, null)
    }

    fun saveSelectedFuelCity(city: String) {
        prefs.edit {
            putString(KEY_FUEL_CITY, city)
        }
    }

    fun saveMarketCacheJson(json: String) {
        prefs.edit {
            putString(KEY_MARKET_CACHE_JSON, json)
        }
    }

    fun loadMarketCacheJson(): String? {
        return prefs.getString(KEY_MARKET_CACHE_JSON, null)
    }

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun searchLocationByName(query: String): SavedLocationState? {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val apiUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=tr&format=json"
        val res = httpGet(apiUrl)
        val json = JSONObject(res)
        val results = json.optJSONArray("results") ?: return null
        if (results.length() == 0) return null
        val item = results.getJSONObject(0)
        val admin = item.optString("admin1", "")
        val locName = item.getString("name") + if (admin.isEmpty()) "" else " ($admin)"
        return SavedLocationState(
            lat = item.getDouble("latitude"),
            lon = item.getDouble("longitude"),
            locationName = locName
        )
    }

    @SuppressLint("MissingPermission")
    fun resolveDeviceCoordinates(onCoordinates: (Double, Double) -> Unit) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
            var best: Location? = null
            for (provider in arrayOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
                if (lm.isProviderEnabled(provider)) {
                    val l = lm.getLastKnownLocation(provider)
                    if (l != null && (best == null || l.time > best.time)) {
                        best = l
                    }
                }
            }
            if (best != null) {
                onCoordinates(best.latitude, best.longitude)
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        onCoordinates(location.latitude, location.longitude)
                        lm.removeUpdates(this)
                    }
                }
                when {
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                        lm.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0L,
                            0f,
                            listener,
                            Looper.getMainLooper()
                        )
                    lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                        lm.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0L,
                            0f,
                            listener,
                            Looper.getMainLooper()
                        )
                }
            }
        } catch (_: Exception) {
        }
    }

    fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale("tr", "TR"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var resolved: String? = null
                val lock = Object()
                var done = false
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    val a = addresses.firstOrNull()
                    if (a != null) {
                        val sub = a.subAdminArea ?: a.locality
                        val admin = a.adminArea
                        if (!sub.isNullOrBlank() || !admin.isNullOrBlank()) {
                            resolved = listOfNotNull(sub, admin).distinct().joinToString(" / ")
                        }
                    }
                    synchronized(lock) {
                        done = true
                        lock.notifyAll()
                    }
                }
                synchronized(lock) {
                    if (!done) {
                        try {
                            lock.wait(2500)
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                }
                resolved
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lon, 1)
                val a = list?.firstOrNull()
                if (a != null) {
                    val sub = a.subAdminArea ?: a.locality
                    val admin = a.adminArea
                    if (!sub.isNullOrBlank() || !admin.isNullOrBlank()) {
                        return listOfNotNull(sub, admin).distinct().joinToString(" / ")
                    }
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun httpGet(urlStr: String): String {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "TarimMeteorolojiAI/2.0")
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
                    sb.append(line)
                }
                sb.toString()
            }
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        private const val SEVEN_DAYS_MILLIS = 7L * 24L * 60L * 60L * 1000L
        private const val PREFS_NAME = "tarim_ai_prefs"
        private const val KEY_LAT = "lat"
        private const val KEY_LON = "lon"
        private const val KEY_LOC_NAME = "loc_name"
        private const val KEY_LEGACY_CACHED_WEATHER = "cached_weather"
        private const val KEY_LEGACY_CACHED_TIME = "cached_time"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_AUTO_SPEAK = "auto_speak_voice_search"
        private const val KEY_SPEECH_RATE = "tts_speech_rate"
        private const val KEY_FUEL_CITY = "selected_fuel_city"
        private const val KEY_MARKET_CACHE_JSON = "cached_market_dashboard_json"
        private const val DEFAULT_LAT = 37.7648
        private const val DEFAULT_LON = 38.2786
        private const val DEFAULT_LOC_NAME = "Adıyaman Merkez"
    }
}
