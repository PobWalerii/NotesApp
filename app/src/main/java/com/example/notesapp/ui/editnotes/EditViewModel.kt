package com.example.notesapp.ui.editnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditViewModel  @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {



}