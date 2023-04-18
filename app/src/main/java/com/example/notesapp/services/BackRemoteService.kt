package com.example.notesapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.notesapp.constants.KeyConstants.CHANNEL_IDD
import com.example.notesapp.constants.KeyConstants.NOTIFICATION_IDD
import com.example.notesapp.data.remotebase.database.dao.RemoteDao
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
@Singleton
class BackRemoteService: Service() {

    @Inject
    lateinit var appSettings: AppSettings
    @Inject
    lateinit var remoteDao: RemoteDao

    private var job: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = ServiceNotification.setNotification(applicationContext,CHANNEL_IDD)
        startForeground(NOTIFICATION_IDD, notification)

        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(appSettings.intervalCreateRecords.value * 1000L)
                withContext(Dispatchers.IO) { addRecord() }
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        appSettings.setIsRemoteService(true)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        job?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        appSettings.setIsRemoteService(false)
    }

    private suspend fun addRecord() {
        remoteDao.insertNote(
            RemoteNotes(
                0,
                "Remote",
                "Remote " + Date().time.toString(),
                Date().time
            )
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}