package com.example.notesapp.data.apiservice
import com.example.notesapp.data.database.entitys.Notes

interface ApiService  {
    fun getAllNote(): List<Notes>
    suspend fun addNote(note: Notes): Long
    suspend fun deleteNote(note: Notes)
}