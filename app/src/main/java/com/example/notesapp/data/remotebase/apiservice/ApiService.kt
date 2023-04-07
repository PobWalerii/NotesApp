package com.example.notesapp.data.remotebase.apiservice
import com.example.notesapp.data.remotebase.model.NoteResponse
import com.example.notesapp.data.database.entitys.Notes

interface ApiService  {
    suspend fun getAllNote(): NoteResponse
    suspend fun addNote(note: Notes): Long
    suspend fun deleteNote(note: Notes)
    fun getChangeBaseTime(): Long

}