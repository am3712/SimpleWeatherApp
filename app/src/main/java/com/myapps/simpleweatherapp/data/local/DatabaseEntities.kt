package com.myapps.simpleweatherapp.data.local

import androidx.room.*
import com.myapps.simpleweatherapp.ui.addalarm.RepeatDay
import com.google.gson.Gson

@Entity
data class Weather(
    @PrimaryKey
    val id: Int,
    val lat: Float,
    val lng: Float,
    val lastUpdate: Long,
    val temp: Float,
    val max_temp: Float,
    val min_temp: Float,
    val feels_like: Float,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Float,
    val uvi: Float,
    val clouds: Int,
    val visibility: Int,
    val wind_speed: Float,
    val Desc: String,
    val icon: String
)

/**
 * day
 */
@Entity
data class Day(
    @PrimaryKey
    val index: Int,
    val date: Long,
    val max_temp: Float,
    val min_temp: Float,
    val desc: String,
    val icon: String
)


/**
 * Hour
 */
@Entity
data class Hour(
    @PrimaryKey
    val index: Int,
    val date: Long,
    val temp: Float,
    val icon: String,
    val desc: String
)

/**
 * Favourite City
 */
@Entity(primaryKeys = ["lat", "lng"])
data class City(
    val lat: Float,
    val lng: Float,
    val location: String,
    val date: Long,
    val temp: Float,
    val max_temp: Float,
    val min_temp: Float,
    val icon: String
)

/**
 * Weather Alarm
 */
@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val alarmId: Long = 0,
    val alarmName: String,
    val start: Long,
    val endHour: Int,
    val endMinute: Int,
    val alarmType: String,
    val repeatDays: List<RepeatDay>,
    val isActive: Boolean
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Alarm::class,
        parentColumns = ["alarmId"],
        childColumns = ["id"]
    )]
)


class Converters {
    @TypeConverter
    fun listToJson(value: List<RepeatDay>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) =
        Gson().fromJson(value, Array<RepeatDay>::class.java).toList()
}