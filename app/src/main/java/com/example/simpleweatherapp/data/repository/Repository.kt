package com.example.simpleweatherapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.simpleweatherapp.data.local.Alarm
import com.example.simpleweatherapp.data.local.City
import com.example.simpleweatherapp.data.local.WeatherDatabase
import com.example.simpleweatherapp.data.remote.*
import com.example.simpleweatherapp.data.remote.api.RetrofitBuilder
import com.example.simpleweatherapp.utils.WeatherApiStatus
import kotlinx.coroutines.yield
import timber.log.Timber
import java.net.UnknownHostException

class Repository(
    private val database: WeatherDatabase
) {

    private val _weatherRequest = MutableLiveData<WeatherApiStatus>()
    val weatherApiStatus: LiveData<WeatherApiStatus>
        get() = _weatherRequest

    fun weatherLiveData() =
        database.weatherDao.getCurrentWeather()

    fun daysLiveData() = database.weatherDao.getDays()

    fun hoursLiveData() = database.weatherDao.getHours()

    fun citiesLiveData() = database.weatherDao.getCities()

    private val _alerts = MutableLiveData<List<Alerts>>()
    val alerts: LiveData<List<Alerts>>
        get() = _alerts

    suspend fun fetchWeather(lat: Float, lng: Float, lang: String) {
        val weatherDatFromApi = weatherResponse(lat, lng, lang)
        if (weatherDatFromApi != null) {
            _alerts.postValue(weatherDatFromApi.alerts)

            val cWeather = weatherDatFromApi.asDatabaseCurrentWeatherModel()
            database.weatherDao.insertCurrentWeather(cWeather)

            val weatherDays = weatherDatFromApi.asDomainWeatherDaysModel()
            database.weatherDao.insertAllDays(*weatherDays.toTypedArray())

            val weatherHours = weatherDatFromApi.asDomainWeatherHoursModel()
            database.weatherDao.insertAllHours(*weatherHours.toTypedArray())
        }
    }

    private suspend fun weatherResponse(lat: Float, lng: Float, lang: String): WeatherResponse? {
        _weatherRequest.postValue(WeatherApiStatus.loading())
        try {
            val weatherDatFromApi =
                RetrofitBuilder.apiService.fetchWeatherDate(lat, lng, lang = lang)
            yield()
            _weatherRequest.postValue(WeatherApiStatus.success())
            return weatherDatFromApi
        } catch (e: UnknownHostException) {
            Timber.i("e: UnknownHostException")
            e.printStackTrace()
            _weatherRequest.postValue(WeatherApiStatus.error("No Internet Connection"))
        } catch (e: Exception) {
            e.printStackTrace()
            val message = "WeatherApiStatus error : ${e.message}"
            _weatherRequest.postValue(WeatherApiStatus.error(message))
        }
        return null
    }

    suspend fun insertCity(lat: Float, lng: Float, loc: String, lang: String) {
        val weatherDatFromApi = weatherResponse(lat, lng, lang)
        Timber.i("weatherDatFromApi from insert city : $weatherDatFromApi")
        if (weatherDatFromApi != null)
            database.weatherDao.insertAllCities(weatherDatFromApi.asDomainWeatherCityModel(loc))
    }

    suspend fun getWeatherApiData(lat: Float, lng: Float, lang: String): WeatherResponse? {
        val weatherDatFromApi = weatherResponse(lat, lng, lang)
        if (weatherDatFromApi != null)
            return weatherDatFromApi
        return null
    }

    fun changeWeatherRequestStatus() {
        _weatherRequest.value = WeatherApiStatus.clear()
    }

    suspend fun updateCity(weatherDatFromApi: WeatherResponse, loc: String) {
        database.weatherDao.insertAllCities(weatherDatFromApi.asDomainWeatherCityModel(loc))
    }

    suspend fun addAlarm(alarm: Alarm): List<Long> {
        return database.weatherDao.insertAllAlarms(alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        database.weatherDao.deleteAlarm(alarm)
    }

    suspend fun updateCity(city: City) {
        database.weatherDao.updateCity(city)
    }

    fun getAlarms() = database.weatherDao.getAlarms()
    suspend fun getAlarmsSuspend() = database.weatherDao.getAlarmsSuspend()

    suspend fun deleteCurrentCache() {
        database.weatherDao.deleteCurrentWeather()
        database.weatherDao.deleteHours()
        database.weatherDao.deleteDays()
    }

    suspend fun deleteCity(city: City) {
        database.weatherDao.deleteCity(city)
    }


}
