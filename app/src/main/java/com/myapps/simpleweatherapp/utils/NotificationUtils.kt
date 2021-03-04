package com.myapps.simpleweatherapp.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.myapps.simpleweatherapp.MainActivity
import com.myapps.simpleweatherapp.R
import timber.log.Timber

/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(
    title: String,
    messageBody: String,
    messageDescription: String?,
    time: String = "",
    channelId: String,
    applicationContext: Context
) {

    Timber.i("title : $title")
    Timber.i("messageBody : $messageBody")
    Timber.i("messageDescription : $messageDescription")
    Timber.i("time : $time")


    //notification id
    val notificationId = "${title}_${messageBody}".hashCode()

    // Create the content intent for the notification, which launches
    // this activity

    // create intent
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

    // create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        notificationId,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        channelId
    )

        .setSmallIcon(R.drawable.ic_baseline_cloud_24)
        .apply {
            if (channelId == applicationContext.getString(R.string.weather_alerts_channel_id))
//                setContentTitle("${applicationContext.getString(R.string.alerts_notification_text)} $title")
                setContentTitle("Alert: $title")
            else
//                setContentTitle("${applicationContext.getString(R.string.alarms_notification_text)} $title")
                setContentTitle("Alarm: $title")
        }

        .apply {
            if (messageBody.isNotEmpty())
                setContentText(messageBody)

            if (!messageDescription.isNullOrEmpty())
                setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        if (messageBody.isNotEmpty())
                            "from : $messageBody\n$messageDescription"
                        else
                            messageDescription
                    )
                )
            else
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(if (messageBody.isNotEmpty()) "$messageBody\n$time" else time)
                )
        }


        // set content intent
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)


        // set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
    // call notify
    notify(notificationId, builder.build())

}

private fun createChannel(
    app: Application,
    channelId: String,
    channelName: String,
    description: String
) {
    // START create a channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            // importance
            NotificationManager.IMPORTANCE_HIGH
        )// disable badges for this channel
            .apply {
                setShowBadge(false)
            }


        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.CYAN
        notificationChannel.enableVibration(true)
        notificationChannel.description = description
        val notificationManager = app.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

fun createAlertsChannel(app: Application) {
    createChannel(
        app,
        app.getString(R.string.weather_alerts_channel_id),
        app.getString(R.string.weather_alerts_channel_name),
        app.getString(R.string.weather_alerts_channel_description)
    )
}

fun createAlarmsChannel(app: Application) {
    createChannel(
        app,
        app.getString(R.string.weather_alarms_channel_id),
        app.getString(R.string.weather_alarms_channel_name),
        app.getString(R.string.weather_alarms_channel_description)
    )
}
