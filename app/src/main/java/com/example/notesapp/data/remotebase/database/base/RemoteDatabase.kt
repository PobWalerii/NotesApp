package com.example.notesapp.data.remotebase.database.base

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.data.remotebase.database.dao.RemoteDao
import com.example.notesapp.data.remotebase.database.model.RemoteNotes

@Database(entities = [RemoteNotes::class], version = 1, exportSchema = false)
abstract class RemoteDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao
}