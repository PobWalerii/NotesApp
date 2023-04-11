package com.example.notesapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.notesapp.constants.KeyConstants.CHANNEL_ID
import com.example.notesapp.constants.KeyConstants.NOTIFICATION_ID
import com.example.notesapp.data.remotebase.apiservice.ApiService
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.services.ServiceNotification.setNotification
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

        //val notification = setNotification(applicationContext,CHANNEL_ID)
        //startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            while (isActive) {
                delay(appSettings.requestIntervalValue.value*1000L)
                if(connectReceiver.isConnectStatusFlow.value) {
                    try {
                        val remoteBaseTime = apiService.getChangeBaseTime()
                        notesRepository.setRemoteBaseTime(remoteBaseTime)
                    } catch (_: Exception) {}
                }
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        scope.cancel()
        //stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    //override fun onDestroy() {
    //    super.onDestroy()
    //    scope.cancel()
    //}
}