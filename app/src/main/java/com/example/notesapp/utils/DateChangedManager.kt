package com.example.notesapp.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.notesapp.receivers.DateChangedBroadcastReceiver
import com.example.notesapp.ui.listnotes.NotesListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DateChangedManager(private val adapter: NotesListAdapter) {
    private val receiver = DateChangedBroadcastReceiver()

    fun register(context: Context) {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        filter.addAction(Intent.ACTION_TIME_TICK)
        context.registerReceiver(receiver, filter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(receiver)
    }

    fun observeDateChanged(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            receiver.isDateChangedFlow.collect { isDateChanged ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (isDateChanged) {
                        adapter.refresh()
                    }
                }
            }
        }
    }
}