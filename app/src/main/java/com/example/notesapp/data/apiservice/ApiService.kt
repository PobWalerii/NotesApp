package com.example.notesapp.data.apiservice
import com.example.notesapp.data.apiservice.model.NoteResponse
import com.example.notesapp.data.database.entitys.Notes

interface ApiService  {
    suspend fun getAllNote(delayTime: Long): NoteResponse
    suspend fun addNote(note: Notes): Long
    suspend fun deleteNote(note: Notes)
}