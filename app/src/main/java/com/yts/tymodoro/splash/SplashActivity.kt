package com.yts.tymodoro.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yts.tymodoro.activities.MainActivity
import com.yts.tymodoro.providers.AppSettings

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = AppSettings(this)
        val firstStart = settings.getBoolean(AppSettings.HAS_FIRST_ENTER)
        if(!firstStart){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("firstEnter", true)
            startActivity(intent)
            finish()
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}