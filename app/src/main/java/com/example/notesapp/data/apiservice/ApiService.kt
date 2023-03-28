package com.example.notesapp.data.apiservice
import com.example.notesapp.data.remotedatabase.model.NoteResponse
import com.example.notesapp.data.database.entitys.Notes

interface ApiService  {
    suspend fun getAllNote(delayTime: Long): NoteResponse
    suspend fun addNote(note: Notes, delayTime: Long): Long
    suspend fun deleteNote(note: Notes)
    fun getChangeBaseTime(): Long
}