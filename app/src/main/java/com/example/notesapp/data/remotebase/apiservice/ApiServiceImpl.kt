package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
import com.example.notesapp.R
import com.example.notesapp.data.remotebase.database.RemoteDao
import com.example.notesapp.data.remotebase.model.NoteResponse
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val remoteDao: RemoteDao,
    connectReceiver: ConnectReceiver,
    private val appSettings: AppSettings,
    private val applicationContext: Context
): ApiService {

    private var listNotes: List<Notes> = emptyList()

    private val isConnectStatus: StateFlow<Boolean> = connectReceiver.isConnectStatusFlow

    private var timeLoadBase: Long = 0L

    init {
        observeDataChange()
    }

    override fun getChangeBaseTime() = timeLoadBase

    private fun observeDataChange() {
        CoroutineScope(Dispatchers.IO).launch {
            remoteDao.loadDataBaseFlow().collect {
                listNotes = it
                timeLoadBase = Date().time
            }
        }
    }

    override suspend fun getAllNote(): NoteResponse {
        setStartData()
        withContext(Dispatchers.Main) {
            val delayValue = if (appSettings.firstLoad.value) {
                appSettings.startDelayValue.value
            } else {
                appSettings.queryDelayValue.value
            }
            withContext(Dispatchers.IO) {
                delay(delayValue * 1000L)
            }
        }
        if (isConnectStatus.value) {
            return NoteResponse(timeLoadBase, listNotes)
        } else {
            throw Exception(applicationContext.getString(R.string.data_error))
        }
    }

    override suspend fun deleteNote(note: Notes) {
        withContext(Dispatchers.IO) {
            delay(appSettings.operationDelayValue.value * 1000L)
        }
        if (isConnectStatus.value) {
            remoteDao.deleteNote(note)
        } else {
            throw Exception(applicationContext.getString(R.string.operation_failed))
        }
    }

    override suspend fun addNote(note: Notes): Long {
        withContext(Dispatchers.IO) {
            delay(appSettings.operationDelayValue.value * 1000L)
        }
        if (isConnectStatus.value) {
            return remoteDao.insertNote(note)
        } else {
            throw Exception(applicationContext.getString(R.string.operation_failed))
        }
    }

    private fun setStartData() {
        CoroutineScope(Dispatchers.Main).launch {
            if (appSettings.firstRun.value) {
                val startList: List<Notes> =
                    listOf(
                        Notes(
                            1,
                            "Colors",
                            "Colors greatly influence our lives today.",
                            1100000000000
                        ),
                        Notes(
                            2,
                            "Find Woman",
                            "Scientists Find Woman Who Sees 99 Million More Colors than Others.",
                            1300000000000
                        ),
                        Notes(
                            3,
                            "Mental activity",
                            "Colors influence our moods and every type of mental activity.",
                            1600000000000
                        )
                    )
                CoroutineScope(Dispatchers.IO).launch {
                    remoteDao.startDatabase(startList)
                }
            }
        }
    }

}