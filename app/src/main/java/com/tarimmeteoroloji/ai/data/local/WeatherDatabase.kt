package com.tarimmeteoroloji.ai.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Entity(tableName = "weather_forecast_cache")
data class WeatherForecastCacheEntity(
    @androidx.room.PrimaryKey
    val locationKey: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val rawJson: String,
    val currentTemp: Double?,
    val currentHumidity: Int?,
    val currentWindSpeed: Double?,
    val currentPrecipitation: Double?,
    val currentWeatherCode: Int?,
    val cachedTimeLabel: String,
    val fetchedAtMillis: Long,
    val expiresAtMillis: Long
)

@Entity(
    tableName = "daily_forecast_days",
    primaryKeys = ["locationKey", "forecastDate"]
)
data class DailyForecastEntity(
    val locationKey: String,
    val forecastDate: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationSum: Double,
    val precipitationProbMax: Int,
    val windSpeedMax: Double,
    val humidityMax: Int,
    val et0: Double,
    val fetchedAtMillis: Long
)

@Dao
interface WeatherCacheDao {

    @Query(
        "SELECT * FROM weather_forecast_cache " +
            "WHERE locationKey = :locationKey AND expiresAtMillis >= :nowMillis LIMIT 1"
    )
    suspend fun getValidForecastCache(
        locationKey: String,
        nowMillis: Long
    ): WeatherForecastCacheEntity?

    @Query(
        "SELECT * FROM weather_forecast_cache " +
            "WHERE expiresAtMillis >= :nowMillis " +
            "ORDER BY fetchedAtMillis DESC LIMIT 1"
    )
    suspend fun getLatestValidForecastCache(nowMillis: Long): WeatherForecastCacheEntity?

    @Query(
        "SELECT * FROM daily_forecast_days " +
            "WHERE locationKey = :locationKey " +
            "ORDER BY forecastDate ASC"
    )
    suspend fun getDailyForecasts(locationKey: String): List<DailyForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertForecastCache(cache: WeatherForecastCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyForecasts(days: List<DailyForecastEntity>)

    @Query("DELETE FROM daily_forecast_days WHERE locationKey = :locationKey")
    suspend fun deleteDailyForLocation(locationKey: String)

    @Transaction
    suspend fun saveCompleteForecast(
        cache: WeatherForecastCacheEntity,
        days: List<DailyForecastEntity>
    ) {
        upsertForecastCache(cache)
        deleteDailyForLocation(cache.locationKey)
        insertDailyForecasts(days)
    }

    @Query("DELETE FROM weather_forecast_cache WHERE expiresAtMillis < :nowMillis")
    suspend fun purgeExpiredForecasts(nowMillis: Long)

    @Query(
        "DELETE FROM daily_forecast_days " +
            "WHERE locationKey NOT IN (SELECT locationKey FROM weather_forecast_cache)"
    )
    suspend fun purgeOrphanedDailyDays()

    @Transaction
    suspend fun purgeExpired(nowMillis: Long) {
        purgeExpiredForecasts(nowMillis)
        purgeOrphanedDailyDays()
    }

    @Query("DELETE FROM weather_forecast_cache")
    suspend fun clearAllForecastCaches()

    @Query("DELETE FROM daily_forecast_days")
    suspend fun clearAllDailyDays()

    @Transaction
    suspend fun clearAll() {
        clearAllDailyDays()
        clearAllForecastCaches()
    }

    @Query("SELECT COUNT(*) FROM weather_forecast_cache WHERE expiresAtMillis >= :nowMillis")
    suspend fun countValidLocations(nowMillis: Long): Int
}

@Database(
    entities = [WeatherForecastCacheEntity::class, DailyForecastEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppWeatherDatabase : RoomDatabase() {
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppWeatherDatabase? = null

        fun getInstance(context: Context): AppWeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppWeatherDatabase::class.java,
                    "tarim_meteoroloji_weather.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
