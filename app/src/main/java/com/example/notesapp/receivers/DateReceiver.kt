package com.example.notesapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.notesapp.settings.AppSettings
import javax.inject.Singleton

@Singleton
class DateManager(
    appSettings: AppSettings,
    private val applicationContext: Context,
) {

    private val receiver = DateChangedBroadcastReceiver(appSettings)

    fun init() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        applicationContext.registerReceiver(receiver, filter)
    }
    fun close() {
        applicationContext.unregisterReceiver(receiver)
    }
}

class DateChangedBroadcastReceiver(
    private val appSettings: AppSettings
): BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        appSettings.setIsDateChanged(true)
    }
}