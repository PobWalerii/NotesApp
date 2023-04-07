package com.example.notesapp.data.apiservice
import com.example.notesapp.data.remotedatabase.model.NoteResponse
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.flow.StateFlow

interface ApiService  {
    suspend fun getAllNote(): NoteResponse
    suspend fun addNote(note: Notes): Long
    suspend fun deleteNote(note: Notes)
    fun getChangeBaseTime(): Long

}