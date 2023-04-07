package com.example.notesapp.data.remotebase.model

import com.example.notesapp.data.database.entitys.Notes

data class NoteResponse(
    val timeBase: Long,
    val fullList: List<Notes>
)
