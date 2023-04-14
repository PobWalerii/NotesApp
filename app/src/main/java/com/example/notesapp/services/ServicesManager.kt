package com.example.notesapp.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.MAX_RETRY_ATTEMPTS
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Singleton
class ServicesManager(
    appSettings: AppSettings,
    connectReceiver: ConnectReceiver,
    private val applicationContext: Context,
) {

    private val remoteServiceIntent = createRemoteServiceIntent()
    private val serviceIntent = createServiceIntent()

    private val firstLoad: StateFlow<Boolean> = appSettings.firstLoad
    private val createBackgroundRecords: StateFlow<Boolean> = appSettings.createBackgroundRecords
    private val isConnectStatus: StateFlow<Boolean> = connectReceiver.isConnectStatusFlow

    private var job1: Job? = null
    private var job2: Job? = null
    private var job3: Job? = null

    private var actBackService: Boolean = false
    private var countActBackService: Int = 0

    fun init() {
        job1 = CoroutineScope(Dispatchers.Default).launch {
            createBackgroundRecords.collect { start ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (start) {
                        startRemoteService()
                    } else {
                        stopRemoteService()
                    }
                }
            }
        }

        job2 = CoroutineScope(Dispatchers.Default).launch {
            firstLoad.collect { isStartLoad ->
                if (!isStartLoad) {
                    startLocalService()
                }
            }
        }
    }

    private fun startLocalService() {
        job2?.cancel()
        job3 = CoroutineScope(Dispatchers.Default).launch {
            isConnectStatus.collect { isConnect ->
                CoroutineScope(Dispatchers.Main).launch {
                    actBackService = isConnect
                    countActBackService = 0
                    if (isConnect) {
                        startService()
                    } else {
                        Toast.makeText(applicationContext, "Wont Stop", Toast.LENGTH_LONG).show()
                        stopService()
                    }
                }
            }
        }
    }

    fun stopAllServices() {
        job1?.cancel()
        job2?.cancel()
        job3?.cancel()
        stopService()
        stopRemoteService()
    }

    private fun stopRemoteService() {
        applicationContext.stopService(remoteServiceIntent)
    }
    private fun startRemoteService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(remoteServiceIntent)
        } else {
            applicationContext.startService(remoteServiceIntent)
        }
    }

    private fun stopService() {
        try {
            applicationContext.stopService(serviceIntent)
        } catch (_: Exception) {
            noStartOrStopService()
        }
    }
    private fun startService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(serviceIntent)
            } else {
                applicationContext.startService(serviceIntent)
            }
        } catch (_: Exception) {
            noStartOrStopService()
        }
    }

    private fun noStartOrStopService() {
        countActBackService ++
        if(countActBackService <= MAX_RETRY_ATTEMPTS) {
            if (actBackService) {
                startService()
            } else {
                stopService()
            }
        } else {
            Toast.makeText(applicationContext, R.string.service_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun createRemoteServiceIntent() = Intent(applicationContext, remoteServiceClass())
    private fun createServiceIntent() = Intent(applicationContext, serviceClass())
    private fun serviceClass() = BackService::class.java
    private fun remoteServiceClass() = BackRemoteService::class.java

}