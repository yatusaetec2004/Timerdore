package com.yts.tymodoro.objects

import java.text.SimpleDateFormat
import java.util.*

data class Pomodore(val id: String, val title: String, val description: String, val startTime: Long, val endTime: Long,
                    var totalWorkCycles: Int = 0, var totalBreakCycles: Int = 0): Comparable<Pomodore> {

    fun getFormatedDate(timeInMs: Long, format: String = "dd/MM/yyyy 'a la(s)' hh:mm a"): String {
        val formatter = SimpleDateFormat(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMs
        return formatter.format(calendar.time)
    }

    fun getDifference(): String {
        val difference = endTime - startTime
        val hours = (difference / (1000 * 60 * 60)) % 24
        val minutes = (difference / (1000 * 60)) % 60
        val seconds = (difference / (1000)) % 60
        return "${hours}h ${minutes}m ${seconds}s de duración"
    }

    override fun compareTo(other: Pomodore): Int {
        return when {
            this.startTime > other.startTime -> -1
            this.startTime < other.startTime -> 1
            else -> 0
        }
    }
}
