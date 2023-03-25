package com.example.notesapp.data.repository

import android.content.Context
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_QUERY
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_START
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.utils.ConnectReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesRepository(
    private val notesDao: NotesDao,
    private val apiService: ApiService,
    private val connectReceiver: ConnectReceiver,
    private val applicationContext: Context
) {

    private var insertedOrEditedId: Long = 0L
    private var fixedTimeLoadedDate: Long = 0L
    private var successfulInitialDataUpload = false

    private val isNoteEdited = MutableStateFlow(false)
    val isNoteEditedFlow: StateFlow<Boolean> = isNoteEdited.asStateFlow()

    private val serviceError = MutableStateFlow("")
    val serviceErrorFlow: StateFlow<String> = serviceError.asStateFlow()

    private val isLoaded = MutableStateFlow(false)
    val isLoadedFlow: StateFlow<Boolean> = isLoaded.asStateFlow()

    fun timeLoadedBase(): Long = fixedTimeLoadedDate
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

    fun loadRemoutData(start: Boolean = false) {
        CoroutineScope(Dispatchers.Default).launch {
            isLoaded.value = true
            try {
                apiService.getAllNote(((if(start) TIME_DELAY_START else TIME_DELAY_QUERY)*1000).toLong()).apply {
                    fixedTimeLoadedDate = this.timeBase
                    successfulInitialDataUpload = true
                    val list: List<Notes> = this.fullList
                    notesDao.updateDatabase(list)
                }
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
            isLoaded.value = false
        }
    }

    fun getInitialDataUpload(): Boolean = successfulInitialDataUpload

    fun addNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if(connectReceiver.isConnectStatusFlow.value) {
                    val resultId: Long = apiService.addNote(note)
                    insertedOrEditedId = resultId
                    isNoteEdited.value = true
                } else {
                    serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
        }
    }

    fun deleteNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if(connectReceiver.isConnectStatusFlow.value) {
                    apiService.deleteNote(note)
                    isNoteEdited.value = true
                } else {
                    serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
        }
    }

}