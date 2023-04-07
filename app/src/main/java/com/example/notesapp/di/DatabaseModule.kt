package com.example.notesapp.di

import android.content.Context
import androidx.room.Room
import com.example.notesapp.constants.KeyConstants
import com.example.notesapp.data.remotebase.apiservice.ApiService
import com.example.notesapp.data.database.AppDatabase
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            KeyConstants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideNotesDao(database: AppDatabase): NotesDao {
        return database.notesDao()
    }
    @Singleton
    @Provides
    fun provideNotesRepository(
        notesDao: NotesDao,
        apiService: ApiService,
        connectReceiver: ConnectReceiver,
        appSettings: AppSettings,
        @ApplicationContext applicationContext: Context
        ): NotesRepository {
            return NotesRepository(notesDao, apiService, connectReceiver, appSettings, applicationContext)
        }

}
