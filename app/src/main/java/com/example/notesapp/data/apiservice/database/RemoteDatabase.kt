package com.example.notesapp.data.apiservice.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.data.database.entitys.Notes

@Database(entities = [Notes::class], version = 1, exportSchema = false)
abstract class RemoteDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao
}