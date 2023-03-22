package com.example.notesapp.data.repository

import android.content.Context
import android.widget.Toast
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

    private val insertedId = MutableStateFlow(0L)
    val insertedIdFlow: StateFlow<Long> = insertedId.asStateFlow()

    private val isNoteEdited = MutableStateFlow(false)
    val isNoteEditedFlow: StateFlow<Boolean> = isNoteEdited.asStateFlow()

    private val isNoteDeleted = MutableStateFlow(false)
    val isNoteDeletedFlow: StateFlow<Boolean> = isNoteDeleted.asStateFlow()

    private val serviceError = MutableStateFlow("")
    val serviceErrorFlow: StateFlow<String> = serviceError.asStateFlow()

    fun setStartFlowParameters() {
        insertedId.value = 0L
        isNoteEdited.value = false
    }

    fun loadDataBase(): Flow<List<Notes>> = notesDao.loadDataBase()

    fun getNoteById(noteId: Long): Flow<List<Notes>> =
        notesDao.getNoteById(noteId)

    fun addNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val resultId: Long = apiService.addNote(note)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(applicationContext, "Отправляем $resultId", Toast.LENGTH_LONG)
                        .show()
                    insertedId.value = resultId
                    isNoteEdited.value = true
                }
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
        }
    }

    fun deleteNote(note: Notes) {
            try {
                apiService.deleteNote(note)
                isNoteDeleted.value = true
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
    }

}