package com.yts.tymodoro.activities

import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.yts.tymodoro.App
import com.yts.tymodoro.databinding.ActivityOptionsBinding
import com.yts.tymodoro.providers.AppSettings
import com.yts.tymodoro.providers.PomodoreServiceProvider
import com.yts.tymodoro.utils.ifSdkIsGreaterOrEqual

class OptionsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityOptionsBinding
    private lateinit var settings: AppSettings
    private lateinit var serviceProvider: PomodoreServiceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptionsBinding.inflate(layoutInflater)
        settings = AppSettings(this)
        serviceProvider = PomodoreServiceProvider(this)
        setContentView(binding.root)

        initializeSwitchSettings()
        if(serviceProvider.hasStarted()){
            enableOptionsWidgets(false)
            binding.textAlertSettings.visibility = View.VISIBLE
        } else {
            enableOptionsWidgets(true)
            binding.textAlertSettings.visibility = View.GONE
        }
    }

    private fun initializeSwitchSettings() {
        val enableNotificationVibrate = binding.enableNotificationVibrate
        val onlyAlertOnce = binding.onlyAlertOnce
        val showHistoryDesc = binding.showHistoryDesc
        val enableAnimations = binding.enableAnimations

        enableNotificationVibrate.isChecked = settings.getBoolean(AppSettings.ENABLE_NOTIF_VIBRATE)
        onlyAlertOnce.isChecked = settings.getBoolean(AppSettings.SHOW_ONLY_ONCE)
        showHistoryDesc.isChecked = settings.getBoolean(AppSettings.SHOW_HISTORY_DESC)
        enableAnimations.isChecked = settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)

        enableNotificationVibrate.setOnCheckedChangeListener { _, isChecked ->
            settings.setBoolean(AppSettings.ENABLE_NOTIF_VIBRATE, isChecked)
            modifyChannelSettings(isChecked)
        }
        onlyAlertOnce.setOnCheckedChangeListener { _, isChecked ->
            settings.setBoolean(AppSettings.SHOW_ONLY_ONCE, isChecked)
        }
        showHistoryDesc.setOnCheckedChangeListener { _, isChecked ->
            settings.setBoolean(AppSettings.SHOW_HISTORY_DESC, isChecked)
        }
        enableAnimations.setOnCheckedChangeListener { _, isChecked ->
            settings.setBoolean(AppSettings.ENABLE_ANIMATIONS, isChecked)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(!settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)) overridePendingTransition(0, 0)
        finish()
    }

    private fun enableOptionsWidgets(enable: Boolean){
        binding.enableNotificationVibrate.isEnabled = enable
        binding.onlyAlertOnce.isEnabled = enable
    }

    private fun modifyChannelSettings(enableVibrations: Boolean){
        ifSdkIsGreaterOrEqual(Build.VERSION_CODES.O){
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(App.CHANNEL_ID)
            channel.apply {
                enableVibration(enableVibrations)
                vibrationPattern = App.VIBRATION_PATTERN1
            }
            manager.deleteNotificationChannel(App.CHANNEL_ID)
            manager.createNotificationChannel(channel)
        }
    }

}