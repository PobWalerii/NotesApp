package com.example.notesapp.data.repository

import android.content.Context
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesRepository(
    private val notesDao: NotesDao,
    private val apiService: ApiService,
    private val applicationContext: Context
) {

    private var insertedOrEditedId: Long = 0L

    private val isNoteEdited = MutableStateFlow(false)
    val isNoteEditedFlow: StateFlow<Boolean> = isNoteEdited.asStateFlow()

    private val serviceError = MutableStateFlow("")
    val serviceErrorFlow: StateFlow<String> = serviceError.asStateFlow()

    fun setInsertedOrEditedIdNull() {
        insertedOrEditedId = 0L
    }
    fun getInsertedOrEditedIdValue(): Long = insertedOrEditedId

    fun setStartFlowParameters() {
        isNoteEdited.value = false
        serviceError.value = ""
    }

    fun loadDataBase(): Flow<List<Notes>> = notesDao.loadDataBase()

    fun getNoteById(noteId: Long): Flow<Notes?> =
        flow {
            emit(notesDao.getNoteById(noteId).firstOrNull())
        }

    fun addNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val resultId: Long = apiService.addNote(note)
                insertedOrEditedId = resultId
                isNoteEdited.value = true
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
        }
    }

    fun deleteNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                apiService.deleteNote(note)
                isNoteEdited.value = true
                //isNoteDeleted.value = true
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
        }
    }

}