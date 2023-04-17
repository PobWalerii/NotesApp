package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.receivers.DateManager
import com.example.notesapp.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DateChangedModule {

    @Singleton
    @Provides
    fun provideDateManager(
        appSettings: AppSettings,
        @ApplicationContext applicationContext: Context,
    ): DateManager {
        return DateManager(appSettings, applicationContext)
    }
}