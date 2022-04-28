package com.yts.tymodoro.providers

import android.content.Context

class AppSettings(context: Context) {

    companion object {
        //Keys for Integers
        const val FOURTH_CYCLE_BREAK = "4thCycleBreak"
        const val BREAK_SPINNER_SELECTION = "breakSpinnerSelection"

        //Keys for booleans
        const val ENABLE_NOTIF_VIBRATE = "enableNotifVibrate"
        const val SHOW_ONLY_ONCE = "showOnlyOnce"
        const val HAS_FIRST_ENTER = "hasFirstEntered"
        const val SHOW_HISTORY_DESC = "showHistoryDesc"
        const val ENABLE_ANIMATIONS = "enableAnimations"
    }

    private val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun setBoolean(key: String, value: Boolean) = settings.edit().putBoolean(key, value).apply()

    fun getBoolean(key: String, defValue: Boolean = false): Boolean = settings.getBoolean(key, defValue)

    fun setIntegerNumber(key: String, value: Long) = settings.edit().putLong(key, value.toLong()).apply()

    fun getIntegerNumber(key: String, defValue: Long = 0): Long = settings.getLong(key, defValue)

    fun setDecimalNumber(key: String, value: Double) = settings.edit().putLong(key, value.toBits()).apply()

    fun getDecimalNumber(key: String, defValue: Double): Double = Double.fromBits(settings.getLong(key, defValue.toLong()))
}