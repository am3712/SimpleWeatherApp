package com.myapps.simpleweatherapp.worker

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.content.ContextCompat
import androidx.lifecycle.asFlow
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.data.local.getDatabase
import com.myapps.simpleweatherapp.data.repository.Repository
import com.myapps.simpleweatherapp.data.repository.TemperatureUnit
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.ui.alertdialog.DefaultSoundAlarm
import com.myapps.simpleweatherapp.utils.getCurrentDayIndex
import com.myapps.simpleweatherapp.utils.getTime
import com.myapps.simpleweatherapp.utils.sendNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

const val ALARM_TITLE = "AlarmTitle"
const val ALARM_MESSAGE_BODY = "AlarmMessageBody"
const val ALARM_MESSAGE_DESCRIPTION = "AlarmMessageDescription"

class AlarmWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    /**
     * A coroutine-friendly method to do your work.
     */

    private lateinit var title: String
    private lateinit var messageBody: String
    private var messageDescription: String? = null
    private var time: String = ""
    override suspend fun doWork(): Result {
        Timber.i("AlarmWorker Started")

        //get current day index and check if this day scheduled to shaw an alarm else fail
        //is scheduled then check the end time if exceed then fail
        //else complete work normal any other exception retry
        val currentDayIndex = getCurrentDayIndex()
        val mapAlarmRepeatable = inputData.keyValueMap
        if (!(mapAlarmRepeatable["$currentDayIndex"] as Boolean)) {
            //this day not scheduled to work in it
            //then fail
            Timber.i("mapAlarmRepeatable[\"$currentDayIndex\"] as Boolean : false")
            Timber.i("Then -> worker failure")
            return Result.failure()
        }
        Timber.i("mapAlarmRepeatable[\"$currentDayIndex\"] as Boolean : true")
        return try {
            val alarmType = inputData.getString(ALARM_TYPE) ?: return Result.failure()
            val alarmName = inputData.getString(ALARM_NAME) ?: return Result.failure()
            val alarmEndHour = inputData.getInt(ALARM_HOUR_END_TIME, 0)
            val alarmEndMinute = inputData.getInt(ALARM_MINUTE_END_TIME, 0)

            Timber.i("alarmName : $alarmName")

            //val calender represent current time
            val alarmEndTime = Calendar.getInstance()
            alarmEndTime.timeInMillis = System.currentTimeMillis()
            alarmEndTime[Calendar.HOUR_OF_DAY] = alarmEndHour
            alarmEndTime[Calendar.MINUTE] = alarmEndMinute


            //check if current time Exceeded the end time
            if (System.currentTimeMillis() > alarmEndTime.timeInMillis) {
                Timber.i("current time Exceeded the end time that associated to worker!!!")
                Timber.i("Then -> worker failure")
                return Result.failure()
            }

            val repository = Repository(getDatabase(applicationContext))

            val userPreferencesRepository =
                UserPreferencesRepository.getInstance(applicationContext)

            val homeLocation = userPreferencesRepository.homeLocationLatLng.first()

            repository.fetchWeather(
                homeLocation.latitude.toFloat(),
                homeLocation.longitude.toFloat(),
                "en"
            )
            var emptyAlert = true
            if (!repository.alerts.value.isNullOrEmpty()) {
                repository.alerts.value!!.forEach { alert ->
                    //must alert be in rang
                    if (alarmEndTime.timeInMillis < alert.end * 1000L && System.currentTimeMillis() >= alert.start * 1000L) {
                        time = getTime(alert.start, alert.end)
                        Timber.i(
                            "alert Start time is : ${
                                DateFormat.getDateTimeInstance(
                                    DateFormat.MEDIUM,
                                    DateFormat.SHORT
                                ).format(alert.start * 1000L)
                            }"
                        )

                        Timber.i(
                            "alert end time is : ${
                                DateFormat.getDateTimeInstance(
                                    DateFormat.MEDIUM,
                                    DateFormat.SHORT
                                ).format(alert.end * 1000L)
                            }"
                        )
                        title = alert.event
                        messageBody = alert.sender_name
                        messageDescription = alert.description
                        fireNotificationOrAlarm(alarmType)
                        emptyAlert = false
                        delay(2000)
                    }
                }
            }
            if (emptyAlert) {
                time = getTime(
                    (System.currentTimeMillis() / 1000).toInt(),
                    (alarmEndTime.timeInMillis / 1000).toInt()
                )
                val temp = repository.weatherLiveData().asFlow().first().temp
                val temperatureUnit =
                    userPreferencesRepository.userPreferencesFlow.first().temperatureUnit
                title = alarmName
                messageBody =
                    when (temperatureUnit) {
                        TemperatureUnit.CELSIUS -> "No alerts every thing ok, ${temp.roundToInt()}${temperatureUnit.degreeSign}"
                        TemperatureUnit.KELVIN -> "No alerts every thing ok, ${
                            temperatureUnit.cToK(
                                temp
                            )
                        }${temperatureUnit.degreeSign}"
                        TemperatureUnit.FAHRENHEIT -> "No alerts every thing ok, ${
                            temperatureUnit.cToF(
                                temp
                            )
                        }${temperatureUnit.degreeSign}"
                    }
                fireNotificationOrAlarm(alarmType)
            }
            //work complete successfully
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            //exception occurs then should be tried at another time according to its retry policy.
            Result.retry()
        }
    }

    private fun fireNotificationOrAlarm(alarmType: String) {
        //notification alarm
        if (alarmType == "notification") {
            // sendNotification
            val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendNotification(
                title,
                messageBody,
                messageDescription,
                time = time,
                channelId = applicationContext.getString(R.string.weather_alarms_channel_id),
                applicationContext = applicationContext
            )
        } else {
            val intent = Intent(applicationContext, DefaultSoundAlarm::class.java)
            intent.putExtra(ALARM_TITLE, title)
            intent.putExtra(ALARM_MESSAGE_BODY, messageBody)
            intent.putExtra(ALARM_MESSAGE_DESCRIPTION, messageDescription)
            intent.flags = FLAG_ACTIVITY_NEW_TASK;
            applicationContext.startActivity(intent)
        }
    }

    companion object {
        const val ALARM_TYPE = "AlarmType"
        const val ALARM_NAME = "AlarmName"
        const val ALARMS_TAG = "AlarmsTag"
        const val ALARM_HOUR_END_TIME = "alarmHourEndTime"
        const val ALARM_MINUTE_END_TIME = "alarmMinuteEndTime"
    }
}