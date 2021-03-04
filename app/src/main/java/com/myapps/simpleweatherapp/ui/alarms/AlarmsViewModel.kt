package com.myapps.simpleweatherapp.ui.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.myapps.simpleweatherapp.data.local.Alarm
import com.myapps.simpleweatherapp.data.local.getDatabase
import com.myapps.simpleweatherapp.data.repository.Repository
import com.myapps.simpleweatherapp.ui.addalarm.RepeatDay
import com.myapps.simpleweatherapp.ui.settings.getUpdatedAlarm
import com.myapps.simpleweatherapp.utils.addAlarmWorkManager
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class AlarmsViewModel(
    val app: Application
) : AndroidViewModel(app) {

    val repository = Repository(getDatabase(app.applicationContext))

    val alarms = repository.getAlarms()

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            cancelAlarm(alarm.alarmId.toString())
            repository.deleteAlarm(alarm)
        }
    }

    private fun cancelAlarm(alarmId: String) {
        WorkManager.getInstance(app.applicationContext).cancelUniqueWork(alarmId)
    }

    fun startStopAlarm(alarm: Alarm) {
        viewModelScope.launch {
            if (alarm.isActive) {
                val updatedAlarm = getUpdatedAlarm(alarm, false)
                repository.addAlarm(updatedAlarm)
                cancelAlarm(alarm.alarmId.toString())
            } else {
                val updatedAlarm = getUpdatedAlarm(alarm, true)
                addAlarmWorkManager(app, updatedAlarm.alarmId, updatedAlarm)
                repository.addAlarm(updatedAlarm)
            }
        }
    }
}

fun getCorrectStart(repeatDays: List<RepeatDay>, startInMilliSeconds: Long): Long {
    val c = Calendar.getInstance(Locale.getDefault())
    val startCalendar = Calendar.getInstance()
    startCalendar.timeInMillis = startInMilliSeconds

    c.timeInMillis = System.currentTimeMillis()
    var dayOfWeekIndex: Int = c[Calendar.DAY_OF_WEEK]
    while (true) {
        if (repeatDays.firstOrNull { it.dayIndex == dayOfWeekIndex && it.checked } != null) {
            c[Calendar.HOUR] = startCalendar[Calendar.HOUR]
            c[Calendar.MINUTE] = startCalendar[Calendar.MINUTE]
            c[Calendar.SECOND] = 0
            Timber.i("Correct Start : ${DateFormat.getDateTimeInstance().format(c.time)}")
            return c.timeInMillis
        }
        c.add(Calendar.DAY_OF_MONTH, 1)
        dayOfWeekIndex = c[Calendar.DAY_OF_WEEK]
    }
}