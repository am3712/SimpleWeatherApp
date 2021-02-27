package com.example.simpleweatherapp.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.simpleweatherapp.R
import com.example.simpleweatherapp.utils.sendNotification
import timber.log.Timber


class AlertWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {

    companion object {

        const val ALERT_EVENT = "alertEvent"
        const val ALERT_DESCRIPTION = "alertDescription"
        const val ALERT_LOCATION = "alertLocation"
        const val ALERTS_TAG = "alertsTag"
        const val ALERTS_TIME = "alertsTime"
        const val MINUTE_10 = 600000L

    }

    override fun doWork(): Result {
        return try {
            val alertEvent = inputData.getString(ALERT_EVENT) ?: return Result.failure()
            val alertDescription = inputData.getString(ALERT_DESCRIPTION) ?: return Result.failure()
            val alertLocation = inputData.getString(ALERT_LOCATION) ?: return Result.failure()
            val alertTime = inputData.getString(ALERTS_TIME) ?: return Result.failure()

            Timber.i("alertEvent : $alertEvent")
            Timber.i("alertDescription : $alertDescription")
            Timber.i("alertLocation : $alertLocation")


            // sendNotification
            val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendNotification(
                title = alertEvent,
                messageBody = alertLocation,
                messageDescription = alertDescription,
                time = alertTime,
                channelId = applicationContext.getString(R.string.weather_alerts_channel_id),
                applicationContext = applicationContext
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}


