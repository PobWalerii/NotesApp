package com.example.notesapp.data.remotebase.apiservice

import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.data.remotebase.remoteapi.RemoteApi
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val remoteApi: RemoteApi,
    private val appSettings: AppSettings,
): ApiService {

    override fun getChangeBaseTime() = remoteApi.getChangeBaseTime()

    override fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse  = runBlocking {
        appSettings.isConnectStatus.collect {
            if (!it) {
                throw Exception("Разрыв")
            }
            val response = remoteApi.getAllNote(firstLoad, firstRun)
            if (it) {
                response
            } else {
                throw Exception("Разрыв")
            }
        }

    }

    override fun modifyNote(note: RemoteNotes, type: Boolean): Long =
        remoteApi.modifyNote(note, type)


}