package com.example.notesapp.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import javax.inject.Singleton

@Singleton
class ServicesManager(
    private val appSettings: AppSettings,
    private val connectReceiver: ConnectReceiver,
    private val applicationContext: Context,
) {

    private val remoteServiceIntent = Intent(applicationContext, BackRemoteService::class.java)
    private val serviceIntent = Intent(applicationContext, BackService::class.java)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun init() {
        coroutineScope.launch {
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
        coroutineScope.launch {
            appSettings.firstLoad.collect { isStartLoad ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (!isStartLoad && connectReceiver.isConnectStatusFlow.value) {
                        startService()
                    }
                }
            }
        }

        coroutineScope.launch {
            connectReceiver.isConnectStatusFlow.collect { isConnect ->
                CoroutineScope(Dispatchers.Main).launch {
                    if(!appSettings.firstLoad.value) {
                        if (isConnect) {
                            startService()
                        } else {
                            stopService()
                        }
                    }
                }
            }
        }
    }

    fun stopAllServices() {
        coroutineScope.cancel()
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
        applicationContext.stopService(serviceIntent)
    }
    private fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent)
        } else {
            applicationContext.startService(serviceIntent)
        }
    }


}