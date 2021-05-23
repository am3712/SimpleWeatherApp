package com.myapps.simpleweatherapp.ui.firstopen

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapps.simpleweatherapp.data.repository.LocationOptions
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.utils.LocationUpdatesUseCase
import com.myapps.simpleweatherapp.utils.disableReceiveAlerts
import com.myapps.simpleweatherapp.utils.enableReceiveAlerts
import com.myapps.simpleweatherapp.utils.getLocationName
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class StartUpViewModel(private val app: Application) : AndroidViewModel(app) {

    private val userPreferencesRepository =
        UserPreferencesRepository.getInstance(app.applicationContext)

    private val geoCoder = Geocoder(app.applicationContext, Locale.getDefault())


    private val _permissionStatus = MutableLiveData<Boolean>()
    val permissionStatus
        get() = _permissionStatus

    private val _loading = MutableLiveData<Boolean>()
    val loading
        get() = _loading

    private val _finishAndNavigate = MutableLiveData<Boolean>()
    val finishAndNavigate
        get() = _finishAndNavigate

    private val _locationName = MutableLiveData<String>()
    val locationName
        get() = _locationName

    private lateinit var locationLatLng: LatLng

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app.applicationContext)

    fun subscribeLocation() {
        if (ActivityCompat.checkSelfPermission(
                app.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                app.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _permissionStatus.value = false
            return
        }
        _loading.value = true
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        val locationUpdatesUseCase = LocationUpdatesUseCase(client = fusedLocationProviderClient)
        viewModelScope.launch {
            val loc = locationUpdatesUseCase.fetchUpdates().firstOrNull()
            if (loc != null)
                withContext(Dispatchers.IO) {
                    Timber.i("latitude :  ${loc.latitude} & longitude :  ${loc.longitude}")
                    val locationName = getLocationName(geoCoder, loc.latitude, loc.longitude)
                    if (locationName != null)
                        locationLatLng = loc
                    _loading.postValue(false)
                    _locationName.postValue(locationName ?: "")
                }
            else {
                _loading.value = false
                _locationName.postValue("")
                Timber.i("Location information isn't available.")
            }
        }
    }

    fun updateAlertsStatus(status: Boolean) {
        viewModelScope.launch {
            //needed
            _loading.value = true
            updateHomeLocationInfo()
            userPreferencesRepository.updateAlertsStatus(status)
            userPreferencesRepository.updateCurrentOption(LocationOptions.CURRENT_LOCATION.name)

            //start or stop receive alerts
            if (!status)
                disableReceiveAlerts(app)
            else
                enableReceiveAlerts(app)

            _loading.value = false
            finishAndNavigate.value = true
        }
    }

    private fun updateHomeLocationInfo() {
        viewModelScope.launch {
            userPreferencesRepository.updateHomeLocation(
                locationLatLng,
                locationName.value!!
            )
        }
    }

    fun resetPermissionStatus() {
        _permissionStatus.value = true
    }

    fun resetLocation(){
        _locationName.value = "RESET"
    }
}