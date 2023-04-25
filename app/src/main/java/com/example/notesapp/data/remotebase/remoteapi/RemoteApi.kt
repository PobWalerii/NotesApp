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

    private fun observeDataChange() {
        CoroutineScope(Dispatchers.IO).launch {
            listNotesFlow.collect {
                timeLoadBase = Date().time
                listNotes = it
            }
        }
    }

    suspend fun processingRequest(
        key: String,
        firstLoad: Boolean,
        firstRun: Boolean,
        note: Any,
        type: Boolean
    ): Any {
        try {
            return when (key) {
                "load" -> getAllNote(firstLoad, firstRun)
                "edit" -> {
                    modifyNote(note as RemoteNotes, type)
                    //getResultEdit()
                }
                "date" -> timeLoadBase
                else -> throw Exception("$key?")
            }
        } catch (e: Exception) {
            throw Exception("Server Error")
        }
    }

    private suspend fun getAllNote(firstLoad: Boolean, firstRun: Boolean): NoteResponse =
        coroutineScope {
            val delayValue =
                if (firstLoad) {
                    startDelayValue.value
                } else {
                    queryDelayValue.value
                }
            if (firstRun) {
                setStartData()
            }
            delay((delayValue * 1000L).coerceAtLeast(KeyConstants.MIN_DELAY_FOR_REMOTE))
            NoteResponse(timeLoadBase, listNotes)
        }






    private suspend fun modifyNote(note: RemoteNotes, type: Boolean): Long {
        val result: Long
        try {
            delay((operationDelayValue.value * 1000L).coerceAtLeast(KeyConstants.MIN_DELAY_FOR_REMOTE))
        } catch (_: Exception) {
        } finally {
            result = makeEdit(note, type)
        }
        return result
    }

    private fun makeEdit(note: RemoteNotes, type: Boolean): Long {
        //= runBlocking {
        var result: Long = 0
        CoroutineScope(Dispatchers.IO).launch {
            val id =if (type) {
                insertNote(note)
            } else {
                deleteNote(note)
            }
            result = id
        }
        return result
    }


    suspend fun insertNote(note: RemoteNotes): Long {
        return remoteDao.insertNote(note)
    }

    private suspend fun deleteNote(note: RemoteNotes): Long {
        remoteDao.deleteNote(note)
        return note.id
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