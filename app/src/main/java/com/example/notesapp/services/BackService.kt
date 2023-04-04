package com.example.notesapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BackService: Service() {

    @Inject
    lateinit var notesRepository: NotesRepository
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var connectReceiver: ConnectReceiver
    @Inject
    lateinit var appSettings: AppSettings

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            while (isActive) {
                delay((appSettings.requestIntervalValue.value*1000).toLong())
                if(connectReceiver.isConnectStatusFlow.value) {
                    try {
                        val remoteBaseTime = apiService.getChangeBaseTime()
                        notesRepository.setRemoteDatabaseChanged(remoteBaseTime)
                    } catch (_: Exception) {}
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}