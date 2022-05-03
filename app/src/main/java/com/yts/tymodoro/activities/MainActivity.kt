package com.yts.tymodoro.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yts.tymodoro.R
import com.yts.tymodoro.adapters.PomodoreTasksAdapter
import com.yts.tymodoro.animations.LayoutAnimation
import com.yts.tymodoro.animations.ProgressBarAnimation
import com.yts.tymodoro.databinding.ActivityMainBinding
import com.yts.tymodoro.databinding.PomodoreTimerDialogLayoutBinding
import com.yts.tymodoro.objects.PomodoreTask
import com.yts.tymodoro.providers.AppSettings
import com.yts.tymodoro.providers.PomodoreServiceProvider
import com.yts.tymodoro.providers.PomodoreTasksProvider
import com.yts.tymodoro.services.PomodoreService
import com.yts.tymodoro.utils.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pomodoreTasksProvider: PomodoreTasksProvider
    private lateinit var pomodoresAdapter: PomodoreTasksAdapter
    private lateinit var pomodoreServiceProvider: PomodoreServiceProvider
    private lateinit var settings: AppSettings

    private var pomodoresList = ArrayList<PomodoreTask>()

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pomodoreTasksProvider = PomodoreTasksProvider(this)
        pomodoreServiceProvider = PomodoreServiceProvider(this)
        settings = AppSettings(this)
        if (pomodoreServiceProvider.hasStarted()) {
            listenToService(true)
            changeEditWidgetsVisibility(View.GONE)
            changeControlWidgetsVisibility(View.VISIBLE)
        }
        loadPomodores()
        addListeners()
        intro(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
            R.id.showSettings -> {
                startActivity(Intent(this, OptionsActivity::class.java))
                if(!settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)) overridePendingTransition(0, 0)
                true
            }
            R.id.showHistory -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                if(!settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)) overridePendingTransition(0, 0)
                true
            } else -> super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() {
        super.onBackPressed()
        if(!settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)) overridePendingTransition(0, 0)
        finish()
    }

    private fun addListeners() {
        //List listeners
        binding.savedPomodores.setOnItemClickListener { _, _, position, _ ->
            if (pomodoresAdapter.isSelectedItem(position)) {
                binding.textCurrentTask.text = "No hay una tarea seleccionada"
                pomodoresAdapter.clearSelection()
            } else {
                pomodoresAdapter.selectedPosition = position
                updateTask()
            }
        }

        //Button listeners
        binding.btnCreatePomodore.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(POMODORE_ACTION, NEW_POMODORE)
            activityLauncher.launch(intent)
            if(!settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)) overridePendingTransition(0, 0)
            Log.d(TAG, "Sent $NEW_POMODORE")
        }
        //Special ProgressBar OnClick
        binding.quickProgressPomodore.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            var currentCycle = PomodoreService.getCurrentStatus()
            val mView = PomodoreTimerDialogLayoutBinding.bind(
                layoutInflater.inflate(
                    R.layout.pomodore_timer_dialog_layout,
                    null,
                    false
                )
            )
            doAfter(500) {
                animateProgressBar(
                    mView.progressPomodore,
                    calculateTimeInPercentage(PomodoreService.getRunningTime(), mView.progressPomodore)
                )
            }
            var mTimerRunning = false
            val mTimer = doEvery(500, 100) {
                runOnUiThread {
                    updateDialogUI(mView)
                    if(currentCycle != PomodoreService.getCurrentStatus()){
                        currentCycle = PomodoreService.getCurrentStatus()
                        animateProgressBar(mView.progressPomodore)
                    }
                    if(!pomodoreServiceProvider.hasStarted()) {
                        dialog.dismiss()
                    }
                }
            }

            if (!pomodoreServiceProvider.isRunning) {
                mTimer.cancel()
                mTimer.purge()
                updateDialogUI(mView)
            }

            mTimerRunning = true

            mView.btnOkClose.setOnClickListener {
                dialog.dismiss()
            }
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                resources.getDimension(R.dimen.bottom_sheet_peek).toInt()
            )
            dialog.setContentView(mView.root, params)
            dialog.show()
            dialog.setOnDismissListener {
                if (mTimerRunning) {
                    mTimer.cancel()
                    mTimer.purge()
                }
            }
        }

        binding.startPomodore.setOnClickListener {
            if (!pomodoreServiceProvider.hasStarted()) {
                if (pomodoresAdapter.hasAnItemSelected()) {
                    if(settings.getBoolean(AppSettings.ENABLE_ANIMATIONS, true)) {
                        enableQuickProgress()
                    }
                    else {
                        changeEditWidgetsVisibility(View.GONE)
                        changeControlWidgetsVisibility(View.VISIBLE)
                    }
                    pomodoreServiceProvider.setRunningTask(pomodoresAdapter.getSelectedItem())
                    sendToService(PomodoreService.ACTION_START)
                    binding.startPomodore.setImageResource(R.drawable.ic_pause)
                    listenToService()
                    pomodoresAdapter.clearSelection()
                } else {
                    toast("No has seleccionado una tarea")
                }
            } else {
                if (pomodoreServiceProvider.isRunning) {
                    sendToService(PomodoreService.ACTION_PAUSE)
                    binding.startPomodore.setImageResource(R.drawable.ic_play_arrow)
                } else {
                    sendToService(PomodoreService.ACTION_START)
                    binding.startPomodore.setImageResource(R.drawable.ic_pause)
                    listenToService()
                }
            }
        }

        binding.stopPomodore.setOnClickListener {
            sendToService(PomodoreService.ACTION_STOP)
            binding.textCurrentTask.text = "No hay una tarea seleccionada"
            binding.startPomodore.setImageResource(R.drawable.ic_play_arrow)
        }

        binding.nextPomodoreCycle.setOnClickListener {
            if(pomodoreServiceProvider.isRunning){
                sendToService(PomodoreService.ACTION_NEXT_CYCLE)
                binding.quickProgressPomodore.progress = 0
                animateProgressBar(binding.quickProgressPomodore)
                calculateTimeInPercentage(0L, binding.quickProgressPomodore)
            } else toast("Solo puedes avanzar de ciclo si el pomodoro esta en curso")
        }

        //Spinner listeners
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.break_minutes_selection, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerConfigureTime.adapter = spinnerAdapter
        binding.spinnerConfigureTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                settings.setIntegerNumber(AppSettings.BREAK_SPINNER_SELECTION, position.toLong())
                when (position){
                    0 -> settings.setIntegerNumber(AppSettings.FOURTH_CYCLE_BREAK, (15 * 60 * 1000))
                    1 -> settings.setIntegerNumber(AppSettings.FOURTH_CYCLE_BREAK, (30 * 60 * 1000))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        //Load spinner's persistent selection
        binding.spinnerConfigureTime.setSelection(settings.getIntegerNumber(AppSettings.BREAK_SPINNER_SELECTION).toInt(), true)
    }

    private fun updateDialogUI(mView: PomodoreTimerDialogLayoutBinding) {
        val maxTime = calculateMaxTime()
        val spannable = SpannableStringBuilder()
        spannable.append("${DecimalFormat("00").format(calculateTimeInMinutes(PomodoreService.getRunningTime()))}:${
            DecimalFormat("00").format(calculateTimeInSeconds(PomodoreService.getRunningTime()))
        }\n${DecimalFormat("00").format(maxTime)}:00")
        val colorSpan = ForegroundColorSpan(Color.BLUE)
        spannable.setSpan(colorSpan, 6, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        mView.progressPomodore.progress =
            calculateTimeInPercentage(PomodoreService.getRunningTime(), mView.progressPomodore).toInt()
        mView.textBreakCycles.text =
            "Ciclos de descanso: ${pomodoreServiceProvider.breakCycles}"
        mView.textWorkCycles.text =
            "Ciclos de trabajo: ${pomodoreServiceProvider.workCycles}"
        mView.cycleTime.text = spannable
        if (PomodoreService.getCurrentStatus() == PomodoreService.WORK) mView.currentCycle.text =
            "Ciclo ${pomodoreServiceProvider.workCycles + 1} de trabajo"
        else mView.currentCycle.text =
            "Ciclo ${pomodoreServiceProvider.breakCycles + 1} de descanso"
    }

    private fun enableQuickProgress() {
        LayoutAnimation.ScaleAnimation(binding.root).executeOnY(530, action = {
            changeEditWidgetsVisibility(View.GONE)
            changeControlWidgetsVisibility(View.VISIBLE)
            LayoutAnimation.ScaleAnimation(binding.root)
                .executeOnY(530, 0f, 1f, action = {
                    doAfter(250) {
                        animateProgressBar(
                            binding.quickProgressPomodore,
                            calculateTimeInPercentage(PomodoreService.getRunningTime(), binding.quickProgressPomodore)
                        )
                    }
                }, startAction = {})
        })
    }

    private fun enableBreakConfig() {
        LayoutAnimation.ScaleAnimation(binding.root).executeOnY(530, action = {
            changeControlWidgetsVisibility(View.GONE)
            changeEditWidgetsVisibility(View.VISIBLE)
            LayoutAnimation.ScaleAnimation(binding.root)
                .executeOnY(530, 0f, 1f, action = {})
        })
    }

    private fun addItem(title: String, description: String) {
        val newPomodore = PomodoreTask(title, description, generateRandom(10))
        pomodoreTasksProvider.savePomodore(newPomodore)
        pomodoresList.add(newPomodore)
        val mAdapter = PomodoreTasksAdapter(pomodoresList, this)
        binding.savedPomodores.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        Log.d(TAG, "Item $newPomodore added")
        pomodoresAdapter = mAdapter
        pomodoresAdapter.setLaunchTask(editPomodoreLauncher)
    }

    private fun loadPomodores() {
        pomodoresList = pomodoreTasksProvider.getSavedPomodores()
        if (pomodoresList.isNotEmpty()) {
            val mAdapter = PomodoreTasksAdapter(pomodoresList, this)
            binding.savedPomodores.adapter = mAdapter
            mAdapter.notifyDataSetChanged()
            Log.d(TAG, "Items loaded")
            pomodoresAdapter = mAdapter
            pomodoresAdapter.setLaunchTask(editPomodoreLauncher)
        }
    }

    private fun sendToService(action: String) {
        val startIntent = Intent(this, PomodoreService::class.java)
        startIntent.putExtra(PomodoreService.ACTION, action)
        ContextCompat.startForegroundService(this, startIntent)
    }

    private fun listenToService(animateQuickProgress: Boolean = false) {
        val runningTime = PomodoreService.getRunningTime()
        binding.textCurrentTask.text =
            "En ejecución: ${pomodoreServiceProvider.getRunningTask().title}"
        if (animateQuickProgress) animateProgressBar(
            binding.quickProgressPomodore,
            calculateTimeInPercentage(runningTime, binding.quickProgressPomodore)
        )
        val timerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    binding.quickProgressPomodore.progress =
                        calculateTimeInPercentage(PomodoreService.getRunningTime(), binding.quickProgressPomodore).toInt()
                    if(!pomodoreServiceProvider.hasStarted()) {
                        this.cancel()
                        if(settings.getBoolean(AppSettings.ENABLE_ANIMATIONS, true)) {
                            enableBreakConfig()
                        } else {
                            changeControlWidgetsVisibility(View.GONE)
                            changeEditWidgetsVisibility(View.VISIBLE)
                        }
                        binding.textCurrentTask.text = "No hay ninguna tarea seleccionada"
                    }
                    if (!pomodoreServiceProvider.isRunning) {
                        binding.startPomodore.setImageResource(R.drawable.ic_play_arrow)
                    } else {
                        binding.startPomodore.setImageResource(R.drawable.ic_pause)
                    }
                }
            }
        }
        val timer = Timer()
        timer.scheduleAtFixedRate(timerTask, 1, 100)
        Log.d(TAG, "Timer has started")
    }

    private fun calculateTimeInMinutes(timeInMs: Long): Int {
        val timeToMinutes = (timeInMs / (1000 * 60)) % 60
        return timeToMinutes.toInt()
    }

    private fun calculateTimeInSeconds(timeInMs: Long): Int {
        val timeToSeconds = (timeInMs / 1000) % 60
        return timeToSeconds.toInt()
    }

    private fun calculateTimeInPercentage(timeInMs: Long, progressBar: ProgressBar): Float {
        val maxTime: Int = calculateMaxTime()
        setMax(progressBar, (maxTime * 60) * 1000)
        return timeInMs.toFloat()
    }

    private fun calculateMaxTime(): Int {
        val maxTime: Int = when (PomodoreService.getCurrentStatus()) {
            PomodoreService.WORK -> 25
            PomodoreService.BREAK -> {
                if(((pomodoreServiceProvider.breakCycles + 1) % 4) == 0) {
                    (settings.getIntegerNumber(AppSettings.FOURTH_CYCLE_BREAK).toInt() / (1000 * 60)) % 60
                } else {
                    5
                }
            }
            else -> 25
        }
        return maxTime
    }

    private fun setMax(progressBar: ProgressBar, max: Int){
        progressBar.max = max
    }

    private fun changeEditWidgetsVisibility(visibility: Int = View.GONE) {
        binding.configureBreakLayout.visibility = visibility
        binding.savedPomodores.visibility = visibility
        binding.textInfo1.visibility = visibility
        binding.btnCreatePomodore.visibility = visibility
    }

    private fun changeControlWidgetsVisibility(visibility: Int = View.VISIBLE) {
        binding.stopPomodore.visibility = visibility
        binding.nextPomodoreCycle.visibility = visibility
        binding.quickProgressPomodore.visibility = visibility
    }

    //Launchers
    private val activityLauncher = launchTask { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "received OK task")
            result.data?.let { data ->
                val task = data.getStringExtra(NEW_POMODORE_TASK)!!
                val title = data.getStringExtra(NEW_POMODORE_TITLE)!!
                addItem(title, task)
            }
        }
    }

    private val editPomodoreLauncher = launchTask { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val editedItem = PomodoreTask(
                    data.getStringExtra(NEW_POMODORE_TITLE)!!,
                    data.getStringExtra(NEW_POMODORE_TASK)!!
                )
                val position = data.getIntExtra(POMODORE_POSITION, -1)
                editedItem.id = pomodoresList[position].id
                pomodoresList[position] = editedItem
                pomodoresAdapter.notifyDataSetChanged()
                pomodoreTasksProvider.savePomodore(editedItem)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTask() {
        binding.textCurrentTask.text =
            "Seleccionado: ${pomodoresAdapter.getSelectedItem().title}"
    }

    private fun animateProgressBar(progressBar: ProgressBar, endProgress: Float = 0f) {
        progressBar.progress = progressBar.max
        val progressAnim = ProgressBarAnimation(progressBar, progressBar.max.toFloat(), endProgress)
        progressAnim.duration = 1200
        progressBar.startAnimation(progressAnim)
    }

    //Intro para la primera vez que se entra a la app
    private fun intro(intent: Intent){
        val firstEnter = intent.getBooleanExtra("firstEnter", false)
        if(firstEnter){
            fakeListItems()
            enableWidgetsForIntro(false)
            val dialog = AlertDialog.Builder(this).apply {
                setTitle("Bienvenido a Timerdore")
                setMessage("Se te dará a continuación un tutorial rápido explicando como usar la aplicación. Dale click a " +
                        "\"Comenzar\"")
                setPositiveButton("Comenzar"){mDialog, _ -> mDialog.dismiss()}
                setCancelable(false)
                setIcon(R.drawable.ic_clock_white)
            }
            dialog.setOnDismissListener {
                doSafeAfter(600){
                    createTapTarget(
                        binding.configureBreakLayout, "Configurar descanso",
                        "Aquí puedes configurar cuanto tiempo quieres descansar tras cada 4 ciclos"
                    ) {
                        createTapTarget(
                            binding.btnCreatePomodore, "Crear tareas nuevas",
                            "Aquí puedes crear tareas nuevas para poder escoger antes de iniciar un pomodoro"
                        ) {
                            createTapTarget(
                                binding.savedPomodores, "Tareas guardadas",
                                "En esta lista se muestran todas las tareas que has guardado. Para empezar un pomodoro, primero " +
                                        "selecciona una tarea"
                            ) {
                                createTapTarget(
                                    binding.startPomodore, "Empezar pomodoro",
                                    "Cuando tengas una tarea seleccionada, toca aquí para empezar el pomodoro. " +
                                            "Solo puedes tener un pomodoro activo a la vez, para empezar otro, debes " +
                                            "terminar el que este en ejecución."
                                ) {
                                    secondIntro()
                                }
                            }
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun secondIntro(){
        enableQuickProgress()
        doSafeAfter(1600){
            clearFakeList()
            createTapTarget(binding.startPomodore, "Reanudar y pausar",
            "Con este botón puedes pausar o reanudar el pomodoro"){
                createTapTarget(binding.stopPomodore, "Detener", "Con este puedes detener el pomodoro totalmente"){
                    createTapTarget(binding.nextPomodoreCycle, "Cambiar ciclo",
                    "Con este botón puedes cambiar de ciclo. Si estás en un ciclo de trabajo, pasará al ciclo de descanso, y si estás " +
                            "en un ciclo de descanso, pasará al ciclo de trabajo."){
                        createTapTarget(binding.quickProgressPomodore, "Progreso",
                        "Esta barra muestra el progreso del ciclo en curso. Si quieres ver más información, dale click"){
                            optionsMenuIntro()
                        }
                    }
                }
            }
        }
    }

    private fun optionsMenuIntro(){
        doAfter(200){
            Handler(Looper.getMainLooper()).post {
                val showHistory = findViewById<View>(R.id.showHistory)
                val showSettings = findViewById<View>(R.id.showSettings)
                createTapTarget(showHistory, "Historial",
                "Aquí puedes consultar los pomodoros que hayas hecho anteriormente, se encuentran ordenados del más reciente " +
                        "al más antiguo"){
                    createTapTarget(showSettings, "Ajustes",
                    "Aquí puedes cambiar los ajustes de la aplicación"){
                        enableBreakConfig()
                        toast("Eso es todo ;)")
                        enableWidgetsForIntro(true)
                    }
                }
            }
        }
    }

    private fun createTapTarget(view: View, primaryText: CharSequence, secondaryText: CharSequence,
                                promptStateChangeListener: () -> Unit){
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(view)
            .setPrimaryText(primaryText)
            .setSecondaryText(secondaryText)
            .setBackButtonDismissEnabled(true)
            .setPromptBackground(RectanglePromptBackground())
            .setBackgroundColour(getColorRes(R.color.cPrimary))
            .setPrimaryTextColour(Color.WHITE)
            .setSecondaryTextColour(Color.WHITE)
            .setPromptFocal(RectanglePromptFocal())
            .setPromptStateChangeListener {prompt, state ->
                if(state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                    || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED){
                    settings.setBoolean(AppSettings.HAS_FIRST_ENTER, true)
                    prompt.finish()
                    doSafeAfter(250){ promptStateChangeListener() }
                }
            }
            .show()
    }

    private fun fakeListItems(){
        val task1 = PomodoreTask("Título 1", "Descripción opcional 1")
        val task2 = PomodoreTask("Título 2", "Descripción opcional 2")
        pomodoresList.clear()
        pomodoresList.add(task1)
        pomodoresList.add(task2)
        val fakeAdapter = PomodoreTasksAdapter(pomodoresList, this)
        binding.savedPomodores.adapter = fakeAdapter
        fakeAdapter.notifyDataSetChanged()
    }

    private fun clearFakeList(){
        pomodoresList.clear()
        val fakeAdapter = PomodoreTasksAdapter(pomodoresList, this)
        binding.savedPomodores.adapter = fakeAdapter
        fakeAdapter.notifyDataSetChanged()
        loadPomodores()
    }

    private fun enableWidgetsForIntro(enable: Boolean){
        binding.spinnerConfigureTime.isEnabled = enable
        binding.btnCreatePomodore.isEnabled = enable
        binding.savedPomodores.isEnabled = enable
        binding.startPomodore.isEnabled = enable
        binding.stopPomodore.isEnabled = enable
        binding.nextPomodoreCycle.isEnabled = enable
        binding.quickProgressPomodore.isEnabled = enable
    }
}