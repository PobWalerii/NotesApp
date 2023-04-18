package com.example.notesapp.data.remotebase.apiservice
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes

interface ApiService  {
    suspend fun getAllNote(): NoteResponse
    suspend fun addNote(note: RemoteNotes): Long
    suspend fun deleteNote(note: RemoteNotes)
    fun getChangeBaseTime(): Long



}