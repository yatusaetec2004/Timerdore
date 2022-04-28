package com.yts.tymodoro.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.util.rangeTo

import com.yts.tymodoro.App.Companion.CHANNEL_ID
import com.yts.tymodoro.App.Companion.CHANNEL_NAME
import com.yts.tymodoro.R
import com.yts.tymodoro.activities.MainActivity

fun generateRandom(stringLength: Int):String{
    val allowerChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    var randomStr = ""
    for (i in 1..stringLength){
        randomStr += allowerChars.random()
    }
    return randomStr
}

fun buildRunningNotification(context: Context, contentText: String, openClass: Class<out Activity> = MainActivity::class.java): NotificationCompat.Builder {
    val openIntent = Intent(context, openClass)
    val pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val myNotification = NotificationCompat.Builder(context, CHANNEL_ID)
    myNotification.setContentTitle("Timerdore")
    myNotification.setContentText(contentText)
    myNotification.setSmallIcon(R.drawable.ic_clock_white)
    myNotification.color = context.getColorRes(R.color.red)
    myNotification.setOnlyAlertOnce(true)
    myNotification.setAutoCancel(true)
    myNotification.priority = NotificationCompat.PRIORITY_HIGH
    myNotification.setContentIntent(pendingIntent)
    myNotification.setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
    return myNotification
}

fun ifSdkIsGreaterOrEqual(versionCodes: Int, operation: () -> Unit){
    if(Build.VERSION.SDK_INT >= versionCodes){
        operation()
    }
}