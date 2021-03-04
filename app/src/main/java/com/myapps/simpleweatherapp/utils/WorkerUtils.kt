package com.myapps.simpleweatherapp.utils

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.*
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.data.local.Alarm
import com.myapps.simpleweatherapp.data.remote.Alerts
import com.myapps.simpleweatherapp.worker.AlarmWorker
import com.myapps.simpleweatherapp.worker.AlertWorker
import com.myapps.simpleweatherapp.worker.RefreshAlertsWorker
import timber.log.Timber
import java.text.DateFormat
import java.util.concurrent.TimeUnit


private fun cancelAlertsWorkManager(app: Application) {
    cancelWorkManager(
        app,
        AlertWorker.ALERTS_TAG,
        app.getString(R.string.weather_alerts_channel_id)
    )
}

fun cancelAlarmsWorkManager(app: Application) {
    cancelWorkManager(
        app,
        AlarmWorker.ALARMS_TAG,
        app.getString(R.string.weather_alarms_channel_id)
    )
}

private fun cancelWorkManager(app: Application, tag: String, channelId: String) {
    // cancel work manager for all incoming alerts
    WorkManager.getInstance(app.applicationContext)
        .cancelAllWorkByTag(tag)

    // cancel all current notification
    val notificationManager =
        ContextCompat.getSystemService(
            app.applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        notificationManager.deleteNotificationChannel(channelId)
    else
        notificationManager.cancelAll()
}


fun setupRecurringWorkBuilder(alert: Alerts): OneTimeWorkRequest.Builder {
    Timber.i("setupRecurringWorkBuilder event : ${alert.event}")
    Timber.i("setupRecurringWorkBuilder sender_name : ${alert.sender_name}")
    Timber.i("setupRecurringWorkBuilder description : ${alert.description}")
    val data = Data.Builder()
        .putString(AlertWorker.ALERT_EVENT, alert.event)
        .putString(AlertWorker.ALERT_LOCATION, alert.sender_name)
        .putString(AlertWorker.ALERT_DESCRIPTION, alert.description)
        .putString(AlertWorker.ALERTS_TIME, getTime(alert.start, alert.end))
        .build()
    Timber.i(
        "alert start time is : ${
            DateFormat.getDateTimeInstance().format(alert.start * 1000L)
        }"
    )
    val delay = alert.start * 1000L - System.currentTimeMillis() - AlertWorker.MINUTE_10
    Timber.i("delay is : $delay")

    return OneTimeWorkRequestBuilder<AlertWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag(AlertWorker.ALERTS_TAG)
}

fun getTime(start: Int, end: Int): String {
    return "from: ${
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(start * 1000L)
    }\nto: ${
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(end * 1000L)
    }"
}

fun setupRecurringWorkBuilder(alarm: Alarm): PeriodicWorkRequest {

    val mapAlarmRepeatable = alarm.repeatDays.map { "${it.dayIndex}" to it.checked }.toMap()

    Timber.i("alarm.alarmName : ${alarm.alarmName}")

    val data = Data.Builder()
        .putString(AlarmWorker.ALARM_TYPE, alarm.alarmType)
        .putString(AlarmWorker.ALARM_NAME, alarm.alarmName)
        .putInt(AlarmWorker.ALARM_HOUR_END_TIME, alarm.endHour)
        .putInt(AlarmWorker.ALARM_MINUTE_END_TIME, alarm.endMinute)
        .putAll(mapAlarmRepeatable)
        .build()

    val delay = alarm.start - System.currentTimeMillis()

    //check to detect if we setUp Periodic work or on time work
    val gap = if (alarm.detectNumberOfSelectedDays() == 1) 7L else 1L
    Timber.i("Periodic Work with gap $gap days")
    return PeriodicWorkRequestBuilder<AlarmWorker>(gap, TimeUnit.DAYS)
        .setInputData(data)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .addTag(AlarmWorker.ALARMS_TAG)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()
}

fun setAlertWorker(
    context: Context,
    repeatingRequestBuilder: OneTimeWorkRequest.Builder,
    uniqueName: String
) {
    WorkManager
        .getInstance(context.applicationContext)
        .enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.KEEP,
            repeatingRequestBuilder.build()
        )
}

fun setAlarmWorker(
    app: Application,
    repeatingRequest: PeriodicWorkRequest,
    uniqueName: String
) {
    WorkManager
        .getInstance(app.applicationContext)
        .enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            repeatingRequest
        )
}

fun addAlarmWorkManager(app: Application, id: Long, alarm: Alarm) {
    Timber.i("addAlarmWorkManager launch")
    val builder = setupRecurringWorkBuilder(alarm)
    setAlarmWorker(app, builder, id.toString())
}


fun Alarm.detectNumberOfSelectedDays(): Int {
    var counter = 0
    repeatDays.forEach {
        if (it.checked)
            counter++
    }
    return counter
}

fun enableReceiveAlerts(app: Application) {
    createAlertsChannel(app)
    setupRetrieveAlertsRecurringWork(app)
}

fun disableReceiveAlerts(app: Application) {
    //cancel RefreshAlertsWorker
    WorkManager.getInstance(app.applicationContext)
        .cancelUniqueWork(RefreshAlertsWorker.WORK_NAME)

    //cancel alert scheduled
    cancelAlertsWorkManager(app)
}

private fun setupRetrieveAlertsRecurringWork(app: Application) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()


    val repeatingRequest = PeriodicWorkRequestBuilder<RefreshAlertsWorker>(1, TimeUnit.DAYS)
        .setConstraints(constraints)
        .build()

    WorkManager
        .getInstance(app.applicationContext)
        .enqueueUniquePeriodicWork(
            RefreshAlertsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
}