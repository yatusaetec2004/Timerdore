package com.yts.tymodoro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import com.yts.tymodoro.providers.AppSettings

class App : Application() {

    private lateinit var settings: AppSettings

    companion object {
        const val CHANNEL_VIBRATION_ID = "pomodoreServiceChannel"
        const val CHANNEL_NO_VIBRATION_ID = "pomodoreServiceChannelNoVibration"
        const val CHANNEL_NAME = "Pomodoros"

        val VIBRATION_PATTERN1 = longArrayOf(200, 200, 200, 200)

        fun createVibrationNotifChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_VIBRATION_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN1
                }

                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
                manager.deleteNotificationChannel(CHANNEL_NO_VIBRATION_ID)
                val settings = AppSettings(context)
                settings.setString(AppSettings.CURRENT_NOTIF_ID, CHANNEL_VIBRATION_ID)
            }
        }

        fun createNoVibrationNotifChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_NO_VIBRATION_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(false)
                }

                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
                manager.deleteNotificationChannel(CHANNEL_VIBRATION_ID)
                val settings = AppSettings(context)
                settings.setString(AppSettings.CURRENT_NOTIF_ID, CHANNEL_NO_VIBRATION_ID)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settings = AppSettings(this)
        setDefaultSettings()
    }

    private fun setDefaultSettings(){
        if (!settings.getBoolean("defaults")){
            settings.setBoolean(AppSettings.ENABLE_ANIMATIONS, true)
            settings.setBoolean(AppSettings.ENABLE_NOTIF_VIBRATE, true)
            settings.setBoolean("defaults", true)
        }
    }
}