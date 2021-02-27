package com.example.simpleweatherapp.utils

data class WeatherApiStatus(val status: Status, val message: String?) {

    companion object {

        fun success(): WeatherApiStatus {
            return WeatherApiStatus(Status.SUCCESS, null)
        }

        fun error(msg: String): WeatherApiStatus {
            return WeatherApiStatus(Status.ERROR, msg)
        }

        fun loading(): WeatherApiStatus {
            return WeatherApiStatus(Status.LOADING, null)
        }

        fun clear(): WeatherApiStatus {
            return WeatherApiStatus(Status.CLEAR, null)
        }
    }

}