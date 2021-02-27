package com.example.simpleweatherapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.simpleweatherapp.data.local.getDatabase
import com.example.simpleweatherapp.data.remote.Alerts
import com.example.simpleweatherapp.data.repository.Repository
import com.example.simpleweatherapp.data.repository.UserPreferencesRepository
import com.example.simpleweatherapp.utils.setAlertWorker
import com.example.simpleweatherapp.utils.setupRecurringWorkBuilder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import timber.log.Timber

class RefreshAlertsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshAlertsWorker"
    }

    override suspend fun doWork(): Result {
        Timber.i("RefreshAlertsWorker Started")
        return try {
            val repository = Repository(getDatabase(applicationContext))

            val preferencesRepository = UserPreferencesRepository.getInstance(applicationContext)

            val homeLocationLatLng: LatLng = preferencesRepository.homeLocationLatLng.first()

            val alertsPreferences = preferencesRepository.weatherAlertsFlow.first()

            val languagePreferences = preferencesRepository.languageFlow.first()

            // if user disable alerts
            if (!alertsPreferences)
                return Result.success()

            repository.fetchWeather(
                homeLocationLatLng.latitude.toFloat(),
                homeLocationLatLng.longitude.toFloat(),
                languagePreferences
            )

            if (!repository.alerts.value.isNullOrEmpty()) {
                Timber.i("alerts not Empty")
                Timber.i("alerts size : ${repository.alerts.value!!.size}")
                repository.alerts.value!!.forEach { alert ->
                    addAlertWorkManager(alert)
                    delay(2000)
                    Timber.i("alert : $alert")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Timber.i("Exception: $e")
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun addAlertWorkManager(alert: Alerts) {
        val builder = setupRecurringWorkBuilder(alert)
        setAlertWorker(applicationContext, builder, "${alert.event}_${alert.sender_name}")
    }
}
