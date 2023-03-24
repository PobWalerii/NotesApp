package com.example.notesapp.data.apiservice.model

import com.example.notesapp.data.database.entitys.Notes

data class NoteResponse(
    val timeBase: Long,
    val fullList: List<Notes>
)
