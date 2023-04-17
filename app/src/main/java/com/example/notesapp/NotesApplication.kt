package com.example.notesapp

import android.app.Application
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.receivers.DateManager
import com.example.notesapp.services.ServicesManager
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.ui.actionbar.AppActionBar
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NotesApplication : Application() {

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
    @Inject
    lateinit var dateManager: DateManager

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Main).launch {
            appSettings.init()
            notesRepository.init()
            connectReceiver.init()
            servicesManager.init()
            dateManager.init()
        }

    }

    override fun onTerminate() {
        super.onTerminate()
        connectReceiver.close()
        servicesManager.stopAllServices()
        notesRepository.clearResources()
        dateManager.close()
    }
}