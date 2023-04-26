package com.example.notesapp.ui.editnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.localbase.database.entitys.Notes
import com.example.notesapp.data.localbase.repository.NotesRepository
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
    var flagActSave: Boolean = false

    val isNoteEdited: StateFlow<Boolean> = notesRepository.isNoteEdited
    val isNoteDeleted: StateFlow<Boolean> = notesRepository.isNoteDeleted
    val isConnectStatus: StateFlow<Boolean> = notesRepository.isConnectStatus
    val idInsertOrEdit: StateFlow<Long> = notesRepository.idInsertOrEdit
    val isLoad: StateFlow<Boolean> = notesRepository.isLoad
    val crashOnEdit: StateFlow<Boolean> = notesRepository.crashOnEdit

    var statusLoadInit: Boolean = false
    private val _isLoadedNote = MutableStateFlow(false)
    val isLoadedNote: StateFlow<Boolean> = _isLoadedNote.asStateFlow()

    init {
        statusLoadInit = isLoad.value
        appSettings.showViewForSnack = null
        flagActSave = false
        notesRepository.resetOperatingParameters()
    }

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
            if ((appSettings.dateChanged.value && content != currentNoteSpecification) || currentId == 0L) {
                Date().time
            } else {
                currentNoteDate
            }
        )
        notesRepository.addNote(note)
        flagActSave = true
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