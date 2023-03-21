package com.example.notesapp.ui.editnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditViewModel  @Inject constructor(
    private val notesRepository: NotesRepository,
    private val apiService: ApiService
): ViewModel() {

    var currentNoteName: String = ""
    var currentNoteSpecification: String = ""
    var currentNoteDate: Long = 0L

    suspend fun getNoteById(idNote: Long): Notes? =
        notesRepository.getNoteById(idNote)


}