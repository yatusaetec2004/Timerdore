package com.yts.tymodoro.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.yts.tymodoro.R
import com.yts.tymodoro.databinding.ActivityCreateBinding
import com.yts.tymodoro.providers.AppSettings
import com.yts.tymodoro.utils.*

class CreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBinding
    private val TAG = "CreateActivity"
    private lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras!!
        settings = AppSettings(this)
        Log.d(TAG, "ACTION = ${extras[POMODORE_ACTION]}")
        if(extras[POMODORE_ACTION] == NEW_POMODORE) createPomodore()
        else editPomodore(extras)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED, null)
        if(!settings.getBoolean(AppSettings.ENABLE_ANIMATIONS)) overridePendingTransition(0, 0)
        finish()
    }

    private fun createPomodore(){
        binding.btnCreatePomodore.setOnClickListener {
            val titleEditText = binding.titleEditText.editText
            if(titleEditText?.text.toString().trim() == "") toast("El título no puede estar vacío")
            else {
                val title = binding.titleEditText.editText?.text.toString()
                val description = binding.descriptionEditText.editText?.text.toString()
                val returnData = Intent()
                returnData.putExtra(NEW_POMODORE_TITLE, title)
                returnData.putExtra(NEW_POMODORE_TASK, description)
                setResult(RESULT_OK, returnData)
                Log.d(TAG, "Pomodore sent with $title and $description")
                finish()
            }
        }

        binding.btnCancelCreation.setOnClickListener{
            setResult(RESULT_CANCELED, null)
            finish()
        }
    }

    private fun editPomodore(extras: Bundle){
        val titleEditText = binding.titleEditText.editText!!
        val descEditText = binding.descriptionEditText.editText!!

        val oldTitle = extras.getString(EDIT_POMODORE_TITLE)
        val oldDescription = extras.getString(EDIT_POMODORE_TASK)

        Log.d(TAG, "received $oldTitle with $oldDescription and position ${extras.getInt(POMODORE_POSITION)}")

        titleEditText.text = oldTitle?.toEditable()
        descEditText.text = oldDescription?.toEditable()

        binding.btnCreatePomodore.setOnClickListener {
            if(isTitleValid(titleEditText)) {
                val title = binding.titleEditText.editText?.text.toString()
                val description = descEditText.text.toString()
                val returnData = Intent()
                returnData.putExtra(NEW_POMODORE_TITLE, title)
                returnData.putExtra(NEW_POMODORE_TASK, description)
                returnData.putExtra(POMODORE_POSITION, extras.getInt(POMODORE_POSITION))
                setResult(RESULT_OK, returnData)
                finish()
            }
        }

        binding.btnCancelCreation.setOnClickListener{
            setResult(RESULT_CANCELED, null)
            finish()
        }
    }

    private fun isTitleValid(editText: EditText): Boolean {
        if(isTitleEmpty(editText.text.toString())) {
            editText.error = "El título no puede estar vacío"
            return false
        } else if(!hasNotExceedLimit(editText)){
            editText.error = "Se ha excedido el límite de carácteres"
            return false
        }
        editText.error = ""
        return true
    }

    private fun isTitleEmpty(text:String): Boolean {
        return text.trim().isEmpty()
    }

    private fun hasNotExceedLimit(editText: EditText): Boolean{
        return editText.length() <= 30
    }
}