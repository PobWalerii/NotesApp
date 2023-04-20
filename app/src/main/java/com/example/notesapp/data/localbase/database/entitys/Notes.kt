package com.example.notesapp.data.localbase.database.entitys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Notes(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteName: String,
    val noteSpecification: String,
    val noteDate: Long
)