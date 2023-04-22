package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
import android.widget.Toast
import com.example.notesapp.R
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.data.remotebase.remoteapi.RemoteApi
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService @Inject constructor(
    private val remoteApi: RemoteApi,
    appSettings: AppSettings,
    private val applicationContext: Context
) {

    private val isConnectStatus: StateFlow<Boolean> = appSettings.isConnectStatus
    private var observe: Job? = null

    fun getChangeBaseTime(): Long {
        return if (isConnectStatus.value) {
            try {
                remoteApi.getChangeBaseTime()
            } catch (e: Exception) {
                throw e
            }
        } else {
            0L
        }
    }

    suspend fun getAllNote(firstLoad: Boolean, firstRun: Boolean): Any = runBlocking {
        val resp = async { remoteApi.getAllNote(firstLoad, firstRun) }
        observe?.cancel()
        observeConnect(resp)
        try {
            resp.await()
        } catch (e: Exception) {
            throw Exception(
                applicationContext.getString(
                    if (firstRun || firstLoad) {
                        R.string.interrupted_start_load
                    } else {
                        R.string.interrupted_update_load
                    }
                )
            )
        }
    }

    private fun observeConnect(job: Job) {
        observe = CoroutineScope(Dispatchers.Default).launch {
            isConnectStatus.collect {
                if(!it) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, R.string.terminated_processes, Toast.LENGTH_SHORT).show()
                    }
                    job.cancelAndJoin()
                }
            }
        }
    }

    fun modifyNote(note: RemoteNotes, type: Boolean): Long =
        remoteApi.modifyNote(note, type)
}