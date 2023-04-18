package com.example.notesapp.data.repository

import android.content.Context
import android.widget.Toast
import com.example.notesapp.R
import com.example.notesapp.data.remotebase.apiservice.ApiService
import com.example.notesapp.data.localbase.dao.NotesDao
import com.example.notesapp.data.localbase.entitys.Notes
import com.example.notesapp.data.mapers.NotesMaper.fromRemote
import com.example.notesapp.data.mapers.NotesMaper.toRemote
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val notesDao: NotesDao,
    private val apiService: ApiService,
    private val appSettings: AppSettings,
    private val applicationContext: Context,
) {

    private var job: Job? = null
    private var connect: Job? = null
    private var message: Job? = null
    private var fixedTimeLoadedDate: Long = 0
    private var fixedTimeRemoteDate: Long = 1

    val isConnectStatus: StateFlow<Boolean> = appSettings.isConnectStatus

    val firstRun: StateFlow<Boolean> = appSettings.firstRun
    private val firstLoad: StateFlow<Boolean> = appSettings.firstLoad

    private val isNoteEdited = MutableStateFlow(false)
    val isNoteEditedFlow: StateFlow<Boolean> = isNoteEdited.asStateFlow()

    private val _serviceError = MutableStateFlow("")
    private val serviceError: StateFlow<String> = _serviceError.asStateFlow()

    private val isLoad = MutableStateFlow(false)
    val isLoadFlow: StateFlow<Boolean> = isLoad.asStateFlow()

    private val counterDelay = MutableStateFlow(false)
    val counterDelayFlow: StateFlow<Boolean> = counterDelay.asStateFlow()

    val listNotesFlow: Flow<List<Notes>> = notesDao.loadDataBase()

    private val _idInsertOrEdit = MutableStateFlow(-1L)
    val idInsertOrEdit: StateFlow<Long> = _idInsertOrEdit.asStateFlow()

    fun init() {
        observeConnectStatus()
        observeErrorMessages()
    }

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
            serviceError.collect { message ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (message.isNotEmpty()) {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        _serviceError.value = ""
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
    }

    private fun loadRemoteData() {
        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                isLoad.value = true
                apiService.getAllNote().apply {
                    fixedTimeLoadedDate = this.timeBase
                    val list: List<Notes> = this.fullList.map {
                        fromRemote(it)
                    }
                    notesDao.updateDatabase(list)
                }
                if( firstLoad.value ) {
                    actionAfterStart()
                }
            } catch (e: Exception) {
                _serviceError.value =
                    if (e is CancellationException) {
                        applicationContext.getString(
                            if(firstRun.value || firstLoad.value) {
                                R.string.interrupted_start_load
                            } else {
                                R.string.interrupted_update_load
                            }
                        )
                    } else {
                        e.message.toString()
                    }
            } finally {
                isLoad.value = false
            }
        }
    }

    private fun actionAfterStart() {
        if (firstRun.value) {
            appSettings.setAppFirstRun()
        }
        appSettings.setFirstLoad()
        fixedTimeRemoteDate = fixedTimeLoadedDate
    }

    fun addNote(note: Notes, fixed: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if(isConnectStatus.value) {
                    counterDelay.value = true
                    val resultId: Long = apiService.addNote(toRemote(note))
                    _idInsertOrEdit.value = resultId
                    if(fixed) {
                        isNoteEdited.value = true
                    }
                } else {
                    _serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                _serviceError.value = e.message.toString()
            } finally {
                counterDelay.value = false
                delay(10)
                isNoteEdited.value = false
            }
        }
    }

    fun getInsertOrEditId(): Long {
        val current = idInsertOrEdit.value
        _idInsertOrEdit.value = -1L
        return current
    }
    fun setInsertOrEditId(current: Long) {
        _idInsertOrEdit.value = current
    }

    fun deleteNote(note: Notes) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if(isConnectStatus.value) {
                    counterDelay.value = true
                    apiService.deleteNote(toRemote(note))
                    isNoteEdited.value = true
                } else {
                    _serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                _serviceError.value = e.message.toString()
            } finally {
                counterDelay.value = false
                delay(10)
                isNoteEdited.value = false
            }
        }
    }

}