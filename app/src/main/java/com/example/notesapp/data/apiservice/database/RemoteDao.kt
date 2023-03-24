package com.example.notesapp.data.apiservice.database

import androidx.room.*
import com.example.notesapp.data.database.entitys.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {

    @Query("SELECT * FROM Notes")
    fun loadDataBase(): Flow<List<Notes>>

//    @Query("SELECT * FROM Notes WHERE id = :curId")
//    suspend fun getNoteById(curId: Long): List<Notes>



/////////////////////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Notes): Long

    @Delete
    suspend fun deleteNote(note: Notes)
////////////////////////////////////////







//    @Query("SELECT * FROM Notes ORDER BY noteDate DESC")
//    suspend fun getAllNotes(): List<Notes>

//    @Query("SELECT * FROM Notes")
//    suspend fun getNotes(): List<Notes>






//    @Query("DELETE FROM Notes")
//    suspend fun deleteAll()

//    suspend fun updateDatabase(list: List<Notes>) {
//        deleteAll()
//        list.map {
//            insertNote(it)
//        }
//    }
}