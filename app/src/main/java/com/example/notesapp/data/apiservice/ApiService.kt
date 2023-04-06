package com.example.notesapp.data.apiservice
import com.example.notesapp.data.remotedatabase.model.NoteResponse
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.flow.StateFlow

interface ApiService  {
    suspend fun getAllNote(firstRun: Boolean): NoteResponse
    suspend fun addNote(note: Notes, delayTime: Int): Long
    suspend fun deleteNote(note: Notes, delayTime: Int)
    fun getChangeBaseTime(): Long

    //val counterDelayFlow: StateFlow<Int>
}