package com.yts.tymodoro.objects

data class PomodoreTask(val title: String, val description: String, var id: String = ""){
    val hasDescription = description.isNotEmpty()
}
