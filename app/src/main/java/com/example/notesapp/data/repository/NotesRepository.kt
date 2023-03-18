package com.example.notesapp.data.repository

import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitis.Notes
import kotlinx.coroutines.flow.Flow

class NotesRepository(
    private val notesDao: NotesDao
) {

    fun loadDataBase(): Flow<List<Notes>> = notesDao.loadDataBase()

    suspend fun getNoteById(noteId: Long): Notes? =
        notesDao.getNoteById(noteId).firstOrNull()

}