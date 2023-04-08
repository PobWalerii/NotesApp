package com.example.notesapp.ui.editnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditViewModel  @Inject constructor(
    private val notesRepository: NotesRepository,
    private val appSettings: AppSettings
): ViewModel() {

    private var currentNote: Notes? = null

    private var currentId: Long = 0L
    var currentNoteName: String = ""
    var currentNoteSpecification: String = ""
    private var currentNoteDate: Long = 0L

    val isNoteEditedFlow: StateFlow<Boolean> = notesRepository.isNoteEditedFlow
    //val counterDelay: StateFlow<Boolean> = notesRepository.counterDelayFlow
    val isConnectStatus: StateFlow<Boolean> = notesRepository.isConnectStatus

    private val _isLoadedNote = MutableStateFlow(false)
    val isLoadedNote: StateFlow<Boolean> = _isLoadedNote.asStateFlow()

    fun getNoteById(idNote: Long) {
        viewModelScope.launch {
            val note: Notes? = notesRepository.getNoteById(idNote)
            if (note != null) {
                setNoteForEdit(note)
                _isLoadedNote.emit(true)
            }
        }
    }

    private fun setNoteForEdit(note: Notes) {
        currentNote = note
        currentId = note.id
        currentNoteName = note.noteName
        currentNoteSpecification = note.noteSpecification
        currentNoteDate = note.noteDate
    }

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