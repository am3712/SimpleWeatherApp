package com.myapps.simpleweatherapp.ui.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.myapps.simpleweatherapp.data.local.Alarm
import com.myapps.simpleweatherapp.data.local.getDatabase
import com.myapps.simpleweatherapp.data.repository.LocationOptions
import com.myapps.simpleweatherapp.data.repository.Repository
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.ui.alarms.getCorrectStart
import com.myapps.simpleweatherapp.utils.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class SettingsViewModel(private val app: Application) : AndroidViewModel(app) {
    init {
        Timber.i("SettingsViewModel created")
    }


    private val userPreferencesRepository =
        UserPreferencesRepository.getInstance(app.applicationContext)

    private val repository = Repository(getDatabase(app.applicationContext))

    val userPreferences = userPreferencesRepository.userPreferencesFlow.asLiveData()

    val userLocationNamePreferences = userPreferencesRepository.homeLocationName.asLiveData()

    val alarmsPreferences = userPreferencesRepository.weatherAlarmsFlow.asLiveData()

    val alertsPreferences = userPreferencesRepository.weatherAlertsFlow.asLiveData()

    val currentLocationLatLng = userPreferencesRepository.homeLocationLatLng


    private val geoCoder = Geocoder(app.applicationContext, Locale.getDefault())


    private val _permissionStatus = MutableLiveData<Boolean>()
    val permissionStatus
        get() = _permissionStatus

    private val _startFetching = MutableLiveData<Boolean>()
    val startFetching
        get() = _startFetching

    private val _locationName = MutableLiveData<String>()
    val locationName
        get() = _locationName

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
        _startFetching.value = true
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
                    if (locationName != null) {
                        userPreferencesRepository.updateHomeLocation(loc, locationName)

                        //delete current cache if current location option not equal LocationOptions.CURRENT_LOCATION
                        if (userPreferences.value?.currentOption != LocationOptions.CURRENT_LOCATION)
                            repository.deleteCurrentCache()
                    }
                    _startFetching.postValue(false)
                    _locationName.postValue(locationName ?: "")
                }
            else {
                _startFetching.value = false
                _locationName.value = ""
                Timber.i("Location information isn't available.")
            }
        }
    }

    fun resetPermissionStatus() {
        _permissionStatus.value = true
    }


    fun saveToDataStore(key: String, value: String) {
        viewModelScope.launch {
            when (key) {
                "temperature_unit" -> userPreferencesRepository.updateTemperatureUnit(value)
                "wind_speed_unit" -> userPreferencesRepository.updateWindSpeedUnit(value)
            }
        }
    }

    fun saveToDataStore(key: String, value: Boolean) {
        viewModelScope.launch {
            Timber.i("saveToDataStore Boolean called")
            when (key) {
                "alarms_key" -> userPreferencesRepository.updateAlarmsStatus(value)
                "alerts_key" -> userPreferencesRepository.updateAlertsStatus(value)
            }
        }
    }


    fun changeAlerts(value: Boolean) {
        Timber.i("changeAlerts : $value ")
        if (!value)
            disableReceiveAlerts(app)
        else
            enableReceiveAlerts(app)
    }

    fun changeAlarms(value: Boolean) {
        Timber.i("changeAlarms : $value ")
        if (value) {
            createAlarmsChannel(app)
            activeAllAlarms()
        } else {
            cancelAlarmsWorkManager(app)
            disableStatusOfAlarms()
        }
    }

    private fun activeAllAlarms() {
        viewModelScope.launch {
            repository.getAlarmsSuspend().asFlow().collect { alarm ->
                val updatedAlarm = getUpdatedAlarm(alarm, true)
                addAlarmWorkManager(app, updatedAlarm.alarmId, updatedAlarm)
                repository.addAlarm(updatedAlarm)
            }
        }
    }


    private fun disableStatusOfAlarms() {
        viewModelScope.launch {
            repository.getAlarmsSuspend().asFlow().collect { alarm ->
                repository.addAlarm(getUpdatedAlarm(alarm, false))
            }
        }
    }

    fun updateLanguageSettings(lang: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLangSetting(lang)
            Repository(getDatabase(app)).deleteCurrentCache()
        }
    }

    fun updateLocationOptions(locationOptions: LocationOptions) {
        viewModelScope.launch {
            userPreferencesRepository.updateCurrentOption(locationOptions.name)
        }
    }

    fun resetLocation() {
        _locationName.value = "RESET"
    }
}

fun getUpdatedAlarm(alarm: Alarm, active: Boolean) = Alarm(
    alarmId = alarm.alarmId,
    alarmName = alarm.alarmName,
    start = if (active) getCorrectStart(alarm.repeatDays, alarm.start) else alarm.start,
    endHour = alarm.endHour,
    endMinute = alarm.endMinute,
    alarmType = alarm.alarmType,
    repeatDays = alarm.repeatDays,
    isActive = active
)