package com.example.notesapp.ui.editnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.apiservice.ApiService
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

    var dateChangetStrategy: Boolean = true

    val isNoteEditedFlow: StateFlow<Boolean> = notesRepository.isNoteEditedFlow

    fun getNoteById(idNote: Long): Flow<List<Notes>> =
        notesRepository.getNoteById(idNote)

    fun saveNote(title: String, content: String) {
        val note = Notes(
            currentId,
            title,
            content,
            if ((dateChangetStrategy && content!=currentNoteSpecification) || currentId==0L) {
                Date().time
            } else {
                currentNoteDate
            }
        )
        notesRepository.addNote(note)
    }

    fun setStartFlowParameters() {
        notesRepository.setStartFlowParameters()
    }

}