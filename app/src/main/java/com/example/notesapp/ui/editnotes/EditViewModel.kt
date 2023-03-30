package com.example.notesapp.ui.editnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditViewModel  @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {

    var currentNote: Notes? = null

    var currentId: Long = 0L
    var currentNoteName: String = ""
    var currentNoteSpecification: String = ""
    var currentNoteDate: Long = 0L
    var lastConnectionStatus: Boolean = true
    var dateChangedStrategy: Boolean = true

    val isNoteEditedFlow: StateFlow<Boolean> = notesRepository.isNoteEditedFlow
    val serviceErrorFlow: StateFlow<String> = notesRepository.serviceErrorFlow
    val counterDelayFlow: StateFlow<Int> = notesRepository.counterDelayFlow

    fun getNoteById(idNote: Long): Flow<Notes?> =
        notesRepository.getNoteById(idNote)

    fun saveNote(title: String, content: String) {
        val note = Notes(
            currentId,
            title,
            content,
            if ((dateChangedStrategy && content!=currentNoteSpecification) || currentId==0L) {
                Date().time
            } else {
                currentNoteDate
            }
        )
        notesRepository.addNote(note)
    }

    fun deleteNote() {
        currentNote?.let {note ->
            notesRepository.deleteNote(note)
        }
    }

    fun setStartFlowParameters() {
        notesRepository.setStartFlowParameters()
    }

    fun clearServiceErrorMessage() {
        notesRepository.clearServiceErrorMessage()
    }

}