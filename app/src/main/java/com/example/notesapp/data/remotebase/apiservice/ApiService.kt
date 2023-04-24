package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
import android.widget.Toast
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.data.remotebase.remoteapi.RemoteApi
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService @Inject constructor(
    private val remoteApi: RemoteApi,
    appSettings: AppSettings,
    private val applicationContext: Context
) {

    private val isConnectStatus: StateFlow<Boolean> = appSettings.isConnectStatus
    //private var observe: Job? = null

    suspend fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse = withContext(Dispatchers.IO) {
        getRequest("load", firstLoad, firstRun) as NoteResponse
    }

    suspend fun modifyNote(note: RemoteNotes, type: Boolean): Long = withContext(Dispatchers.IO) {
        getRequest("edit", note = note, type = type) as Long
    }

    suspend fun getChangeBaseTime(): Long = withContext(Dispatchers.IO) {
        getRequest("date") as Long
    }

    private suspend fun getRequest(
        key: String,
        firstLoad: Boolean = false,
        firstRun: Boolean = false,
        note: Any = false,
        type: Boolean = false
    ): Any = supervisorScope {
        val resp = async {
            remoteApi.processingRequest(
                key,
                firstLoad,
                firstRun,
                note,
                type
            )
        }
        val observe = isConnectStatus.onEach {
            if(!it) {
                withContext(Dispatchers.Main) {
                    showMessage(key, firstLoad, firstRun)
                }
                resp.cancelAndJoin()
            }
        }.launchIn(this)
        val result = resp.await()
        observe.cancel()
        result
    }

    private fun showMessage(
        key: String,
        firstLoad: Boolean = false,
        firstRun: Boolean = false,
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val message = when (key) {
                "load" -> "zzzzzzz"
                "edit" -> "yyyyyyy"
                else -> ""
            }
            if(message.isNotEmpty()) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
    throw Exception(
        applicationContext.getString(
            if (firstRun || firstLoad) {
                R.string.interrupted_start_load
            } else {
                R.string.interrupted_update_load
            }
        )
    )

     */


}