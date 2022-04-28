package com.yts.tymodoro.animations

import android.view.View
import android.view.animation.*
import android.widget.LinearLayout

sealed class LayoutAnimation(private val layout: View) {

    class ScaleAnimation(private val layout: View) : LayoutAnimation(layout){

        private var animation: android.view.animation.ScaleAnimation? = null

        fun executeOnY(duration: Long, startY: Float = 1f, endY: Float = 0f, action: (Animation?) -> Unit, startAction: (() -> Unit)? = null){
            animation = ScaleAnimation(
                1f, 1f,
                startY, endY,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f
            )
            animation?.duration = duration
            animation?.fillAfter = true
            animation?.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(animation: Animation?) {
                    startAction?.let {
                        it()
                    }
                }
                override fun onAnimationEnd(animation: Animation?) {
                    action(animation)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            layout.startAnimation(animation)
        }
    }

    class FadeAnimation(private val layout: View) : LayoutAnimation(layout) {

        private var animation: AlphaAnimation? = null

        fun animateAlpha(duration: Long, from: Float = 1f, to: Float = 0f, action: (Animation?) -> Unit){
            animation = AlphaAnimation(from, to)
            animation?.duration = duration
            animation?.fillAfter = true
            animation?.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    action(animation)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            layout.startAnimation(animation)
        }
    }

    class RotateAnimation(private val layout: View): LayoutAnimation(layout) {
        fun rotate(duration: Long, fromDegrees: Float, toDegrees: Float){
            val rotateAnimation = android.view.animation.RotateAnimation(fromDegrees, toDegrees,
                android.view.animation.RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.RotateAnimation.RELATIVE_TO_SELF, 0.5f)
            rotateAnimation.setInterpolator(layout.context, android.R.anim.decelerate_interpolator)
            rotateAnimation.duration = duration
            rotateAnimation.fillAfter = true
            layout.startAnimation(rotateAnimation)
        }
    }

}