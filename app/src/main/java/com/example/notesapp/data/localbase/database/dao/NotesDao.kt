package com.example.notesapp.data.localbase.database.dao

import androidx.room.*
import com.example.notesapp.data.localbase.database.entitys.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM Notes ORDER BY noteDate DESC")
    fun loadDataBase(): Flow<List<Notes>>

    @Query("SELECT * FROM Notes WHERE id = :curId")
    suspend fun getNoteById(curId: Long): List<Notes>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Notes): Long

    @Query("DELETE FROM Notes")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateDatabase(list: List<Notes>) {
        deleteAll()
        list.map {
            insertNote(it)
        }
    }
}