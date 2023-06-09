package com.example.notesapp.di

import android.content.Context
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
object ConnectModule {

    @Singleton
    @Provides
    fun provideConnectReceiver(
        appSettings: AppSettings,
        @ApplicationContext applicationContext: Context,
    ): ConnectReceiver {
        return ConnectReceiver(appSettings, applicationContext)
    }

}