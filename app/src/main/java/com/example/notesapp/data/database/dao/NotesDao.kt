package com.example.notesapp.data.database.dao

import androidx.room.*
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM Notes ORDER BY noteDate DESC")
    fun loadDataBase(): Flow<List<Notes>>

    @Query("SELECT * FROM Notes WHERE id = :curId")
    suspend fun getNoteById(curId: Long): List<Notes>




    @Query("SELECT * FROM Notes ORDER BY noteDate DESC")
    suspend fun getAllNotes(): List<Notes>

    @Query("SELECT * FROM Notes")
    suspend fun getNotes(): List<Notes>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Notes): Long
    @Delete
    suspend fun deleteNote(note: Notes)



    @Query("DELETE FROM Notes")
    suspend fun deleteAll()

    suspend fun updateDatabase(list: List<Notes>) {
        deleteAll()
        list.map {
            insertNote(it)
        }
    }
}