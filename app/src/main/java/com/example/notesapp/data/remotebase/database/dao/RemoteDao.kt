package com.example.notesapp.data.remotebase.database.dao

import androidx.room.*
import com.example.notesapp.data.remotebase.database.model.RemoteNotes
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {

    @Query("SELECT * FROM RemoteNotes")
    fun loadDataBase(): Flow<List<RemoteNotes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: RemoteNotes): Long

    @Delete
    suspend fun deleteNote(note: RemoteNotes)

    @Query("DELETE FROM RemoteNotes")
    suspend fun deleteAll()

    @Transaction
    suspend fun startDatabase(list: List<RemoteNotes>) {
        deleteAll()
        list.map {
            insertNote(it)
        }
    }



}