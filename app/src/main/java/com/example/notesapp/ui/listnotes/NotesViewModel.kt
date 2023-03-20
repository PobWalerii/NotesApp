package com.example.notesapp.ui.listnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val apiService: ApiService
): ViewModel() {
    fun loadDatabase(): Flow<List<Notes>> = notesRepository.loadDataBase()
    fun deleteNote(note: Notes) {
        viewModelScope.launch {
            apiService.deleteNote(note)
        }
    }


    fun addNote() {
        viewModelScope.launch {
            apiService.addNote(
                Notes(0, "", "", Date().time)
            )
        }
    }





}