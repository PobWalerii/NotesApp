package com.example.notesapp.data.remotebase.apiservice

import android.content.Context
import android.widget.Toast
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

    private var timeLoadBase: Long = 0L
    private var listNotes: List<Notes> = emptyList()

    val isConnectStatus: StateFlow<Boolean> = connectReceiver.isConnectStatusFlow
    val firstRun: StateFlow<Boolean> = appSettings.firstRun

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

    private fun setStartData() {
        val startList: List<Notes> =
            listOf(
                Notes(0, "Colors", "Colors greatly influence our lives today.", 1100000000000),
                Notes(0,"Find Woman","Scientists Find Woman Who Sees 99 Million More Colors than Others.",1300000000000),
                Notes(0,"Mental activity","Colors influence our moods and every type of mental activity.",1600000000000)
            )
        CoroutineScope(Dispatchers.IO).launch {
            remoteDao.startDatabase(startList)
        }
    }

    override suspend fun getAllNote(): NoteResponse {
        if (firstRun.value) {
            setStartData()
        }
        delay(
            ((if (firstRun.value) {
                appSettings.startDelayValue.value
            } else {
                appSettings.queryDelayValue.value
            }) * 1000).toLong()
        )
        if (isConnectStatus.value) {
            return NoteResponse(timeLoadBase, listNotes)
        } else {
            throw Exception(applicationContext.getString(R.string.data_error))
        }
    }

    override suspend fun deleteNote(note: Notes) {
        delay((appSettings.operationDelayValue.value * 1000).toLong())
        if (isConnectStatus.value) {
            remoteDao.deleteNote(note)
        } else {
            throw Exception(applicationContext.getString(R.string.operation_failed))
        }
    }
    override suspend fun addNote(note: Notes): Long {
        delay((appSettings.operationDelayValue.value * 1000).toLong())
        if (isConnectStatus.value) {
            return remoteDao.insertNote(note)
        } else {
            throw Exception(applicationContext.getString(R.string.operation_failed))
        }
    }

}