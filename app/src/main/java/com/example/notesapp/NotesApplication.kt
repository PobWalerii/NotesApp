package com.example.notesapp

import android.app.Application
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.services.ServicesManager
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.ui.actionbar.AppActionBar
import dagger.hilt.android.HiltAndroidApp
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

    override fun onCreate() {
        super.onCreate()
        appSettings.init()
        connectReceiver.init()
        servicesManager.init()

        appActionBar.init()
        notesRepository.init()
    }

    override fun onTerminate() {
        super.onTerminate()
        connectReceiver.closeObserve()
        servicesManager.stopAllServices()
        notesRepository.clearResources()
        appActionBar.closeResources()
    }
}