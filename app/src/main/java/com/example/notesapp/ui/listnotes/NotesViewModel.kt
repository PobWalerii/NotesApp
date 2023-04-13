package com.example.notesapp.ui.listnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {

    val isLoadFlow: StateFlow<Boolean> = notesRepository.isLoadFlow
    val isConnectStatus: StateFlow<Boolean> = notesRepository.isConnectStatus
    val firstRun: StateFlow<Boolean> = notesRepository.firstRun

    fun loadDatabase(): Flow<List<Notes>> = notesRepository.loadDataBase()

    fun deleteNote(note: Notes) {
        notesRepository.deleteNote(note)
    }

    fun addNote() {
        notesRepository.addNote(
            Notes(0, "", "", Date().time),
            false
        )
    }

    fun getCurrentId() = notesRepository.getInsertOrEditId()
    fun setCurrentId(current: Long) {
        notesRepository.setInsertOrEditId(current)
    }

}