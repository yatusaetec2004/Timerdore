package com.yts.tymodoro.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yts.tymodoro.R
import com.yts.tymodoro.adapters.PomodoresAdapter
import com.yts.tymodoro.databinding.ActivityHistoryBinding
import com.yts.tymodoro.objects.Pomodore
import com.yts.tymodoro.providers.AppSettings
import com.yts.tymodoro.providers.PomodoresProvider
import com.yts.tymodoro.utils.toast

class HistoryActivity: AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var pomodoresProvider: PomodoresProvider
    private lateinit var adapter: PomodoresAdapter
    private lateinit var settings: AppSettings
    private lateinit var pomodoresList: ArrayList<Pomodore>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = AppSettings(this)
        pomodoresProvider = PomodoresProvider(this)
        pomodoresList = pomodoresProvider.getAllPomodores()
        adapter = PomodoresAdapter(this, pomodoresList)
        binding.listviewHistory.adapter = adapter
        adapter.notifyDataSetChanged()
        fillSpinner()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.deleteAllHistory -> {
            val dialog = AlertDialog.Builder(this).apply {
                setTitle("Borrar todo el historial")
                setMessage("¿Seguro que quieres borrar todo el historial?")
                setPositiveButton("Borrar todo"){mDialog, _ ->
                    pomodoresProvider.clear()
                    pomodoresList.clear()
                    adapter = PomodoresAdapter(this@HistoryActivity, pomodoresList)
                    binding.listviewHistory.adapter = adapter
                    adapter.notifyDataSetChanged()
                    toast("Se borró todo el historial")
                    mDialog.dismiss()
                }
                setNegativeButton("Cancelar"){mDialog, _ ->
                    mDialog.dismiss()
                }
            }
            dialog.show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(!settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)) overridePendingTransition(0, 0)
        finish()
    }

    private fun fillSpinner(){
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sort_history_selection, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortHistorySpinner.adapter = spinnerAdapter
        binding.sortHistorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 0) {
                    adapter.dataList.sort()
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.dataList.sort()
                    adapter.dataList.reverse()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}