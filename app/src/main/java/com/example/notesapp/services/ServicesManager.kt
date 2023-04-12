package com.example.notesapp.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class ServicesManager(
    private val appSettings: AppSettings,
    private val connectReceiver: ConnectReceiver,
    private val applicationContext: Context,
) {

    private val remoteServiceIntent = Intent(applicationContext, BackRemoteService::class.java)
    private val serviceIntent = Intent(applicationContext, BackService::class.java)

    private var job1: Job? = null
    private var job2: Job? = null
    private var job3: Job? = null

    fun init() {
        job1 = CoroutineScope(Dispatchers.Default).launch {
            appSettings.createBackgroundRecords.collect { start ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (start) {
                        Toast.makeText(applicationContext,"Start Service Remote Base",Toast.LENGTH_SHORT).show()
                        startRemoteService()
                    } else {
                        Toast.makeText(applicationContext,"Stop Service Remote Base",Toast.LENGTH_SHORT).show()
                        stopRemoteService()
                    }
                }
            }
        }
        job2 = CoroutineScope(Dispatchers.Default).launch {
            appSettings.firstLoad.collect { isStartLoad ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (!isStartLoad && connectReceiver.isConnectStatusFlow.value) {
                        Toast.makeText(applicationContext,"Start Service variant 1",Toast.LENGTH_SHORT).show()
                        startService()
                    }
                }
            }
        }

        job3 =CoroutineScope(Dispatchers.Default).launch {
            connectReceiver.isConnectStatusFlow.collect { isConnect ->
                CoroutineScope(Dispatchers.Main).launch {
                    if(!appSettings.firstLoad.value) {
                        if (isConnect) {
                            Toast.makeText(applicationContext,"Start Service variant 2",Toast.LENGTH_SHORT).show()
                            startService()
                        } else {
                            Toast.makeText(applicationContext,"Stop Service",Toast.LENGTH_SHORT).show()
                            stopService()
                        }
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