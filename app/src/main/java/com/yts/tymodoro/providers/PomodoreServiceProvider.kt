package com.yts.tymodoro.providers

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.yts.tymodoro.objects.Pomodore
import com.yts.tymodoro.objects.PomodoreTask
import com.yts.tymodoro.utils.generateRandom

class PomodoreServiceProvider(val context: Context) {
    
    private val TAG = "PomodoreServiceProvider"

    private var onDataChange: (() -> Unit)? = null
    var startTime: Long = 0
        set(value) {
            field = value
            if (value > 0) {
                currentPomodore.edit().putLong("startTime", field).apply()
            }
        }
        get() = currentPomodore.getLong("startTime", -1)

    var endTime: Long = 0
        set(value) {
            field = value
            if (value > 0) {
                currentPomodore.edit().putLong("endTime", field).apply()
                savePomodore()
            }
        }
        get() = currentPomodore.getLong("endTime", -1)

    var workCycles = 0
        set(value) {
            field = value
            if (value > 0) currentPomodore.edit().putInt("workCycles", field).apply()
        }
        get() = currentPomodore.getInt("workCycles", 0)

    var breakCycles = 0
        set(value) {
            field = value
            if (value > 0) currentPomodore.edit().putInt("breakCycles", field).apply()
        }
        get() = currentPomodore.getInt("breakCycles", 0)

    var isRunning = false
        set(value) {
            field = value
            currentPomodore.edit().putBoolean("isRunning", field).apply()
        }
        get() = currentPomodore.getBoolean("isRunning", false)

    companion object {
        private const val POMODORE_SERVICE_KEY = "pomodoreProvider"
        private const val POMODORE_DATA_KEY = "endedPomodoresProvider"
    }

    private var lastSavedId = ""

    private val savedPomodores =
        context.getSharedPreferences(POMODORE_DATA_KEY, Context.MODE_PRIVATE)
    private val currentPomodore =
        context.getSharedPreferences(POMODORE_SERVICE_KEY, Context.MODE_PRIVATE)

    fun setOnDataChangeListener(onDataChange: (() -> Unit)?) {
        this.onDataChange = onDataChange
    }

    fun setRunningTask(pomodore: PomodoreTask?) {
        pomodore?.let { mPomodore ->
            val json = Gson().toJson(mPomodore)
            currentPomodore.edit().putString("runningTask", json).apply()
        } ?: kotlin.run {
            currentPomodore.edit().putString("runningTask", "").apply()
        }
    }

    fun getRunningTask(): PomodoreTask {
        return if(currentPomodore.getString("runningTask", "") != "") Gson().fromJson(
            currentPomodore.getString("runningTask", ""),
            PomodoreTask::class.java
        ) else PomodoreTask("No hay una tarea en ejecución", "No hay una tarea en ejecución", "0")
    }

    fun hasStarted(): Boolean = currentPomodore.getLong("startTime", -1) > 0

    fun getLastSavedPomodore(): Pomodore {
        val pomodoresProvider = PomodoresProvider(context)
        return pomodoresProvider.getPomodore(lastSavedId)
    }

    private fun savePomodore() {
        val task = getRunningTask()
        val pomodore = Pomodore(generateRandom(20), task.title, task.description,
            startTime, endTime, workCycles, breakCycles)
        val jsonString = Gson().toJson(pomodore)
        savedPomodores.edit().putString(pomodore.id, jsonString).apply()
        lastSavedId = pomodore.id
        Log.d(TAG, "saved $jsonString with Id ${pomodore.id}")
        workCycles = 0
        breakCycles = 0
        endTime = 0
        startTime = 0
        currentPomodore.edit().clear().apply()
    }

}