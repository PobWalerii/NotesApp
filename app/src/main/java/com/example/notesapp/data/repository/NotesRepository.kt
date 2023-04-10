package com.example.notesapp.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.notesapp.R
import com.example.notesapp.data.remotebase.apiservice.ApiService
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.services.BackRemoteService
import com.example.notesapp.services.BackService
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Singleton

@Singleton
class NotesRepository(
    private val notesDao: NotesDao,
    private val apiService: ApiService,
    private val connectReceiver: ConnectReceiver,
    private val appSettings: AppSettings,
    private val applicationContext: Context
) {

    private var job: Job? = null
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

    init {
        if(isConnectStatus.value) {
            loadRemoteData()
        }
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

    private fun observeConnectStatus() {
        CoroutineScope(Dispatchers.Default).launch {
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
                setStartSettings()
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

    private fun setStartSettings() {
        CoroutineScope(Dispatchers.Main).launch {
            if (firstRun.value) {
                appSettings.setFromAppFirstRun()
            }
            if (firstLoad.value) {
                appSettings.setFirstLoad()
                startRemoteService()
            }
        }
    }

    private fun startRemoteService() {
        val serviceIntent = Intent(applicationContext, BackService::class.java)
        val remoteServiceIntent = Intent(applicationContext, BackRemoteService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent)
            //applicationContext.startForegroundService(remoteServiceIntent)
        } else {
            applicationContext.startService(serviceIntent)
            //applicationContext.startService(remoteServiceIntent)
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