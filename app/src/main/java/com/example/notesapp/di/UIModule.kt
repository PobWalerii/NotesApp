package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.data.localbase.repository.NotesRepository
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.ui.actionbar.AppActionBar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        @ApplicationContext applicationContext: Context
    ): AppActionBar {
        return AppActionBar(noteRepository, appSettings, applicationContext)
    }
}