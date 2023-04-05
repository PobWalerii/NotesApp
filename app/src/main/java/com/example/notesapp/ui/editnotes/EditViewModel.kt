package com.example.notesapp.ui.editnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditViewModel  @Inject constructor(
    private val notesRepository: NotesRepository,
    private val appSettings: AppSettings
): ViewModel() {

    var currentNote: Notes? = null

    var currentId: Long = 0L
    var currentNoteName: String = ""
    var currentNoteSpecification: String = ""
    var currentNoteDate: Long = 0L

    val isNoteEditedFlow: StateFlow<Boolean> = notesRepository.isNoteEditedFlow
    val counterDelayFlow: StateFlow<Int> = notesRepository.counterDelayFlow
    val isConnectStatus: StateFlow<Boolean> = notesRepository.isConnectStatus

    fun getNoteById(idNote: Long): Flow<Notes?> =
        notesRepository.getNoteById(idNote)

    fun saveNote(title: String, content: String) {
        val note = Notes(
            currentId,
            title,
            content,
            if ((appSettings.dateChanged.value && content!=currentNoteSpecification) || currentId==0L) {
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

    fun isChangedNote(title: String, text: String): Boolean {
        return currentNoteName != title ||
               currentNoteSpecification != text
    }

}