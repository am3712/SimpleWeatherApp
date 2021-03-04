package com.myapps.simpleweatherapp

import com.myapps.simpleweatherapp.utils.createAlarmsChannel
import com.myapps.simpleweatherapp.utils.createAlertsChannel
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