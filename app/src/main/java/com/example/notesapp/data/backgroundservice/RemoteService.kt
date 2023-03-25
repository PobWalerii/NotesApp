package com.example.notesapp.data.backgroundservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.notesapp.constants.KeyConstants.INTERVAL_REQUESTS
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class RemoteService: Service() {

    @Inject
    lateinit var notesRepository: NotesRepository
    @Inject
    lateinit var apiService: ApiService

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delay: Long = (INTERVAL_REQUESTS*1000).toLong()
        scope.launch {
            while (isActive) {
                delay(delay)
                val remoteBaseTime = apiService.getChangeBaseTime()
                val localBaseTime = notesRepository.timeLoadedBase()
                if(remoteBaseTime != localBaseTime) {
                    notesRepository.loadRemoutData()
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