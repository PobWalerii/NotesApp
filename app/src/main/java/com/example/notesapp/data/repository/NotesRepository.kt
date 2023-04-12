package com.example.notesapp.data.repository

import android.content.Context
import android.widget.Toast
import com.example.notesapp.R
import com.example.notesapp.data.remotebase.apiservice.ApiService
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val notesDao: NotesDao,
    private val apiService: ApiService,
    connectReceiver: ConnectReceiver,
    private val appSettings: AppSettings,
    private val applicationContext: Context,
) {

    private var job: Job? = null
    private var connect: Job? = null
    private var message: Job? = null
    private var fixedTimeLoadedDate: Long = 0
    private var fixedTimeRemoteDate: Long = 0
    val isConnectStatus: StateFlow<Boolean> = connectReceiver.isConnectStatusFlow
    val firstRun: StateFlow<Boolean> = appSettings.firstRun
    private val firstLoad: StateFlow<Boolean> = appSettings.firstLoad

    private val isNoteEdited = MutableStateFlow(false)
    val isNoteEditedFlow: StateFlow<Boolean> = isNoteEdited.asStateFlow()

    private val serviceError = MutableStateFlow("")
    private val serviceErrorFlow: StateFlow<String> = serviceError.asStateFlow()

    private val isLoad = MutableStateFlow(false)
    val isLoadFlow: StateFlow<Boolean> = isLoad.asStateFlow()

    private val counterDelay = MutableStateFlow(false)
    val counterDelayFlow: StateFlow<Boolean> = counterDelay.asStateFlow()

    private var idInsertOrEdit: Long = 0

    fun init() {
        Toast.makeText(applicationContext,"Start Repositiory",Toast.LENGTH_SHORT).show()
        observeErrorMessages()
        observeConnectStatus()
    }

    fun loadDataBase(): Flow<List<Notes>> = notesDao.loadDataBase()

    suspend fun getNoteById(noteId: Long): Notes? =
        notesDao.getNoteById(noteId).firstOrNull()

    fun setRemoteBaseTime(timeRemote: Long) {
        if(fixedTimeLoadedDate != timeRemote) {
            fixedTimeRemoteDate = timeRemote
            loadRemoteData()
        }
    }

    private fun observeErrorMessages() {
        message = CoroutineScope(Dispatchers.Default).launch {
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

    private fun observeConnectStatus() {
        connect = CoroutineScope(Dispatchers.Main).launch {
            isConnectStatus.collect { isConnect ->
                if( isConnect ) {
                    if(firstLoad.value || fixedTimeLoadedDate != fixedTimeRemoteDate) {
                        loadRemoteData()
                    }
                } else {
                    job?.cancel()
                }
            }
        }
    }

    fun clearResources() {
        connect?.cancel()
        message?.cancel()
        job?.cancel()
    }

    private fun loadRemoteData() {
        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    isLoad.value = true
                }
                apiService.getAllNote().apply {
                    fixedTimeLoadedDate = this.timeBase
                    val list: List<Notes> = this.fullList
                    notesDao.updateDatabase(list)
                }
                if( firstLoad.value ) {
                    actionAfterStart()
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    serviceError.value =
                        if (firstRun.value || firstLoad.value) {
                            applicationContext.getString(R.string.interrupted_start_load)
                        } else {
                            applicationContext.getString(R.string.interrupted_update_load)
                        }
                } else {
                    serviceError.value = e.message.toString()
                }
            } finally {
                isLoad.value = false
            }
        }
    }

    private fun actionAfterStart() {
        CoroutineScope(Dispatchers.Main).launch {
            if (firstRun.value) {
                appSettings.setAppFirstRun()
            }
            appSettings.setFirstLoad()
            fixedTimeRemoteDate = fixedTimeLoadedDate
        }
    }

    fun addNote(note: Notes, fixed: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if(isConnectStatus.value) {
                    counterDelay.value = true
                    val resultId: Long = apiService.addNote(note)
                    idInsertOrEdit = resultId
                    if(fixed) {
                        isNoteEdited.value = true
                    }
                } else {
                    serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            } finally {
                counterDelay.value = false
                delay(10)
                isNoteEdited.value = false
            }
        }
    }

    fun getInsertOrEditId(): Long {
        val current = idInsertOrEdit
        idInsertOrEdit = 0
        return current
    }
    fun setInsertOrEditId(current: Long) {
        idInsertOrEdit = current
    }

    fun deleteNote(note: Notes) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if(isConnectStatus.value) {
                    counterDelay.value = true
                    apiService.deleteNote(note)
                    isNoteEdited.value = true
                } else {
                    serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                serviceError.value = e.message.toString()
            } finally {
                counterDelay.value = false
                delay(10)
                isNoteEdited.value = false
            }
        }
    }

}