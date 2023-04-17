package com.example.notesapp.data.localbase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.data.localbase.dao.NotesDao
import com.example.notesapp.data.localbase.entitys.Notes

@Database(entities = [Notes::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}