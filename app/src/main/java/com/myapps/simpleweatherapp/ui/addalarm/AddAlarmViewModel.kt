package com.myapps.simpleweatherapp.ui.addalarm

import android.app.Application
import android.net.ParseException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.data.local.Alarm
import com.myapps.simpleweatherapp.data.local.getDatabase
import com.myapps.simpleweatherapp.data.repository.Repository
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.utils.addAlarmWorkManager
import com.myapps.simpleweatherapp.utils.createAlarmsChannel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class AddAlarmViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    private val _timeFrom = MutableLiveData<Long>()
    val timeFrom
        get() = _timeFrom

    private val _timeTo = MutableLiveData<Long>()
    val timeTo
        get() = _timeTo

    private var endTimeHour: Int = 0
    private var endTimeMinute: Int = 0


    private lateinit var _timePicker: MaterialTimePicker
    val timePicker
        get() = _timePicker

    private val _navigateTo = MutableLiveData<String>()
    val navigateTo
        get() = _navigateTo

    val repeatDays = mutableListOf<RepeatDay>()

    private var alarmsStatus: Boolean? = null

    init {

        val c = Calendar.getInstance(Locale.getDefault())
        c.time.time = System.currentTimeMillis()
        var dayOfWeekIndex = c[Calendar.DAY_OF_WEEK]
        repeatDays.add(RepeatDay(dayIndex = dayOfWeekIndex, checked = false))
        repeat(6) {
            c.add(Calendar.DAY_OF_MONTH, 1)
            dayOfWeekIndex = c[Calendar.DAY_OF_WEEK]
            repeatDays.add(RepeatDay(dayIndex = dayOfWeekIndex, checked = false))
        }
        Timber.i(repeatDays.toString())
    }

    fun getTimePickerBuilderInstance(fromOrTo: String): MaterialTimePicker.Builder {
        val timePickerBuilder =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTitleText("Select time")
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)

        _timePicker = timePickerBuilder.build()

        _timePicker.addOnPositiveButtonClickListener {
            try {
                Timber.i("clicked & fromOrTo : $fromOrTo►")
                val selectedTime = getDateLong(_timePicker.hour, _timePicker.minute)
                if (fromOrTo == "FROM")
                    _timeFrom.value = selectedTime
                else {
                    endTimeHour = _timePicker.hour
                    endTimeMinute = _timePicker.minute
                    _timeTo.value = selectedTime
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return timePickerBuilder
    }

    private fun getDateLong(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.time.time = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time.time
    }


    fun addAlarm(alarmName: String, alarmType: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                if (alarmsStatus == null)
                    alarmsStatus =
                        UserPreferencesRepository.getInstance(app.applicationContext).weatherAlarmsFlow.first()

                if (alarmsStatus == false) {
                    UserPreferencesRepository.getInstance(app.applicationContext)
                        .updateAlarmsStatus(true)
                    createAlarmsChannel(app)
                }
                if (alarmName.isEmpty() || alarmName.isBlank())
                    _navigateTo.postValue(app.resources.getString(R.string.please_select_name))
                else if (timeFrom.value == null || timeFrom.value == 0L)
                    _navigateTo.postValue(app.resources.getString(R.string.please_select_start))
                else if (timeTo.value == null || timeTo.value == 0L)
                    _navigateTo.postValue(app.resources.getString(R.string.please_select_end))
                else if (alarmType.isEmpty() || alarmType.isBlank())
                    _navigateTo.postValue(app.resources.getString(R.string.please_select_type))
                else if (!hasAtLeastOneElementChecked())
                    _navigateTo.postValue(app.resources.getString(R.string.please_select_one))
                else if (timeFrom.value!! <= System.currentTimeMillis())
                    _navigateTo.postValue(app.getString(R.string.start_time_must_be_grater))
                else {
                    setNearestDayAsStartTime()
                    val alarm = Alarm(
                        alarmName = alarmName,
                        start = timeFrom.value!!,
                        endHour = endTimeHour,
                        endMinute = endTimeMinute,
                        alarmType = alarmType,
                        repeatDays = repeatDays,
                        isActive = true
                    )
                    val idList = Repository(getDatabase(app.applicationContext)).addAlarm(alarm)
                    Timber.i(alarm.alarmId.toString())
                    addAlarmWorkManager(app, idList[0], alarm)
                    _navigateTo.postValue("")
                }
            }
        }
    }

    private fun setNearestDayAsStartTime() {
        repeatDays.forEach { selectedDay ->
            if (selectedDay.checked) {
                val c = Calendar.getInstance()
                c.timeInMillis = timeFrom.value!!
                Timber.i("date before fix gap : ${DateFormat.getDateTimeInstance().format(c.time)}")
                c.set(Calendar.DAY_OF_WEEK, selectedDay.dayIndex)
                Timber.i("date after fix gap : ${DateFormat.getDateTimeInstance().format(c.time)}")
                _timeFrom.postValue(c.timeInMillis)
                return
            }
        }
    }

    fun clearNavigate() {
        _navigateTo.value = " "
    }

    private fun hasAtLeastOneElementChecked(): Boolean {
        repeatDays.forEach {
            if (it.checked)
                return true
        }
        return false
    }
}

