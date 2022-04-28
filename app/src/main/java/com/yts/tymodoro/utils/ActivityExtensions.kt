package com.yts.tymodoro.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

// Archivo de extensiones para cualquier actividad y sus derivados de AppCommpat

fun AppCompatActivity.launchTask(task: (ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
    val launcher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result ->
        task(result)
    }
    return launcher
}

fun Activity.doAfter(delay: Long, task: () -> Unit){
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            task()
        }
    }, delay)
}

fun Activity.doSafeAfter(delay: Long, task: () -> Unit){
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            runOnUiThread {
                task()
            }
        }
    }, delay)
}

fun Activity.doEvery(delay: Long, period: Long, task: (Timer) -> Unit): Timer{
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            task(timer)
        }
    }, delay, period)
    return timer
}

fun Activity.toast(message: String, length: Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, message, length).show()
}

fun Context.getColorRes(@ColorRes color: Int):Int{
    return ContextCompat.getColor(this, color)
}

fun Context.getImageRes(@DrawableRes image: Int): Drawable {
    return ContextCompat.getDrawable(this, image)!!
}

//Constants
val Activity.NEW_POMODORE_TASK: String
    get() = "newPomodoreDesc"

val Activity.NEW_POMODORE_TITLE: String
    get() = "newPomodoreTitle"
val Activity.NEW_POMODORE: String
    get() = "newPomodore"

val Activity.EDIT_POMODORE_TASK: String
    get() = "editPomodoreDesc"

val Activity.EDIT_POMODORE_TITLE: String
    get() = "editPomodoreTitle"

val Activity.EDIT_POMODORE: String
    get() = "editPomodore"

val Activity.POMODORE_ACTION: String get() = "pomodoreAction"

val Activity.POMODORE_POSITION: String get() = "position"