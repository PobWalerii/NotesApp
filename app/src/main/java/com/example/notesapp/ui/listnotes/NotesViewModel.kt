package com.example.notesapp.ui.listnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.localbase.database.entitys.Notes
import com.example.notesapp.data.localbase.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
): ViewModel() {

    val isLoad: StateFlow<Boolean> = notesRepository.isLoad
    val isConnectStatus: StateFlow<Boolean> = notesRepository.isConnectStatus
    val firstRun: StateFlow<Boolean> = notesRepository.firstRun
    val listNotes: Flow<List<Notes>> = notesRepository.listNotes
    val idInsertOrEdit: StateFlow<Long> = notesRepository.idInsertOrEdit

    fun deleteNote(note: Notes) {
        notesRepository.deleteNote(note)
    }

    fun addNote() {
        notesRepository.addNote(
            Notes(0, "", "", Date().time)
        )
    }

    fun setCurrentIdTo(id: Long) {
        notesRepository.setInsertOrEditId(id)
    }

}