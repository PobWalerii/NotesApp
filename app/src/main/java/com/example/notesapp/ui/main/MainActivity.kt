package com.example.notesapp.ui.main

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import com.example.notesapp.R
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.services.ServicesManager
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.ui.actionbar.AppActionBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var servicesManager: ServicesManager
    @Inject
    lateinit var appSettings: AppSettings
    @Inject
    lateinit var notesRepository: NotesRepository
    @Inject
    lateinit var connectReceiver: ConnectReceiver
    @Inject
    lateinit var appActionBar: AppActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        handleSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notesRepository.init()
        servicesManager.init()
        connectReceiver.init()
        appActionBar.init()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragmentContainerView).navigateUp() || super.onSupportNavigateUp()
    }

    private fun handleSplashScreen() {
        val splashScreen = installSplashScreen()
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

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(applicationContext,"Stop All Services", Toast.LENGTH_SHORT).show()
        servicesManager.stopAllServices()
        connectReceiver.closeObserve()
        notesRepository.clearResources()
        appActionBar.closeResources()
        appSettings.setFirstLoad(true)
    }

}