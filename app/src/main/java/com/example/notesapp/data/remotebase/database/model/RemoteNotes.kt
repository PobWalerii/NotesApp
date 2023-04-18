package com.example.notesapp.data.remotebase.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteNotes(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteName: String,
    val noteSpecification: String,
    val noteDate: Long
)