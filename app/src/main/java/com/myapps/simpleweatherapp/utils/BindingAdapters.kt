package com.myapps.simpleweatherapp.utils

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.data.local.Alarm
import com.myapps.simpleweatherapp.data.local.City
import com.myapps.simpleweatherapp.data.local.Day
import com.myapps.simpleweatherapp.data.local.Hour
import com.myapps.simpleweatherapp.data.remote.atEndOfDay
import com.myapps.simpleweatherapp.data.repository.TemperatureUnit
import com.myapps.simpleweatherapp.ui.addalarm.RepeatDay
import com.myapps.simpleweatherapp.ui.addalarm.RepeatDayAdapter
import com.myapps.simpleweatherapp.ui.alarms.AlertAdapter
import com.myapps.simpleweatherapp.ui.favourite.CityAdapter
import com.myapps.simpleweatherapp.ui.weather.DayAdapter
import com.myapps.simpleweatherapp.ui.weather.HourAdapter
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


/**
 * Binding adapter used to display correct temperature
 */
@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["temp", "temperatureUnit"], requireAll = false)
fun setTemp(view: TextView, temp: Float, temperatureUnit: TemperatureUnit?) {
    val nf = NumberFormat.getInstance(Locale.getDefault())
    //switch to determine current unit
    when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> view.text =
            "${nf.format(temp.roundToInt())}${temperatureUnit.degreeSign}"
        TemperatureUnit.KELVIN -> view.text =
            "${nf.format(temperatureUnit.cToK(temp))}${temperatureUnit.degreeSign}"
        TemperatureUnit.FAHRENHEIT -> view.text =
            "${nf.format(temperatureUnit.cToF(temp))}${temperatureUnit.degreeSign}"
    }


}

@BindingAdapter("dateTime")
fun setDateTime(textView: TextView, date: Long) {
    if (date != 0L)
        textView.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
}

@BindingAdapter(value = ["hour", "minute"], requireAll = false)
fun setDateTimeByHourMinute(textView: TextView, hour: Int, minute: Int) {
    val c = Calendar.getInstance()
    c.timeInMillis = System.currentTimeMillis()
    c[Calendar.MINUTE] = minute
    c[Calendar.HOUR] = hour
    if (hour != 0)
        textView.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.timeInMillis)
}

@BindingAdapter("lastUpdate")
fun setLastUpdate(textView: TextView, date: Long) {
    if (date != 0L)
        textView.text =
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date)
}

@BindingAdapter("alarmType")
fun setAlarmType(image: ImageView, type: String?) {
    if (type == image.resources.getString(R.string.notification_type))
        image.setImageResource(R.drawable.ic_baseline_notifications_24)
    else
        image.setImageResource(R.drawable.ic_baseline_alarm_24)
}

@BindingAdapter("hourTime")
fun setHourTime(textView: TextView, date: Long) {
    if (date != 0L) {
        val endOfDay: Long = atEndOfDay(date)
        if (date < endOfDay) {
            textView.text =
                DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
        } else {
            textView.text =
                SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(date)
        }
    }
}

@BindingAdapter("dayDate")
fun setDayDate(textView: TextView, date: Long) {
    if (date != 0L) {
        textView.text =
            SimpleDateFormat("EEE, dd/M", Locale.getDefault()).format(date)
    }
}


@BindingAdapter("goneStatus")
fun setGone(view: View, status: Boolean) {
    if (status)
        view.visibility = View.GONE
}

@BindingAdapter(value = ["listDayData", "dayTemperatureUnit"], requireAll = false)
fun bindDayRecyclerView(
    recyclerView: RecyclerView,
    data: List<Day>?,
    dayTemperatureUnit: TemperatureUnit?
) {
    val adapter = recyclerView.adapter as DayAdapter
    adapter.temperatureUnit = dayTemperatureUnit
    adapter.submitList(data)
}

@BindingAdapter(value = ["listHourData", "hourTemperatureUnit"])
fun bindHourRecyclerView(
    recyclerView: RecyclerView,
    data: List<Hour>?,
    hourTemperatureUnit: TemperatureUnit?
) {
    val adapter = recyclerView.adapter as HourAdapter
    adapter.temperatureUnit = hourTemperatureUnit
    adapter.submitList(data)
}

@BindingAdapter("repeatDayList")
fun bindRepeatDayRecyclerView(recyclerView: RecyclerView, data: List<RepeatDay>?) {
    val adapter = recyclerView.adapter as RepeatDayAdapter
    if (data != null) {
        adapter.data = data
    }
}

@BindingAdapter("dayName")
fun bindRepeatDaysName(textView: TextView, index: Int) {
    val weekDays = getWeekDays()
    textView.text = weekDays[index]
}


@BindingAdapter(value = ["listFavouriteData", "favouriteTemperatureUnit"])
fun bindFavouriteRecyclerView(
    recyclerView: RecyclerView,
    data: List<City>?,
    favouriteTemperatureUnit: TemperatureUnit?
) {
    val adapter = recyclerView.adapter as CityAdapter
    adapter.temperatureUnit = favouriteTemperatureUnit
    adapter.submitList(data)
}

@BindingAdapter("listAlertData")
fun bindAlertRecyclerView(
    recyclerView: RecyclerView,
    data: List<Alarm>?
) {
    val adapter = recyclerView.adapter as AlertAdapter
    adapter.submitList(data)
}

@BindingAdapter("weatherIcon")
fun bindIcon(imgView: ImageView, img: String?) {
    var show: Drawable?
    img?.let {
        show = when (img) {
            "01d" -> AppCompatResources.getDrawable(imgView.context, R.drawable._01d)
            "01n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._01n)
            "02d" -> AppCompatResources.getDrawable(imgView.context, R.drawable._02d)
            "02n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._02n)
            "03d", "03n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._03)
            "04d", "04n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._04)
            "09d", "09n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._09)
            "10d" -> AppCompatResources.getDrawable(imgView.context, R.drawable._10d)
            "10n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._10n)
            "11d", "11n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._11)
            "13d", "13n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._13)
            "50d", "50n" -> AppCompatResources.getDrawable(imgView.context, R.drawable._50)
            else -> AppCompatResources.getDrawable(imgView.context, R.drawable.ic_broken_image)
        }
        imgView.setImageDrawable(show)
    }
}

