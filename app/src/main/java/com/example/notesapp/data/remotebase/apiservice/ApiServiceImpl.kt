package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
import com.example.notesapp.R
import com.example.notesapp.data.remotebase.database.dao.RemoteDao
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val remoteDao: RemoteDao,
    private val appSettings: AppSettings,
    private val applicationContext: Context
): ApiService {

    private var listNotes: List<RemoteNotes> = emptyList()
    private val isConnectStatus: StateFlow<Boolean> = appSettings.isConnectStatus
    private val firstLoad: StateFlow<Boolean> = appSettings.firstLoad
    private val startDelayValue: StateFlow<Int> = appSettings.startDelayValue
    private val queryDelayValue: StateFlow<Int> = appSettings.queryDelayValue
    private val operationDelayValue: StateFlow<Int> = appSettings.operationDelayValue
    private val listNotesFlow: Flow<List<RemoteNotes>> = remoteDao.loadDataBase()

    private var timeLoadBase: Long = 0L

    init {
        observeDataChange()
    }

    override fun getChangeBaseTime() = timeLoadBase

    private fun observeDataChange() {
        CoroutineScope(Dispatchers.IO).launch {
            listNotesFlow.collect {
                timeLoadBase = Date().time
                listNotes = it
            }
        }
    }

    override suspend fun getAllNote(): NoteResponse {
        val delayValue = if (firstLoad.value) {
            startDelayValue.value
        } else {
            queryDelayValue.value
        }
        if (appSettings.firstRun.value) {
            setStartData()
        }
        withContext(Dispatchers.IO) {
            delay((delayValue.coerceAtLeast(1)) * 1000L)
        }
        if (isConnectStatus.value) {
            return NoteResponse(timeLoadBase, listNotes)
        } else {
            throw Exception(applicationContext.getString(R.string.data_error))
        }
    }

    override suspend fun deleteNote(note: RemoteNotes) {
        withContext(Dispatchers.IO) {
            delay((operationDelayValue.value.coerceAtLeast(1)) * 1000L)
            if (isConnectStatus.value) {
                remoteDao.deleteNote(note)
            } else {
                throw Exception(applicationContext.getString(R.string.operation_failed))
            }
        }
    }

    override suspend fun addNote(note: RemoteNotes): Long = runBlocking {
        delay((operationDelayValue.value.coerceAtLeast(1)) * 1000L)
        if (isConnectStatus.value) {
            remoteDao.insertNote(note)
        } else {
            throw Exception(applicationContext.getString(R.string.operation_failed))
        }
    }

    private suspend fun setStartData() {
        val startList: List<RemoteNotes> =
            listOf(
                RemoteNotes(1,"Colors","Colors greatly influence our lives today.",1100000000000),
                RemoteNotes(2,"Find Woman","Scientists Find Woman Who Sees 99 Million More Colors than Others.",1300000000000),
                RemoteNotes(3,"Mental activity","Colors influence our moods and every type of mental activity.",1600000000000)
            )
        remoteDao.startDatabase(startList)
    }


}