package com.example.simpleweatherapp.data.local

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface WeatherDao {
    //current Weather
    @Query("select * from Weather")
    fun getCurrentWeather(): LiveData<Weather>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weather: Weather)

    @Query("delete from Weather")
    suspend fun deleteCurrentWeather()


    //Day Weather
    @Query("select * from Day")
    fun getDays(): LiveData<List<Day>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDays(vararg day: Day)

    @Query("delete from Day")
    suspend fun deleteDays()

    //Hour Weather
    @Query("select * from Hour")
    fun getHours(): LiveData<List<Hour>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHours(vararg hour: Hour)

    @Query("delete from Hour")
    suspend fun deleteHours()

    //City
    @Query("select * from city")
    fun getCities(): LiveData<List<City>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCities(vararg city: City)

    @Delete
    suspend fun deleteCity(city: City?)

    @Update
    suspend fun updateCity(vararg city: City)

    //Alarm
    @Query("select * from Alarm")
    fun getAlarms(): LiveData<List<Alarm>>

    @Query("select * from Alarm")
    suspend fun getAlarmsSuspend(): List<Alarm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlarms(vararg alarm: Alarm): List<Long>

    @Delete
    suspend fun deleteAlarm(alarm: Alarm?)


}

@Database(
    entities = [Weather::class, Day::class, Hour::class, City::class, Alarm::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
}

private lateinit var INSTANCE: WeatherDatabase

fun getDatabase(context: Context): WeatherDatabase {
    synchronized(WeatherDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                WeatherDatabase::class.java,
                "WeatherData"
            ).build()
        }
    }
    return INSTANCE
}
