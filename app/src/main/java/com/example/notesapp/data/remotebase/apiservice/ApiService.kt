package com.example.notesapp.data.remotebase.apiservice
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes

interface ApiService  {
    fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse
    fun modifyNote(note: RemoteNotes, type: Boolean): Long
    fun getChangeBaseTime(): Long
}