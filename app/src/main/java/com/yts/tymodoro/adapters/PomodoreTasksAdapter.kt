package com.yts.tymodoro.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.result.ActivityResultLauncher
import com.yts.tymodoro.R
import com.yts.tymodoro.activities.CreateActivity
import com.yts.tymodoro.animations.LayoutAnimation
import com.yts.tymodoro.databinding.PomodoreTaskItemBinding
import com.yts.tymodoro.objects.PomodoreTask
import com.yts.tymodoro.providers.PomodoreTasksProvider
import com.yts.tymodoro.utils.EDIT_POMODORE_TASK
import com.yts.tymodoro.utils.EDIT_POMODORE_TITLE
import com.yts.tymodoro.utils.POMODORE_POSITION

class PomodoreTasksAdapter(var dataList: ArrayList<PomodoreTask>, val context: Context) : BaseAdapter() {

    private var launchTask: ActivityResultLauncher<Intent>? = null

    var selectedPosition = -1
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    private val TAG = "PomodoreTasksAdapter"

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): PomodoreTask {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView
            ?: PomodoreTaskItemBinding.bind(LayoutInflater.from(context).inflate(R.layout.pomodore_task_item, null)).root
        val binder = PomodoreTaskItemBinding.bind(view)
        val currentItem = getItem(position)
        val descTextView = binder.taskMessage
        binder.taskTitle.text = currentItem.title
        if(!currentItem.hasDescription) descTextView.visibility = View.GONE else descTextView.text = currentItem.description

        if(position == selectedPosition){
            binder.root.setBackgroundColor(context.getColor(R.color.green))
            binder.taskTitle.setTextColor(context.getColor(R.color.white))
            descTextView.setTextColor(context.getColor(R.color.white))
        } else {
            binder.root.setBackgroundColor(context.getColor(R.color.white))
            binder.taskTitle.setTextColor(context.getColor(R.color.black))
            descTextView.setTextColor(context.getColor(R.color.black))
        }

        binder.deletePomodore.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("Borrar")
            dialog.setMessage("¿Seguro que quieres borrar \"${currentItem.title}\" de la lista de tareas?")
            dialog.setPositiveButton("Borrar") {dialogInterface, which ->
                LayoutAnimation.FadeAnimation(binder.root).animateAlpha(650, action = {
                    it?.fillAfter = false
                    PomodoreTasksProvider.deletePomodore(context, currentItem.id)
                    dataList.remove(currentItem)
                    notifyDataSetChanged()
                })
            }
            dialog.setNegativeButton("Cancelar") {dialogInterface, which ->
                dialogInterface.dismiss()
            }
            dialog.show()
        }
        binder.deletePomodore.isFocusable = false
        binder.deletePomodore.isFocusableInTouchMode = false

        binder.editPomodore.setOnClickListener {
            launchTask?.let {
                val mACtivity = context as Activity
                val mIntent = Intent(context, CreateActivity::class.java)
                val mTitle = currentItem.title
                val mDesc = currentItem.description
                Log.d(TAG, "Sending position $position with title $mTitle and description $mDesc")
                mIntent.putExtra(mACtivity.POMODORE_POSITION, position)
                mIntent.putExtra(mACtivity.EDIT_POMODORE_TASK, mDesc)
                mIntent.putExtra(mACtivity.EDIT_POMODORE_TITLE, mTitle)
                it.launch(mIntent)
            }
        }
        binder.editPomodore.isFocusable = false
        binder.editPomodore.isFocusableInTouchMode = false

        return view
    }

    fun setLaunchTask(taskLauncher: ActivityResultLauncher<Intent>){
        launchTask = taskLauncher
    }

    fun getSelectedItem(): PomodoreTask = dataList[selectedPosition]

    fun isSelectedItem(position: Int):Boolean = position == selectedPosition

    fun clearSelection(){
        selectedPosition = -1
    }

    fun hasAnItemSelected(): Boolean = selectedPosition >= 0
}