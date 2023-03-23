package com.example.notesapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DateChangedBroadcastReceiver : BroadcastReceiver() {

    private val isDateChanged = MutableStateFlow(false)
    val isDateChangedFlow: StateFlow<Boolean> = isDateChanged.asStateFlow()

    override fun onReceive(context: Context, intent: Intent) {
        isDateChanged.value = true
    }

}