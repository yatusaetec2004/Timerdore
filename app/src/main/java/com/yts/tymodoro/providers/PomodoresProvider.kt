package com.yts.tymodoro.providers

import android.content.Context
import com.google.gson.Gson
import com.yts.tymodoro.objects.Pomodore

class PomodoresProvider(val context: Context) {

    companion object {
        private const val POMODORE_DATA_KEY = "endedPomodoresProvider"
    }

    private val savedPomodores =
        context.getSharedPreferences(POMODORE_DATA_KEY, Context.MODE_PRIVATE)

    fun getPomodore(id: String): Pomodore {
        return if(savedPomodores.getString(id, "") != "") {
            Gson().fromJson(savedPomodores.getString(id, ""), Pomodore::class.java)
        } else Pomodore("", "Sin datos", "", -1, -1, -1, -1)
    }

    fun getAllPomodores(): ArrayList<Pomodore> {
        val savedData = savedPomodores.all
        val list = ArrayList<Pomodore>()
        for (item in savedData){
            val gson = Gson()
            val pomodore = gson.fromJson(item.value.toString(), Pomodore::class.java)
            list.add(pomodore)
        }
        return list
    }

    fun deletePomodore(id: String){
        savedPomodores.edit().remove(id).apply()
    }

    fun clear(){
        savedPomodores.edit().clear().apply()
    }

}