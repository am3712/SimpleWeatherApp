package com.example.simpleweatherapp.ui.favourite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.simpleweatherapp.data.local.City
import com.example.simpleweatherapp.data.local.getDatabase
import com.example.simpleweatherapp.data.repository.Repository
import com.example.simpleweatherapp.data.repository.UserPreferencesRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavouriteViewModel(app: Application) : AndroidViewModel(app) {

    private val database = getDatabase(app)
    private val repository = Repository(database)
    private var userLanguage: String = "en"
    val temperatureUnit =
        UserPreferencesRepository.getInstance(app).temperatureUnitFlow.asLiveData()

    init {
        viewModelScope.launch {
            userLanguage = UserPreferencesRepository.getInstance(app).languageFlow.first()
        }
    }

    //Live Data of cities addition and deletion
    val cities = repository.citiesLiveData()

    fun addCity(latLng: LatLng, loc: String) {
        viewModelScope.launch {
            repository.insertCity(
                latLng.latitude.toFloat(),
                latLng.longitude.toFloat(),
                loc,
                userLanguage
            )
        }
    }

    fun removeCity(city: City) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }
    }
}