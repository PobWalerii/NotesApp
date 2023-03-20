package com.example.notesapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.database.entitys.Notes

@Database(entities = [Notes::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}