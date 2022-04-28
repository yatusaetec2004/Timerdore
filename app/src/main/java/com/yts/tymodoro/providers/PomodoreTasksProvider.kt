package com.yts.tymodoro.providers

import android.content.Context
import com.google.gson.Gson
import com.yts.tymodoro.objects.PomodoreTask
import com.yts.tymodoro.utils.generateRandom

class PomodoreTasksProvider(val context: Context) {

    private val preferences = context.getSharedPreferences(POMODORE_KEY, Context.MODE_PRIVATE)

    fun getSavedPomodores(): ArrayList<PomodoreTask> {
        val savedData = preferences.all
        val list = ArrayList<PomodoreTask>()
        for(item in savedData){
            val gson = Gson()
            val itemValue = gson.fromJson(item.value.toString(), PomodoreTask::class.java)
            list.add(itemValue)
        }
        return list
    }

    fun savePomodore(item: PomodoreTask){
        if(item.id.isEmpty()){ item.id = generateRandom(10) }
        val jsonString = Gson().toJson(item)
        preferences.edit().putString(item.id, jsonString).apply()
    }

    fun deletePomodore(id: String){
        preferences.edit().remove(id).apply()
    }

    companion object{

        private const val POMODORE_KEY = "pomodores"

        fun deletePomodore(context: Context, id: String){
            val preferences = context.getSharedPreferences(POMODORE_KEY, Context.MODE_PRIVATE)
            preferences.edit().remove(id).apply()
        }
    }

}