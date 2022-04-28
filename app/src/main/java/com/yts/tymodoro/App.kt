package com.yts.tymodoro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat

class App : Application() {

    companion object {
        const val CHANNEL_ID = "pomodoreServiceChannel"
        const val CHANNEL_NAME = "Pomodoros"

        val VIBRATION_PATTERN1 = longArrayOf(200, 200, 200, 200)
    }

    override fun onCreate() {
        super.onCreate()
        createNotifcationChannel()
    }

    private fun createNotifcationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN1

            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

}