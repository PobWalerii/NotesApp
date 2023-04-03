package com.example.notesapp.data.repository

import android.content.Context
import android.widget.Toast
import com.example.notesapp.R
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.servicesandreceivers.ConnectReceiver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class NotesRepository(
    private val notesDao: NotesDao,
    private val apiService: ApiService,
    private val connectReceiver: ConnectReceiver,
    private val applicationContext: Context
) {

    private var insertedOrEditedId: Long = 0L
    private var fixedTimeLoadedDate: Long = 0L
    private var successfulInitialDataUpload = false

    private var startDelayValue: Long = 0L
    private var queryDelayValue: Long = 0L
    private var requestIntervalValue: Long = 0L
    private var operationDelayValue: Int = 0

    private var job: Job? = null

    private val isNoteEdited = MutableStateFlow(false)
    val isNoteEditedFlow: StateFlow<Boolean> = isNoteEdited.asStateFlow()

    private val serviceError = MutableStateFlow("")
    val serviceErrorFlow: StateFlow<String> = serviceError.asStateFlow()

    private val isLoaded = MutableStateFlow(false)
    val isLoadedFlow: StateFlow<Boolean> = isLoaded.asStateFlow()

    private val isRemoteDatabaseChanged = MutableStateFlow(false)
    val isRemoteDatabaseChangedFlow: StateFlow<Boolean> = isRemoteDatabaseChanged.asStateFlow()

    val counterDelayFlow: StateFlow<Int> = apiService.counterDelayFlow

    private var isLoadCanceled = false

    fun refreshDelaySettings(startDelay: Int, queryDelay: Int, requestInterval: Int, operationDelay: Int) {
        startDelayValue = (startDelay*1000).toLong()
        queryDelayValue = (queryDelay*1000).toLong()
        requestIntervalValue = (requestInterval*1000).toLong()
        operationDelayValue = operationDelay
    }

    fun setRemoteDatabaseChanged(timeRemote: Long) {
        if(fixedTimeLoadedDate != timeRemote) {
            isRemoteDatabaseChanged.value = true
        }
    }

    fun getRequestInterval(): Long = requestIntervalValue

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

    fun setIsLoadCanceled() {
        isLoadCanceled = true
    }

    fun getInitialDataUpload(): Boolean = successfulInitialDataUpload

    private fun observeErrorMessages() {
        CoroutineScope(Dispatchers.Default).launch {
            serviceErrorFlow.collect { message ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (message.isNotEmpty()) {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        serviceError.value = ""
                    }
                }
            }
        }
    }

    fun loadRemoteData(start: Boolean = false) {
        if(start) {
            observeErrorMessages()
        }
        job = CoroutineScope(Dispatchers.Default).launch {
            isLoadCanceled = false
            isLoaded.value = true
            try {
                apiService.getAllNote(if (start) startDelayValue else queryDelayValue).apply {
                    fixedTimeLoadedDate = this.timeBase
                    isRemoteDatabaseChanged.value = false
                    successfulInitialDataUpload = true
                    val list: List<Notes> = this.fullList
                    notesDao.updateDatabase(list)
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    isLoadCanceled = true
                    serviceError.value =
                        if (start) {
                            applicationContext.getString(R.string.interrupted_start_load)
                        } else {
                            applicationContext.getString(R.string.interrupted_update_load)
                        }
                } else {
                    serviceError.value = e.message.toString()
                }
            } finally {
                isLoaded.value = false
            }
        }
    }

    fun stopLoadRemoteData() {
        CoroutineScope(Dispatchers.Default).launch {
            job?.cancel()
        }
    }
    fun restartLoadRemoteData(start: Boolean) {
        if(isLoadCanceled) {
            loadRemoteData(start)
        }
    }

    fun addNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if(isRemoteConnect()) {
                    val resultId: Long = apiService.addNote(note, operationDelayValue)
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
                if(isRemoteConnect()) {
                    apiService.deleteNote(note, operationDelayValue)
                    isNoteEdited.value = true
                } else {
                    serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            }
        }
    }

    private fun isRemoteConnect(): Boolean {
        return connectReceiver.isConnectStatusFlow.value
    }

}