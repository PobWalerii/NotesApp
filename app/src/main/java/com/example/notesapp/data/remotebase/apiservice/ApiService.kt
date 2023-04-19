package com.example.notesapp.data.remotebase.apiservice
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes

interface ApiService  {
    suspend fun getAllNote(): NoteResponse
    suspend fun modifyNote(note: RemoteNotes, type: Boolean): Long
    fun getChangeBaseTime(): Long
}