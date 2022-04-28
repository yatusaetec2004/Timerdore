package com.yts.tymodoro.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yts.tymodoro.App
import com.yts.tymodoro.R
import com.yts.tymodoro.activities.HistoryActivity
import com.yts.tymodoro.providers.AppSettings
import com.yts.tymodoro.providers.PomodoreServiceProvider
import com.yts.tymodoro.providers.PomodoresProvider
import com.yts.tymodoro.utils.buildRunningNotification
import java.util.*

class PomodoreService : Service() {

    private val TAG = "PomodoreService"

    private lateinit var thread: Timer
    private lateinit var threadTask: TimerTask
    private lateinit var serviceProvider: PomodoreServiceProvider
    private lateinit var notification: NotificationCompat.Builder
    private lateinit var runningMsg: String
    private lateinit var runningId: String
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var settings: AppSettings


    private val breakPhrases = arrayOf(
        "Tiempo de descansar",
        "Toca descansar un rato",
        "Hora de un descansito",
        "Es hora de una pausa personal",
        "Mucho trabajo no es bueno, tomate un descanso ;)",
        "Descansa un rato"
    )
    private var hasFirstStarted = false

    companion object {

        @JvmStatic
        val WORK = "run"

        @JvmStatic
        val BREAK = "break"

        @JvmStatic
        val ACTION = "Action"

        @JvmStatic
        val ACTION_START = "Start"

        @JvmStatic
        val ACTION_PAUSE = "Pause"

        @JvmStatic
        val ACTION_STOP = "Stop"

        @JvmStatic
        val ACTION_NEXT_CYCLE = "NextCycle"

        @JvmStatic
        val NOTIF_MSG = "Message"

        @JvmStatic
        private var RUNNING_TIME = 0L

        @JvmStatic
        private var currentStatus = ""

        @JvmStatic
        fun getRunningTime(): Long = RUNNING_TIME

        @JvmStatic
        fun getCurrentStatus(): String = currentStatus
    }

    override fun onCreate() {
        super.onCreate()
        serviceProvider = PomodoreServiceProvider(this)
        settings = AppSettings(this)
        if (!serviceProvider.hasStarted()) {
            serviceProvider.startTime = Calendar.getInstance().timeInMillis
        }
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PomodoreLock:PomodoreWakeLock")
        wakeLock.acquire()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Received to service action ${intent?.getStringExtra(ACTION) ?: "NO_ACTION"}")
        runningMsg = serviceProvider.getRunningTask().title
        runningId = serviceProvider.getRunningTask().id
        if (intent?.getStringExtra(ACTION) == ACTION_START) {
            if ((currentStatus == WORK || currentStatus == BREAK) or (currentStatus == "")) {
                runTask {
                    runPomodore()
                }
            }
            notification = buildRunningNotification(this, runningMsg)
            if (!hasFirstStarted) {
                addNotificationActions(notification, ACTION_PAUSE)
                notification.setAutoCancel(false)
                startForeground(1, notification.build())
                hasFirstStarted = true
            }
            else updateNotification(runningMsg)
        } else if (intent?.getStringExtra(ACTION) == ACTION_PAUSE) {
            updateNotification("Pomodoro en pausa", ACTION_START)
            thread.cancel()
            destroyWakeLock()
            serviceProvider.isRunning = false
        } else if (intent?.getStringExtra(ACTION) == ACTION_NEXT_CYCLE) {
            checkIfNextCycleIsAvailable()
        } else if(intent?.getStringExtra(ACTION) == ACTION_STOP){
            stopSelf()
            stopForeground(true)
            savePomodore()
            destroyWakeLock()
            thread.cancel()
            thread.purge()
            serviceProvider.isRunning = false
        }
        return START_REDELIVER_INTENT
    }

