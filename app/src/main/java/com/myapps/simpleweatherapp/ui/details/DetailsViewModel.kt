package com.myapps.simpleweatherapp.ui.details

import android.app.Application
import androidx.lifecycle.*
import com.myapps.simpleweatherapp.data.local.Day
import com.myapps.simpleweatherapp.data.local.Hour
import com.myapps.simpleweatherapp.data.local.Weather
import com.myapps.simpleweatherapp.data.local.getDatabase
import com.myapps.simpleweatherapp.data.remote.WeatherResponse
import com.myapps.simpleweatherapp.data.remote.asDatabaseCurrentWeatherModel
import com.myapps.simpleweatherapp.data.remote.asDomainWeatherDaysModel
import com.myapps.simpleweatherapp.data.remote.asDomainWeatherHoursModel
import com.myapps.simpleweatherapp.data.repository.Repository
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.utils.splitLocationDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DetailsViewModel(
    private val lat: Float,
    private val lng: Float,
    private val locName: String,
    app: Application
) :
    AndroidViewModel(app) {


    private val userPreferencesRepository =
        UserPreferencesRepository.getInstance(app.applicationContext)

    val homeName =
        if (locName.isNotEmpty())
            splitLocationDetails(locName)
        else
            ""

    val userPreferences = userPreferencesRepository.userPreferencesFlow.asLiveData()

    private val database = getDatabase(app)
    private val repository = Repository(database)


    //Live Data of network error
    val networkRequest = repository.weatherApiStatus

    private val _weather = MutableLiveData<Weather>()
    val weather
        get() = _weather

    //Live Data of Weather days Info
    private val _dayList = MutableLiveData<List<Day>>()
    val dayList
        get() = _dayList

    //Live Data of Weather hours Info
    private val _hoursList = MutableLiveData<List<Hour>>()
    val hoursList
        get() = _hoursList


    //progressbar status
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        fetchCity()
    }

    private fun fetchCity() {
        viewModelScope.launch {
            Timber.i("userPreferences.value?.lang :${userPreferences.value?.lang}")
            Timber.i("lat :$lat")
            Timber.i("lng :$lng")
            _isLoading.value = true
            val apiData: WeatherResponse? = if (userPreferences.value?.lang.isNullOrEmpty())
                repository.getWeatherApiData(
                    lat,
                    lng,
                    userPreferencesRepository.userPreferencesFlow.first().lang
                )
            else
                repository.getWeatherApiData(lat, lng, userPreferences.value?.lang!!)
            if (apiData != null) {
                withContext(Dispatchers.IO) {
                    //weather data
                    val wd = apiData.asDatabaseCurrentWeatherModel()
                    _weather.postValue(wd)

                    //days data
                    val dd = apiData.asDomainWeatherDaysModel()
                    _dayList.postValue(dd)

                    //hours data
                    val hd = apiData.asDomainWeatherHoursModel()
                    _hoursList.postValue(hd)
                    Timber.i("Status.SUCCESS")
                }
                repository.updateCity(apiData, locName)
            }
            _isLoading.value = false
        }
    }

    fun retryFetchData() {
        fetchCity()
    }

    fun clearStatus() {
        repository.changeWeatherRequestStatus()
    }


    //Factory
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val lat: Float,
        private val lng: Float,
        private val loc: String,
        private val app: Application
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DetailsViewModel(lat, lng, loc, app) as T
        }
    }
}