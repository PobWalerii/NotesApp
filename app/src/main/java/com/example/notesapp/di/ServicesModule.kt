package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.services.ServicesManager
import com.example.notesapp.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServicesModule {

    @Singleton
    @Provides
    fun provideRemoteServiceManager(
        appSettings: AppSettings,
        connectReceiver: ConnectReceiver,
        @ApplicationContext applicationContext: Context,
    ): ServicesManager {
        return ServicesManager(appSettings, connectReceiver, applicationContext) //, backService, backRemoteService)
    }

}