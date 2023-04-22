package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
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
    private val appSettings: AppSettings,
    private val applicationContext: Context
) {

    val isConnectStatus: StateFlow<Boolean> = appSettings.isConnectStatus
    fun getChangeBaseTime() = remoteApi.getChangeBaseTime()

    suspend fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse = coroutineScope{
        val resp = async { remoteApi.getAllNote(firstLoad, firstRun) }
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
        CoroutineScope(Dispatchers.Default).launch {
            isConnectStatus.collect {
                if(!it) {
                    job.cancelAndJoin()
                }
            }
        }
    }

    fun modifyNote(note: RemoteNotes, type: Boolean): Long =
        remoteApi.modifyNote(note, type)
}