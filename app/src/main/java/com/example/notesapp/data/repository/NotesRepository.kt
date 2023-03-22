package com.example.notesapp.data.repository

import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class NotesRepository(
    private val notesDao: NotesDao,
    private val apiService: ApiService
) {

    private val insertedId = MutableStateFlow(0L)
    val insertedIdFlow: StateFlow<Long> = insertedId.asStateFlow()

    private val isNoteEdited = MutableStateFlow(false)
    val isNoteEditedFlow: StateFlow<Boolean> = isNoteEdited.asStateFlow()

    private val isNoteDeleted = MutableStateFlow(false)
    val isNoteDeletedFlow: StateFlow<Boolean> = isNoteDeleted.asStateFlow()

    private val serviceError = MutableStateFlow("")
    val serviceErrorFlow: StateFlow<String> = serviceError.asStateFlow()

    fun loadDataBase(): Flow<List<Notes>> = notesDao.loadDataBase()

    fun getNoteById(noteId: Long): Flow<List<Notes>> =
        notesDao.getNoteById(noteId)


    fun addNote(note: Notes) {
        insertedId.value = 0L
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val resultId: Long = apiService.addNote(note)
                while (resultId == 0L) {
                    delay(10)
                }
                insertedId.value = resultId
                isNoteEdited.value = true
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