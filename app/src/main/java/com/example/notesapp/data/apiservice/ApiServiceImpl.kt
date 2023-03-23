package com.example.notesapp.data.apiservice

import com.example.notesapp.data.apiservice.database.RemoteDao
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val notesDao: NotesDao,
    private val remoteDao: RemoteDao
): ApiService {
    override fun getAllNote(): List<Notes> {
        TODO("Not yet implemented")
    }
    override suspend fun deleteNote(note: Notes) {
        notesDao.deleteNote(note)
    }
    override suspend fun addNote(note: Notes): Long {
        return notesDao.insertNote(note)
        //return remoteDao.insertNote(note)
    }
}