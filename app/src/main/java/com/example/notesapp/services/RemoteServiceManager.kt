package com.example.notesapp.services

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class RemoteServiceManager(
    private val appSettings: AppSettings,
    private val applicationContext: Context,
) {

    val remoteServiceIntent = Intent(applicationContext, BackRemoteService::class.java)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            appSettings.createBackgroundRecords.collect { start ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (start) {
                        startRemoteService()
                    } else {
                        stopRemoteService()
                    }
                }
            }
        }
    }

    fun stopRemoteService() {
        applicationContext.stopService(remoteServiceIntent)
    }
    private fun startRemoteService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(remoteServiceIntent)
        } else {
            applicationContext.startService(remoteServiceIntent)
        }
    }
}