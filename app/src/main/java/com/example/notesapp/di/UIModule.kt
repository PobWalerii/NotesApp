package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.data.database.dao.NotesDao
import com.example.notesapp.data.remotebase.apiservice.ApiService
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.ui.MyActionBar.MyActionBar
import com.example.notesapp.ui.main.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UIModule {
    @Singleton
    @Provides
    fun provideMyActionBar(
        noteRepository: NotesRepository,
        appSettings: AppSettings,
        @ApplicationContext applicationContext: Context,
    ): MyActionBar {
        return MyActionBar(noteRepository, appSettings, applicationContext)
    }

}