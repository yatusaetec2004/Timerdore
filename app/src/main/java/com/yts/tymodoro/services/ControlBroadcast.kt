package com.yts.tymodoro.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ControlBroadcast : BroadcastReceiver() {

    private val TAG = "ControlBroadcast"

    override fun onReceive(context: Context?, intent: Intent?) {
        val flag = intent?.getStringExtra("flag")!!
        val newIntent = Intent(context, PomodoreService::class.java)
        newIntent.putExtra(PomodoreService.ACTION, flag)
        context?.startService(newIntent)
        Log.d(TAG, "Broadcast reached, sent flag $flag")
    }
}