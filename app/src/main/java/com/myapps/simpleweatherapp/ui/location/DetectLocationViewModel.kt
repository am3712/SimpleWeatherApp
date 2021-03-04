package com.myapps.simpleweatherapp.ui.location

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapps.simpleweatherapp.data.local.getDatabase
import com.myapps.simpleweatherapp.data.repository.LocationOptions
import com.myapps.simpleweatherapp.data.repository.Repository
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.utils.disableReceiveAlerts
import com.myapps.simpleweatherapp.utils.enableReceiveAlerts
import com.myapps.simpleweatherapp.utils.getLocationName
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*


class DetectLocationViewModel(private val app: Application) : AndroidViewModel(app) {
    private val userPreferencesRepository =
        UserPreferencesRepository.getInstance(app.applicationContext)

    private val repository = Repository(getDatabase(app.applicationContext))

    private val geoCoder = Geocoder(app.applicationContext, Locale.getDefault())

    private val _latLon = MutableLiveData<LatLng>()
    val latLon
        get() = _latLon

    private val _placeDisplayName = MutableLiveData<String>()
    val placeDisplayName
        get() = _placeDisplayName

    private var retryCount = 0

    private val _showDialog = MutableLiveData<Boolean>()
    val showDialog
        get() = _showDialog


    fun setLatLon(coordinates: LatLng) {
        _latLon.value = coordinates
        fetchPlaceInfo()
    }

    suspend fun updateHomeLocation() {
        latLon.value?.let { location ->
            Timber.i("location : $location")
            placeDisplayName.value?.let { cityName ->
                Timber.i("cityName : $cityName")
                Timber.i("location lat : ${location.latitude}")
                Timber.i("location lng : ${location.longitude}")
                userPreferencesRepository.updateHomeLocation(location, cityName)
                userPreferencesRepository.updateCurrentOption(LocationOptions.FROM_MAP.name)
                repository.deleteCurrentCache()
            }
        }
    }

    suspend fun updateAlertsStatus(status: Boolean) {
        userPreferencesRepository.updateAlertsStatus(status)
        userPreferencesRepository.updateCurrentOption(LocationOptions.FROM_MAP.name)

        //start or stop receive alerts
        if (status)
            enableReceiveAlerts(app)
        else
            disableReceiveAlerts(app)
    }

    fun fetchAlerts() {
        disableReceiveAlerts(app)
        enableReceiveAlerts(app)
    }

    private fun fetchPlaceInfo() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val placeName =
                    getLocationName(geoCoder, latLon.value?.latitude, latLon.value?.longitude)
                _placeDisplayName.postValue(placeName ?: "")
            }
        }
    }

    fun retryFetchPlaceInfo() {
        if (retryCount >= 5) {
            Timber.i("Retry count increased to max")
            retryCount = 0
            return
        }
        fetchPlaceInfo()
        Timber.i("retryCount : $retryCount")
        retryCount++
    }

    fun showDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }
}