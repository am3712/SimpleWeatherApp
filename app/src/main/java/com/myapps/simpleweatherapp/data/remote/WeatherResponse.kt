package com.myapps.simpleweatherapp.data.remote

import com.myapps.simpleweatherapp.data.local.City
import com.myapps.simpleweatherapp.data.local.Day
import com.myapps.simpleweatherapp.data.local.Hour
import com.myapps.simpleweatherapp.data.local.Weather
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*


data class WeatherResponse(
	@SerializedName("lat") val lat: Float,
	@SerializedName("lon") val lon: Float,
	@SerializedName("timezone") val timezone: String,
	@SerializedName("current") val current: CurrentResponse,
	@SerializedName("hourly") val hourly: List<HourlyApiResponse>,
	@SerializedName("daily") val daily: List<DailyApiResponse>,
	@SerializedName("alerts") val alerts: List<Alerts>
)

data class CurrentResponse(
	@SerializedName("dt") val dt: Long,
	@SerializedName("temp") val temp: Float,
	@SerializedName("feels_like") val feels_like: Float,
	@SerializedName("pressure") val pressure: Int,
	@SerializedName("humidity") val humidity: Int,
	@SerializedName("dew_point") val dew_point: Float,
	@SerializedName("uvi") val uvi: Float,
	@SerializedName("clouds") val clouds: Int,
	@SerializedName("visibility") val visibility: Int,
	@SerializedName("wind_speed") val wind_speed: Float,
	@SerializedName("weather") val weather: List<WeatherApiResponse>
)

data class WeatherApiResponse(
	@SerializedName("id") val id: Int,
	@SerializedName("main") val main: String,
	@SerializedName("description") val description: String,
	@SerializedName("icon") val icon: String
)

data class TempApiResponse(
	@SerializedName("day") val day: Float,
	@SerializedName("min") val min: Float,
	@SerializedName("max") val max: Float,
	@SerializedName("night") val night: Float,
	@SerializedName("eve") val eve: Float,
	@SerializedName("morn") val morn: Float
)

data class HourlyApiResponse(
	@SerializedName("dt") val dt: Long,
	@SerializedName("temp") val temp: Float,
	@SerializedName("weather") val weather: List<WeatherApiResponse>
)

data class FeelsLikeApiResponse(
	@SerializedName("day") val day: Float,
	@SerializedName("night") val night: Float,
	@SerializedName("eve") val eve: Float,
	@SerializedName("morn") val morn: Float
)

data class DailyApiResponse(
	@SerializedName("dt") val dt: Long,
	@SerializedName("temp") val temp: TempApiResponse,
	@SerializedName("feels_like") val feels_like: FeelsLikeApiResponse,
	@SerializedName("weather") val weather: List<WeatherApiResponse>
)

data class Alerts(

	@SerializedName("sender_name") val sender_name: String,
	@SerializedName("event") val event: String,
	@SerializedName("start") val start: Int,
	@SerializedName("end") val end: Int,
	@SerializedName("description") val description: String
)

/**
 * Convert Network results to database objects
 */
//"EE, dd/M hh:mm a"
fun WeatherResponse.asDatabaseCurrentWeatherModel(): Weather {
    return Weather(
		id = 0,
		lat = lat,
		lng = lon,
		lastUpdate = current.dt * 1000,
		clouds = current.clouds,
		temp = current.temp,
		max_temp = daily[0].temp.max,
		min_temp = daily[0].temp.min,
		Desc = current.weather[0].description,
		dew_point = current.dew_point,
		feels_like = current.feels_like,
		humidity = current.humidity,
		icon = current.weather[0].icon,
		pressure = current.pressure,
		uvi = current.uvi,
		visibility = current.visibility,
		wind_speed = current.wind_speed
	)
}

fun WeatherResponse.asDomainWeatherDaysModel(): List<Day> {
    return daily.subList(1, 8).mapIndexed { index: Int, day: DailyApiResponse ->
        Day(
			index = index,
			date = day.dt * 1000,
			max_temp = day.temp.max,
			min_temp = day.temp.min,
			icon = day.weather[0].icon,
			desc = day.weather[0].description
		)
    }
}

fun WeatherResponse.asDomainWeatherCityModel(location: String): City {
    return City(
		lat = lat, lng = lon,
		location = location,
		date = current.dt * 1000,
		temp = current.temp,
		max_temp = daily[0].temp.max,
		min_temp = daily[0].temp.min,
		icon = daily[0].weather[0].icon
	)
    //getDate(timezone, current.dt, "EE, dd/M hh:mm a")
}

fun WeatherResponse.asDomainWeatherHoursModel(): List<Hour> {
    return hourly.subList(0, 24).mapIndexed { index: Int, hour: HourlyApiResponse ->
        Hour(
			index = index,
			date = hour.dt * 1000,
			temp = hour.temp,
			icon = hour.weather[0].icon,
			desc = hour.weather[0].description
		)
    }
}


fun getDate(timezone: String, time: Long, pattern: String): String {
    val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(timezone)
    val date = Date(time * 1000)
    return formatter.format(date)
}

fun atEndOfDay(date: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.time.time = date
    calendar[Calendar.HOUR_OF_DAY] = 23
    calendar[Calendar.MINUTE] = 59
    calendar[Calendar.SECOND] = 59
    calendar[Calendar.MILLISECOND] = 999
    return calendar.time.time
}