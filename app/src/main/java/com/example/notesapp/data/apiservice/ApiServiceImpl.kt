package com.example.notesapp.data.apiservice

import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val notesDao: NotesDao
): ApiService {


    override fun getAllNote(): List<Notes> {
        TODO("Not yet implemented")
    }

    override fun deleteNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            notesDao.deleteNote(note)
        }
    }
    override suspend fun addNote(note: Notes): Long {
        return notesDao.insertNote(note)
    }


    override fun editNote(note: Notes) {
        TODO("Not yet implemented")
    }




}