    private fun checkIfNextCycleIsAvailable() {
        if (serviceProvider.isRunning) {
            if (currentStatus == WORK) {
                transitionToBreak()
            } else if (currentStatus == BREAK) {
                transitionToWork()
            }
        } else Toast.makeText(
            this,
            "Solo puedes avanzar de ciclo si el pomodoro esta en curso",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun runPomodore() {
        serviceProvider.isRunning = true
        RUNNING_TIME += 1
        if (currentStatus == WORK && RUNNING_TIME >= 1500000L) {
            transitionToBreak()
        } else if ((currentStatus == BREAK && RUNNING_TIME >= getMaxBreakTimeInMs()) || currentStatus == "") {
            transitionToWork()
        }
    }

    private fun getMaxBreakTimeInMs(): Int = if(((serviceProvider.breakCycles + 1) % 4) == 0) {
            settings.getIntegerNumber(AppSettings.FOURTH_CYCLE_BREAK).toInt()
        } else {
            300000 //5 minutes in milliseconds
    }

    override fun onDestroy() {
        super.onDestroy()
        RUNNING_TIME = 0
        currentStatus = ""
        val calculateTime = fun(time1: Long, time2: Long): String {
            val difference = time2 - time1
            return "${(difference / (1000 * 60 * 60) % 24)}h ${(difference / (1000 * 60) % 60)}m ${(difference / 1000) % 60}s"
        }
        val savedPomodore = serviceProvider.getLastSavedPomodore()
        val lastNotif = buildRunningNotification(this, "Se ha finalizado el pomodoro con ${calculateTime(savedPomodore.startTime, savedPomodore.endTime)}" +
                " de duración, ${savedPomodore.totalWorkCycles}" +
                " ciclos de trabajo y ${savedPomodore.totalBreakCycles} ciclos de descanso", HistoryActivity::class.java)
        if(settings.getBoolean(AppSettings.ENABLE_NOTIF_VIBRATE)) lastNotif.setVibrate(App.VIBRATION_PATTERN1)
        notify(2, lastNotif.build())
        val manager = NotificationManagerCompat.from(this)
        manager.cancel(1)
        destroyWakeLock()
    }

    private fun runTask(task: () -> Unit) {
        thread = Timer()
        threadTask = object : TimerTask() {
            override fun run() {
                task()
            }
        }
        thread.scheduleAtFixedRate(threadTask, 1, 1)
    }

    private fun updateNotification(msg: String, flags: String = ACTION_PAUSE) {
        notification = buildRunningNotification(this, msg)

        addNotificationActions(notification, flags)
        notification.setAutoCancel(false)

        notify(1, notification.build())
    }

    private fun addNotificationActions(notification: NotificationCompat.Builder, flags: String){
        val pendingIntent = createPendingIntent(flags, 90)
        val stopPendingIntent = createPendingIntent(ACTION_STOP, 91)
        val nextPendingIntent = createPendingIntent(ACTION_NEXT_CYCLE, 92)
        val actionText = when (flags){
            ACTION_PAUSE -> "Pausar"
            ACTION_START -> "Reanudar"
            else -> "Indefinido"
        }
        val actionIcon = when (flags){
            ACTION_PAUSE -> R.drawable.ic_pause
            ACTION_START -> R.drawable.ic_play_arrow
            else -> 0
        }

        notification.addAction(actionIcon, actionText, pendingIntent)
        notification.addAction(R.drawable.ic_next, "Siguiente ciclo", nextPendingIntent)
        notification.addAction(R.drawable.ic_stop, "Detener", stopPendingIntent)
    }

    private fun createPendingIntent(intentFlag: String, requestCode: Int, flag: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent{
        val mIntent = Intent(this, ControlBroadcast::class.java)
        mIntent.putExtra("flag", intentFlag)
        return PendingIntent.getBroadcast(this, requestCode, mIntent, flag)
    }

    private fun destroyWakeLock() {
        if (wakeLock.isHeld) wakeLock.release()
    }

    private fun savePomodore() { //all logic of saving is inside endTime's setter
        serviceProvider.endTime = Calendar.getInstance().timeInMillis
    }

    private fun transitionToWork() {
        RUNNING_TIME = 0
        currentStatus = WORK
        updateNotification(runningMsg)
        if(serviceProvider.workCycles > 0){
            serviceProvider.breakCycles += 1
            val notif = buildRunningNotification(this, "Terminó el descanso, de vuelta a trabajar")
            if(settings.getBoolean(AppSettings.ENABLE_NOTIF_VIBRATE)) {
                notif.setVibrate(App.VIBRATION_PATTERN1)
            }
            notif.setOnlyAlertOnce(settings.getBoolean(AppSettings.SHOW_ONLY_ONCE))
            notify(2, notif.build())
        }
    }

    private fun transitionToBreak() {
        RUNNING_TIME = 0
        currentStatus = BREAK
        updateNotification(breakPhrases.random())
        serviceProvider.workCycles += 1
        val notif = buildRunningNotification(this, "Ciclo de trabajo completo. Toca descansar")
        if(settings.getBoolean(AppSettings.ENABLE_NOTIF_VIBRATE)) notif.setVibrate(App.VIBRATION_PATTERN1)
        notif.setOnlyAlertOnce(settings.getBoolean(AppSettings.SHOW_ONLY_ONCE))
        notify(2, notif.build())
    }

    fun notify(id: Int, notification: Notification){
        val manager = NotificationManagerCompat.from(this)
        manager.notify(id, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

}