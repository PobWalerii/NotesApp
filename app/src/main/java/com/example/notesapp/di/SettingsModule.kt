package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Singleton
    @Provides
    fun provideAppSettings(
        @ApplicationContext applicationContext: Context
    ): AppSettings {
        return com.example.notesapp.settings.AppSettings(applicationContext)
    }
}