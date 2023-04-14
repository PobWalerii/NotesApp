package com.example.notesapp.utils

import android.animation.ObjectAnimator
import android.app.Activity
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

object AppSplashScreen {

    fun startSplash(activity: Activity) {
        val splashScreen = activity.installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                0f
            )
            slideUp.interpolator = LinearInterpolator()
            slideUp.duration = 1000L
            slideUp.doOnEnd { splashScreenViewProvider.remove() }
            slideUp.start()
        }
    }

}
