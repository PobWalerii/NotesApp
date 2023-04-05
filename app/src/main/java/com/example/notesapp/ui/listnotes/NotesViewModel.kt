package com.example.notesapp.ui.listnotes

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {

    var firstDataLoad = false
    var isStartApp = true

    val isLoadedFlow: StateFlow<Boolean> = notesRepository.isLoadedFlow
    val isRemoteDatabaseChangedFlow: StateFlow<Boolean> = notesRepository.isRemoteDatabaseChangedFlow
    val counterDelayFlow: StateFlow<Int> = notesRepository.counterDelayFlow
    val isConnectStatus: StateFlow<Boolean> = notesRepository.isConnectStatus

    fun getInitialDataUpload(): Boolean = notesRepository.getInitialDataUpload()

    fun loadDatabase(): Flow<List<Notes>> = notesRepository.loadDataBase()

    fun loadRemoteData() {
        notesRepository.loadRemoteData(isStartApp)
    }

    fun stopLoadRemoteData() {
        notesRepository.stopLoadRemoteData()
    }
    fun restartLoadRemoteData() {
        notesRepository.restartLoadRemoteData(isStartApp)
    }

    fun setIsLoadCanceled() {
        notesRepository.setIsLoadCanceled()
    }

    fun deleteNote(note: Notes) {
        notesRepository.deleteNote(note)
    }

    fun addNote() {
        notesRepository.addNote(
            Notes(0, "", "", Date().time)
        )
    }

    fun setInsertedOrEditedIdNull() {
        notesRepository.setInsertedOrEditedIdNull()
    }

    fun getInsertedOrEditedIdValue() = notesRepository.getInsertedOrEditedIdValue()

}