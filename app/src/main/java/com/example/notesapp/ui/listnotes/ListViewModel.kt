package com.example.notesserviceimitation.ui.listnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {




}