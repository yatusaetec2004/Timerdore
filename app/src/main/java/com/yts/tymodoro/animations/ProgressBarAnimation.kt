package com.yts.tymodoro.animations

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar

class ProgressBarAnimation(val progressBar: ProgressBar, val from: Float, val to: Float): Animation() {

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        super.applyTransformation(interpolatedTime, t)
        val value: Float = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
    }

}