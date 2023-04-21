package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
import com.example.notesapp.R
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.data.remotebase.remoteapi.RemoteApi
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val remoteApi: RemoteApi,
    private val appSettings: AppSettings,
    private val applicationContext: Context
): ApiService {

    private var _response: StateFlow<NoteResponse?> = MutableStateFlow(
        NoteResponse(
        0,
        emptyList<RemoteNotes>()
    )
    )
    private val response: StateFlow<NoteResponse?> = _response

    var job: Job? = null
    override fun getChangeBaseTime() = remoteApi.getChangeBaseTime()


    override fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse{
        getNotesResponse(firstLoad, firstRun)



            try {
                val response = remoteApi.getAllNote(firstLoad, firstRun)
                return response
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw Exception(
                        applicationContext.getString(
                            if (firstRun || firstLoad) {
                                R.string.interrupted_start_load
                            } else {
                                R.string.interrupted_update_load
                            }
                        )
                    )
                } else {
                    throw e
                }
            }
        }
    }

    private fun getNotesResponse(firstLoad: Boolean, firstRun: Boolean) {
        _response.value = remoteApi.getAllNote(firstLoad, firstRun)
    }

    override fun modifyNote(note: RemoteNotes, type: Boolean): Long =
        remoteApi.modifyNote(note, type)


}