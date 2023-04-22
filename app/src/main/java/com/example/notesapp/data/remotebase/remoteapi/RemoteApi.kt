package com.example.notesapp.data.remotebase.remoteapi

import com.example.notesapp.constants.KeyConstants
import com.example.notesapp.data.remotebase.database.dao.RemoteDao
import com.example.notesapp.data.remotebase.database.model.NoteResponse
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import com.example.notesapp.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteApi @Inject constructor(
    private val remoteDao: RemoteDao,
    appSettings: AppSettings,
) {

    private val startDelayValue: StateFlow<Int> = appSettings.startDelayValue
    private val queryDelayValue: StateFlow<Int> = appSettings.queryDelayValue
    private val operationDelayValue: StateFlow<Int> = appSettings.operationDelayValue

    private val listNotesFlow: Flow<List<RemoteNotes>> = remoteDao.loadDataBase()
    private var listNotes: List<RemoteNotes> = emptyList()
    private var timeLoadBase: Long = 0L

    init {
        observeDataChange()
    }

    fun getChangeBaseTime() = timeLoadBase

    private fun observeDataChange() {
        CoroutineScope(Dispatchers.IO).launch {
            listNotesFlow.collect {
                timeLoadBase = Date().time
                listNotes = it
            }
        }
    }

    fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse = runBlocking {
        val delayValue =
            if (firstLoad) {
                startDelayValue.value
            } else {
                queryDelayValue.value
            }
        if (firstRun) {
            setStartData()
        }
        delay((delayValue*1000L).coerceAtLeast(KeyConstants.MIN_DELAY_FOR_REMOTE))
        NoteResponse(timeLoadBase, listNotes)
    }

    fun modifyNote(note: RemoteNotes, type: Boolean): Long = runBlocking {
        delay((operationDelayValue.value * 1000L).coerceAtLeast(KeyConstants.MIN_DELAY_FOR_REMOTE))
        try {
            if (type) {
                remoteDao.insertNote(note)
            } else {
                remoteDao.deleteNote(note)
                note.id
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    private suspend fun setStartData() {
        val startList: List<RemoteNotes> =
            listOf(
                RemoteNotes(1,"Colors","Colors greatly influence our lives today.",1100000000000),
                RemoteNotes(2,"Find Woman","Scientists Find Woman Who Sees 99 Million More Colors than Others.",1300000000000),
                RemoteNotes(3,"Mental activity","Colors influence our moods and every type of mental activity.",1600000000000)
            )
        try {
            remoteDao.startDatabase(startList)
        } catch (_: Exception) {}
    }

}