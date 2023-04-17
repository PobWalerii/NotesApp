package com.example.notesapp.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.notesapp.R
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.receivers.DateManager
import com.example.notesapp.services.ServicesManager
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.utils.AppSplashScreen.startSplash
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
    lateinit var dateManager: DateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        startSplash(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(applicationContext,"Вход", Toast.LENGTH_SHORT).show()
        appSettings.init()
        connectReceiver.init()
        dateManager.init()
        servicesManager.init()
        notesRepository.init()




    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragmentContainerView).navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        startSplash(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(applicationContext,"Выход",Toast.LENGTH_SHORT).show()

        notesRepository.clearResources()
        servicesManager.stopAllServices()
        dateManager.close()
        connectReceiver.close()
        appSettings.close()
    }

}