package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.services.BackRemoteService
import com.example.notesapp.services.BackService
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
        backService: BackService,
        backRemoteService: BackRemoteService
    ): ServicesManager {
        return ServicesManager(appSettings, connectReceiver, applicationContext, backService, backRemoteService)
    }
    @Singleton
    @Provides
    fun provideBackService(): BackService = BackService()

    @Singleton
    @Provides
    fun provideBackRemoteService(): BackRemoteService = BackRemoteService()




}