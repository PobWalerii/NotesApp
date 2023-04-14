package com.example.notesapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.example.notesapp.constants.KeyConstants.CHANNEL_ID
import com.example.notesapp.constants.KeyConstants.NOTIFICATION_ID
import com.example.notesapp.data.remotebase.apiservice.ApiService
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.services.ServiceNotification.setNotification
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
@Singleton
class BackService: Service() {

    @Inject
    lateinit var notesRepository: NotesRepository
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var appSettings: AppSettings

    private var job: Job? = null
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = setNotification(applicationContext, CHANNEL_ID)
        startForeground(NOTIFICATION_ID, notification)

        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                withContext(Dispatchers.Main) {setRuning()}
                delay(appSettings.requestIntervalValue.value * 1000L)
                try {
                    val remoteBaseTime = apiService.getChangeBaseTime()
                    notesRepository.setRemoteBaseTime(remoteBaseTime)
                } catch (_: Exception) { }
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        job?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        job?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun getRuning() = isRunning
    private fun setRuning() {
        isRunning = true
        Toast.makeText(applicationContext, "сервис поставил $isRunning", Toast.LENGTH_LONG).show()
        Toast.makeText(applicationContext, "getRuning() ${getRuning()}", Toast.LENGTH_LONG).show()
    }
}