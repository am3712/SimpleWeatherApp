package com.example.simpleweatherapp.utils

import java.text.DateFormatSymbols
import java.util.*

class DateHelper {

    //    val c = Calendar.getInstance()
//    val date = Date()
//    c.time = date
//    val dayOfWeek = c[Calendar.DAY_OF_WEEK]
//    Timber.i("dayOfWeek : $dayOfWeek")
//    Timber.i(weekdays[dayOfWeek])
    companion object {

    }
}

fun getWeekDays(): Array<String> = DateFormatSymbols(Locale.getDefault()).weekdays

fun getCurrentDayIndex(): Int {
    val c = Calendar.getInstance()
    c.time.time = System.currentTimeMillis()
    return c[Calendar.DAY_OF_WEEK]
}