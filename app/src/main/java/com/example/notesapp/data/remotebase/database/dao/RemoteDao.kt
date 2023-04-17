package com.example.notesapp.data.remotebase.database.dao

import androidx.room.*
import com.example.notesapp.data.localbase.entitys.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {

    @Query("SELECT * FROM Notes")
    fun loadDataBaseFlow(): Flow<List<Notes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Notes): Long

    @Delete
    suspend fun deleteNote(note: Notes)

    @Query("DELETE FROM Notes")
    suspend fun deleteAll()

    @Transaction
    suspend fun startDatabase(list: List<Notes>) {
        deleteAll()
        list.map {
            insertNote(it)
        }
    }



}