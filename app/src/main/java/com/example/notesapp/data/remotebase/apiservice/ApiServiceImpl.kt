package com.example.notesapp.data.remotebase.apiservice

import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.data.remotebase.remoteapi.RemoteApi
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val remoteApi: RemoteApi
): ApiService {

    override fun getChangeBaseTime() = remoteApi.getChangeBaseTime()

    override fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse =
        remoteApi.getAllNote(firstLoad,firstRun)

    override fun modifyNote(note: RemoteNotes, type: Boolean): Long =
        remoteApi.modifyNote(note, type)


}