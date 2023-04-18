package com.example.notesapp.data.remotebase.database.model

data class NoteResponse(
    val timeBase: Long,
    val fullList: List<RemoteNotes>
)
