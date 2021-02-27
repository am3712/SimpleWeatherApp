package com.example.simpleweatherapp.ui.weather

import android.app.Application
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.simpleweatherapp.data.local.City
import com.example.simpleweatherapp.data.local.getDatabase
import com.example.simpleweatherapp.data.repository.Repository
import com.example.simpleweatherapp.data.repository.UserPreferencesRepository
import com.example.simpleweatherapp.utils.getLocationName
import com.example.simpleweatherapp.utils.splitLocationDetails
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class WeatherViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    private val userPreferencesRepository =
        UserPreferencesRepository.getInstance(app.applicationContext)

    val homeName = userPreferencesRepository.homeLocationName.asLiveData()
        .map {
            if (it.isNotEmpty())
                splitLocationDetails(it)
            else
                ""
        }


    val userPreferences = userPreferencesRepository.userPreferencesFlow.asLiveData()

    private var homeLocationLatLng: LatLng? = null

    init {
        Timber.i("WeatherViewModel created")
    }

    private val database = getDatabase(app)
    private val repository = Repository(database)

    //Live Data of network error
    val networkRequest = repository.weatherApiStatus

    //Live Data of Main Weather Info
    val weather = repository.weatherLiveData()

    //Live Data of Weather days Info
    val dayList = repository.daysLiveData()

    //Live Data of Weather hours Info
    val hoursList = repository.hoursLiveData()

    //progressbar status
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading get() = _isLoading

    private val _networkStatus = MutableLiveData(true)
    val networkStatus: LiveData<Boolean>
        get() = _networkStatus


    private fun updateWeather() {
        viewModelScope.launch {
            Timber.i("userPreferences.value : ${userPreferences.value?.lang}")
            val lang = if (userPreferences.value?.lang.isNullOrEmpty()) {
                userPreferencesRepository.homeLocationName.first()
            } else
                userPreferences.value?.lang

            Timber.i("val lang : $lang")
            repository.fetchWeather(
                getHomeLocationLatLng()!!.latitude.toFloat(),
                getHomeLocationLatLng()!!.longitude.toFloat(),
                lang!!
            )
            _isLoading.value = false
        }
    }

    fun fetchWeatherAction() {
        _isLoading.value = true
        if (isNetworkConnected()) {
            Timber.i("Network Source Found")
            _networkStatus.value = true
            updateWeather()
        } else {
            Timber.i("No Network Source Found")
            _networkStatus.value = false
            _isLoading.value = false
        }
    }


    private fun isNetworkConnected(): Boolean {
        val connectivityManager = ContextCompat.getSystemService(
            app,
            ConnectivityManager::class.java
        ) as ConnectivityManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            networkCapabilities != null &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            activeNetwork?.isConnectedOrConnecting == true
        }
    }


    fun clearStatus() {
        repository.changeWeatherRequestStatus()
    }


    override fun onCleared() {
        super.onCleared()
        Timber.i("WeatherViewModel Cleared")
    }


    private suspend fun getHomeLocationLatLng(): LatLng? {
        if (homeLocationLatLng == null)
            homeLocationLatLng = userPreferencesRepository.homeLocationLatLng.first()
        return homeLocationLatLng
    }

    fun updateHomeLocationName() {
        viewModelScope.launch {
            Dispatchers.IO
            val geoCoder = Geocoder(app, Locale.getDefault())
            getLocationName(
                geoCoder,
                getHomeLocationLatLng()?.latitude,
                getHomeLocationLatLng()?.longitude
            )?.let { userPreferencesRepository.updateHomeLocationName(it) }

            val cities = repository.citiesLiveData().asFlow().first()
            if (!cities.isNullOrEmpty()) {
                //update cities location name
                cities.forEach { city ->
                    val newCityLocationName =
                        getLocationName(geoCoder, city.lat.toDouble(), city.lng.toDouble())
                    if (newCityLocationName != null)
                        repository.updateCity(
                            City(
                                lat = city.lat,
                                lng = city.lng,
                                location = newCityLocationName,
                                date = city.date,
                                temp = city.temp,
                                max_temp = city.max_temp,
                                min_temp = city.min_temp,
                                icon = city.icon
                            )
                        )
                }
            }
        }
    }
}
