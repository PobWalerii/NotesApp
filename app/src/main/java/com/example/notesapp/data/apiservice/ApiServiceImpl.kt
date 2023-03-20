package com.example.notesapp.data.apiservice

import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val notesDao: NotesDao
): ApiService {


    override fun getAllNote(): List<Notes> {
        TODO("Not yet implemented")
    }

    override suspend fun addNote(note: Notes) {
        notesDao.insertNote(note)
    }

    override fun editNote(note: Notes) {
        TODO("Not yet implemented")
    }

    override fun deleteNote(note: Notes) {
        TODO("Not yet implemented")
    }


}