package com.example.notesapp.data.remotedatabase.database

import androidx.room.*
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {

    @Query("SELECT * FROM Notes")
    fun loadDataBaseFlow(): Flow<List<Notes>>

    @Query("SELECT * FROM Notes")
    suspend fun loadDataBase(): List<Notes>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Notes): Long

    @Delete
    suspend fun deleteNote(note: Notes)
}