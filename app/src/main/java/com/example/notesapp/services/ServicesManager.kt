package com.example.notesapp.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.MAX_RETRY_ATTEMPTS
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Singleton
class ServicesManager(
    appSettings: AppSettings,
    private val applicationContext: Context,
) {

    private val remoteServiceIntent = createRemoteServiceIntent()
    private val serviceIntent = createServiceIntent()

    private val firstLoad: StateFlow<Boolean> = appSettings.firstLoad
    private val createBackgroundRecords: StateFlow<Boolean> = appSettings.createBackgroundRecords
    private val isConnectStatus: StateFlow<Boolean> = appSettings.isConnectStatus
    private val isBackService: StateFlow<Boolean> = appSettings.isBackService
    private val isRemoteService: StateFlow<Boolean> = appSettings.isRemoteService

    private var job1: Job? = null
    private var job2: Job? = null
    private var job3: Job? = null

    private var actBackService: Boolean = false
    private var countActBackService: Int = 0
    private var actRemoteService: Boolean = false
    private var countActRemoteService: Int = 0

    fun init() {
        job1 = CoroutineScope(Dispatchers.Default).launch {
            createBackgroundRecords.collect { start ->
                CoroutineScope(Dispatchers.Main).launch {
                    actRemoteService = start
                    countActRemoteService = 0
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
        Toast.makeText(applicationContext,"ServicesManager init ok", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(applicationContext,"ServicesManager close ok", Toast.LENGTH_SHORT).show()
    }

    private fun stopRemoteService() {
        if(isRemoteService.value) {
            try {
                applicationContext.stopService(remoteServiceIntent)
            } catch (_: Exception) {
                noStartOrStopRemoteService()
            }
        }
    }
    private fun startRemoteService() {
        if(!isRemoteService.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    applicationContext.startForegroundService(remoteServiceIntent)
                } else {
                    applicationContext.startService(remoteServiceIntent)
                }
            } catch (_: Exception) {
                noStartOrStopRemoteService()
            }
        }
    }

    private fun stopService() {
        if(isBackService.value) {
            try {
                applicationContext.stopService(serviceIntent)
            } catch (_: Exception) {
                noStartOrStopService()
            }
        }
    }
    private fun startService() {
        if(!isBackService.value) {
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
            unexpectedMessage()
        }
    }

    private fun noStartOrStopRemoteService() {
        countActRemoteService ++
        if(countActRemoteService <= MAX_RETRY_ATTEMPTS) {
            if (actRemoteService) {
                startRemoteService()
            } else {
                stopRemoteService()
            }
        } else {
            unexpectedMessage()
        }
    }

    private fun createRemoteServiceIntent() = Intent(applicationContext, BackRemoteService::class.java)
    private fun createServiceIntent() = Intent(applicationContext, BackService::class.java)

    private fun unexpectedMessage() {
        Toast.makeText(applicationContext, R.string.service_error, Toast.LENGTH_LONG).show()
    }


}