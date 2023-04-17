package com.example.notesapp.data.remotebase.apiservice
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.localbase.entitys.Notes

interface ApiService  {
    suspend fun getAllNote(): NoteResponse
    suspend fun addNote(note: Notes): Long
    suspend fun deleteNote(note: Notes)
    fun getChangeBaseTime(): Long



}