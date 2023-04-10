package com.example.notesapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.notesapp.constants.KeyConstants.NOTIFICATION_ID
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.remotebase.database.RemoteDao
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BackRemoteService: Service() {

    @Inject
    lateinit var appSettings: AppSettings
    @Inject
    lateinit var remoteDao: RemoteDao

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = ServiceNotification.setNotification(applicationContext)
        startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            while (isActive) {
                delay(appSettings.intervalCreateRecords.value * 1000L)
                addRecord()
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private suspend fun addRecord() {
        remoteDao.insertNote(
            Notes(
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
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}