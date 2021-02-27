package com.example.simpleweatherapp

import com.example.simpleweatherapp.utils.createAlarmsChannel
import com.example.simpleweatherapp.utils.createAlertsChannel
import com.zeugmasolutions.localehelper.LocaleAwareApplication
import timber.log.Timber

class SimpleWeatherApplication : LocaleAwareApplication() {


    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        //alerts channel
        createAlertsChannel(this)

        //alarms channel
        createAlarmsChannel(this)

    }

}