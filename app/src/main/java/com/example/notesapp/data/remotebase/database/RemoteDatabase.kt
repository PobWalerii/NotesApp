package com.example.notesapp.data.remotebase.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.data.localbase.entitys.Notes
import com.example.notesapp.data.remotebase.database.dao.RemoteDao

@Database(entities = [Notes::class], version = 1, exportSchema = false)
abstract class RemoteDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao
}