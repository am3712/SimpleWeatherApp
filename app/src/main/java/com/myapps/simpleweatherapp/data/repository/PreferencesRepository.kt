package com.myapps.simpleweatherapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

private const val USER_PREFERENCES_NAME = "user_preferences"

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)


enum class TemperatureUnit(val degreeSign: String) {
    KELVIN("\u212A"),
    CELSIUS("\u2103"),
    FAHRENHEIT("\u2109");

    fun cToF(c: Float) = ((9 / 5.0 * c) + 32).roundToInt()
    fun cToK(c: Float) = (c + 273.15).roundToInt()
}

enum class WindSpeedUnit {
    METER_SEC,
    MILES_HOUR;
}

enum class LocationOptions {
    CURRENT_LOCATION,
    FROM_MAP,
    NOT_SET
}

data class UserPreferences(
    val windSpeedUnit: WindSpeedUnit,
    val temperatureUnit: TemperatureUnit,
    val currentOption: LocationOptions,
    var lang: String
)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository private constructor(context: Context) {

    object PreferencesKeys {
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val CURRENT_LOCATION_OPTION = stringPreferencesKey("current_location_option")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
        val LOCATION_LAT = floatPreferencesKey("location_lat")
        val LOCATION_LNG = floatPreferencesKey("location_lng")
        val ALARMS_KEY = booleanPreferencesKey("alarms_key")
        val ALERTS_KEY = booleanPreferencesKey("alerts_key")
        val USER_LANG_KEY = stringPreferencesKey("user_lang_key")
    }

    private val dataStore = context.dataStore

    val homeLocationLatLng: Flow<LatLng> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        // Get home location lat:
        val locLat = preferences[PreferencesKeys.LOCATION_LAT] ?: 91.0f

        // Get home location lng:
        val locLng = preferences[PreferencesKeys.LOCATION_LNG] ?: 181.0f

        LatLng(locLat.toDouble(), locLng.toDouble())
    }
    val languageFlow: Flow<String> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { it[PreferencesKeys.USER_LANG_KEY] ?: Locale.getDefault().language }

    val temperatureUnitFlow: Flow<TemperatureUnit> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map {
        TemperatureUnit.valueOf(
            it[PreferencesKeys.TEMPERATURE_UNIT] ?: TemperatureUnit.CELSIUS.name
        )
    }

    val weatherAlarmsFlow: Flow<Boolean> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { it[PreferencesKeys.ALARMS_KEY] ?: true }

    val weatherAlertsFlow: Flow<Boolean> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { it[PreferencesKeys.ALERTS_KEY] ?: false }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->

            val temperatureUnit =
                TemperatureUnit.valueOf(
                    preferences[PreferencesKeys.TEMPERATURE_UNIT] ?: TemperatureUnit.CELSIUS.name
                )

            // Get wind speed unit:
            val windSpeedUnit =
                WindSpeedUnit.valueOf(
                    preferences[PreferencesKeys.WIND_SPEED_UNIT] ?: WindSpeedUnit.METER_SEC.name
                )

            // Get location option:
            val currentLocationOption =
                LocationOptions.valueOf(
                    preferences[PreferencesKeys.CURRENT_LOCATION_OPTION]
                        ?: LocationOptions.NOT_SET.name
                )

            //get lang
            val lang = preferences[PreferencesKeys.USER_LANG_KEY] ?: Locale.getDefault().language

            UserPreferences(
                windSpeedUnit = windSpeedUnit,
                temperatureUnit = temperatureUnit,
                currentOption = currentLocationOption,
                lang = lang
            )
        }


    val homeLocationName = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { it[PreferencesKeys.LOCATION_NAME] ?: "" }


    suspend fun updateTemperatureUnit(temperatureUnit: String) {
        dataStore.edit { it[PreferencesKeys.TEMPERATURE_UNIT] = temperatureUnit }
    }

    suspend fun updateWindSpeedUnit(windSpeedUnit: String) {
        dataStore.edit { it[PreferencesKeys.WIND_SPEED_UNIT] = windSpeedUnit }
    }

    suspend fun updateHomeLocation(location: LatLng, cityName: String) {
        Timber.i("updateHomeLocation called")
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_LAT] = location.latitude.toFloat()
            preferences[PreferencesKeys.LOCATION_LNG] = location.longitude.toFloat()
            preferences[PreferencesKeys.LOCATION_NAME] = cityName
            Timber.i("updateHomeLocation finished")
        }
    }

    suspend fun updateHomeLocationName(cityName: String) {
        Timber.i("updateHomeLocationName called")
        dataStore.edit { it[PreferencesKeys.LOCATION_NAME] = cityName }
    }

    suspend fun updateCurrentOption(currentOption: String) {
        dataStore.edit { it[PreferencesKeys.CURRENT_LOCATION_OPTION] = currentOption }
    }

    suspend fun updateAlarmsStatus(status: Boolean) {
        dataStore.edit { it[PreferencesKeys.ALARMS_KEY] = status }
    }

    suspend fun updateAlertsStatus(status: Boolean) {
        dataStore.edit { it[PreferencesKeys.ALERTS_KEY] = status }
    }

    suspend fun updateLangSetting(lang: String) {
        dataStore.edit { it[PreferencesKeys.USER_LANG_KEY] = lang }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = UserPreferencesRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
