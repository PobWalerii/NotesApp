package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
import com.example.notesapp.R
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

    suspend fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse =
        getRequest("load", firstLoad, firstRun) as NoteResponse

    suspend fun modifyNote(note: RemoteNotes, type: Boolean): Long =
        getRequest("edit", note = note, type = type) as Long

    suspend fun getChangeBaseTime(): Long =
        getRequest("date") as Long

    private suspend fun getRequest(
        key: String,
        firstLoad: Boolean = false,
        firstRun: Boolean = false,
        note: Any = false,
        type: Boolean = false
    ): Any = supervisorScope {

       try {
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
               if (!it) {
                   resp.cancelAndJoin()
               }
           }.launchIn(this)

           val result = resp.await()
           observe.cancel()

           result

       } catch (e: Exception) {
           throw CancellationException(messageException(key, firstLoad, firstRun))
       }
    }

    private fun messageException(
        key: String,
        firstLoad: Boolean,
        firstRun: Boolean): String {

        return applicationContext.getString(
            when (key) {
                "load" -> {
                    if (firstRun || firstLoad) {
                        R.string.interrupted_start_load
                    } else {
                        R.string.interrupted_update_load
                    }
                }
                "edit" -> R.string.data_changes_not_known
                else -> R.string.unknown_error
            }
        )
    }

}