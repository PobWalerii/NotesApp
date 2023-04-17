package com.example.notesapp.data.remotebase.database.model

import com.example.notesapp.data.localbase.entitys.Notes

data class NoteResponse(
    val timeBase: Long,
    val fullList: List<Notes>
)
