package com.myapps.simpleweatherapp.data.remote.api

import com.myapps.simpleweatherapp.data.remote.Constant
import com.myapps.simpleweatherapp.data.remote.Constant.APP_ID
import com.myapps.simpleweatherapp.data.remote.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET(Constant.WEATHER_PATH)
    suspend fun fetchWeatherDate(
        @Query("lat") lat: Float,
        @Query("lon") lon: Float,
        @Query("exclude") exclude: String = "minutely",
        @Query("APPID") key: String = APP_ID,
        @Query("units") unit: String = "metric",
        @Query("lang") lang: String = "en"
    ): WeatherResponse
}