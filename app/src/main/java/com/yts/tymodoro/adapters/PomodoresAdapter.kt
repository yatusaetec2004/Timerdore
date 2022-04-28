package com.yts.tymodoro.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.yts.tymodoro.R
import com.yts.tymodoro.animations.LayoutAnimation
import com.yts.tymodoro.databinding.PomodoreItemBinding
import com.yts.tymodoro.objects.Pomodore
import com.yts.tymodoro.providers.AppSettings
import com.yts.tymodoro.providers.PomodoresProvider

class PomodoresAdapter(val context: Context, val dataList: ArrayList<Pomodore>) :
    BaseAdapter() {

    private var isShowed = false
    private val provider = PomodoresProvider(context)
    private val settings = AppSettings(context)

    override fun getCount(): Int = dataList.size

    override fun getItem(position: Int): Pomodore = dataList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
            ?: PomodoreItemBinding.bind(
                LayoutInflater.from(context).inflate(R.layout.pomodore_item, null)
            ).root
        val binder = PomodoreItemBinding.bind(view)
        val currentItem = getItem(position)

        binder.icHistory.setColorFilter(R.color.light_green1, PorterDuff.Mode.MULTIPLY)
        binder.textTitle.text = currentItem.title
        binder.textDescription.text = currentItem.description

        binder.textStartTime.text = currentItem.getFormatedDate(currentItem.startTime)
        binder.textEndTime.text = currentItem.getFormatedDate(currentItem.endTime)
        binder.textDifferenceTime.text = currentItem.getDifference()

        if(!settings.getBoolean(AppSettings.SHOW_HISTORY_DESC)) binder.textDescription.visibility = View.GONE

        binder.showHideDateInfo.setOnClickListener {
            if (!isShowed) {
                binder.datesContentLayout.visibility = View.VISIBLE
                LayoutAnimation.RotateAnimation(binder.showHideDateInfo).rotate(650, 0f, -180f)
                LayoutAnimation.ScaleAnimation(binder.datesContentLayout)
                    .executeOnY(650, 0f, 1f, action = {})
                isShowed = true
            } else {
                LayoutAnimation.RotateAnimation(binder.showHideDateInfo).rotate(650, -180f, 0f)
                LayoutAnimation.ScaleAnimation(binder.datesContentLayout)
                    .executeOnY(650, action = {
                        binder.datesContentLayout.visibility = View.GONE
                    })
                isShowed = false
            }
        }

        binder.btnDeletePomodore.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("Borrar")
            dialog.setMessage("¿Seguro que quieres borrar \"${currentItem.title}\" del historial?")
            dialog.setPositiveButton("Borrar") { _, _ ->
                LayoutAnimation.FadeAnimation(binder.container).animateAlpha(650, action = {
                    it?.fillAfter = false
                    dataList.remove(currentItem)
                    provider.deletePomodore(currentItem.id)
                    notifyDataSetChanged()
                    Toast.makeText(context, "Elemento eliminado", Toast.LENGTH_SHORT).show()
                })
            }
            dialog.setNegativeButton("Cancelar") {dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            dialog.show()
        }

        return view
    }

}