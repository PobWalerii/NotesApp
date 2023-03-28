package com.example.notesapp.data.apiservice

import android.content.Context
import android.net.ConnectivityManager
import com.example.notesapp.R
import com.example.notesapp.data.remotedatabase.database.RemoteDao
import com.example.notesapp.data.remotedatabase.model.NoteResponse
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val remoteDao: RemoteDao,
    private val applicationContext: Context
): ApiService {

    private var timeLoadBase: Long = 0L
    private var listNotes: List<Notes> = emptyList()

    private val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        CoroutineScope(Dispatchers.Default).launch {
            remoteDao.loadDataBase().apply {
                if(this.isEmpty()) {
                    setStartData()
                }
            }
        }
        observeDataChange()
    }

    override fun getChangeBaseTime() = timeLoadBase

    private fun observeDataChange() {
        CoroutineScope(Dispatchers.Default).launch {
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
        CoroutineScope(Dispatchers.Default).launch {
            startList.map {
                remoteDao.insertNote(it)
            }
        }
    }

    override suspend fun getAllNote(delayTime: Long): NoteResponse = withContext(Dispatchers.IO) {
        delay(delayTime)
        NoteResponse(timeLoadBase, listNotes)
    }

    override suspend fun deleteNote(note: Notes, delayTime: Long) {
        delay(delayTime)
        if(isInetConnect()) {
            remoteDao.deleteNote(note)
        }  else {
            throw Exception(applicationContext.getString(R.string.operation_failed))
        }
    }
    override suspend fun addNote(note: Notes, delayTime: Long): Long {
        delay(delayTime)
        if(isInetConnect()) {
            return remoteDao.insertNote(note)
        } else {
            throw Exception(applicationContext.getString(R.string.operation_failed))
        }
    }

    private fun isInetConnect(): Boolean {
        return connectivityManager.activeNetwork != null
    }
}