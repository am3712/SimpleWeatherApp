package com.myapps.simpleweatherapp.ui.alertdialog

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.databinding.ActivityDefaultSoundAlertBinding
import com.myapps.simpleweatherapp.worker.ALARM_MESSAGE_BODY
import com.myapps.simpleweatherapp.worker.ALARM_MESSAGE_DESCRIPTION
import com.myapps.simpleweatherapp.worker.ALARM_TITLE


class DefaultSoundAlarm : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: ActivityDefaultSoundAlertBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefaultSoundAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.messageBody.movementMethod = ScrollingMovementMethod()
        getIntentData()
        setTheme(R.style.RoundShapeTheme)
        this.setFinishOnTouchOutside(false)
        binding.button.setOnClickListener { finish() }
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
    }


    private fun getIntentData() {
        binding.title.text = intent.getStringExtra(ALARM_TITLE)
        binding.messageBody.text = intent.getStringExtra(ALARM_MESSAGE_BODY)
        binding.messageDesc.text = intent.getStringExtra(ALARM_MESSAGE_DESCRIPTION)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            turnScreenOn()
        }
    }


    private fun turnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            val win = window
            win.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
        mediaPlayer.release()
    }


}