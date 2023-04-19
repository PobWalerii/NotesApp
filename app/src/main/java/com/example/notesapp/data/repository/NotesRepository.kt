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

    private val _serviceError = MutableStateFlow("")
    private val serviceError: StateFlow<String> = _serviceError.asStateFlow()

    private val _isLoad = MutableStateFlow(false)
    val isLoad: StateFlow<Boolean> = _isLoad.asStateFlow()

    private val _counterDelay = MutableStateFlow(false)
    val counterDelay: StateFlow<Boolean> = _counterDelay.asStateFlow()

    val listNotes: Flow<List<Notes>> = notesDao.loadDataBase()

    private val _isNoteEdited = MutableStateFlow(false)
    val isNoteEdited: StateFlow<Boolean> = _isNoteEdited.asStateFlow()

    private val _isNoteDeleted = MutableStateFlow(false)
    val isNoteDeleted: StateFlow<Boolean> = _isNoteDeleted.asStateFlow()

    private val _idInsertOrEdit = MutableStateFlow(0L)
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
                _isLoad.value = true
                apiService.getAllNote().apply {
                    fixedTimeLoadedDate = this.timeBase
                    val list: List<Notes> = this.fullList.map {
                        fromRemote(it)
                    }
                    try {
                        notesDao.updateDatabase(list)
                    } catch (e: Exception) {
                        _serviceError.value = e.message.toString()
                    }
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
                _isLoad.value = false
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

    fun addNote(note: Notes) {
        modifyNote(note, true)
    }
    fun deleteNote(note: Notes) {
        modifyNote(note, false)
    }

    private fun modifyNote(note: Notes, type: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if(isConnectStatus.value) {
                    _counterDelay.value = true
                    val resultId: Long = apiService.modifyNote(toRemote(note), type)
                    _idInsertOrEdit.value = resultId
                    if( type ) {
                        _isNoteEdited.value = true
                    } else {
                        _isNoteDeleted.value = true
                    }
                } else {
                    _serviceError.value = applicationContext.getString(R.string.operation_not_possible)
                }
            } catch (e: Exception) {
                _serviceError.value = e.message.toString()
            } finally {
                _counterDelay.value = false
                delay(10)
                _isNoteEdited.value = false
                _isNoteDeleted.value = false
            }
        }
    }

    fun setInsertOrEditId() {
        _idInsertOrEdit.value = 0L
    }

}