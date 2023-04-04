package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.receivers.ConnectReceiver
import com.example.notesapp.utils.ShowConnectStatus
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
        @ApplicationContext applicationContext: Context
    ): ConnectReceiver {
        return ConnectReceiver(applicationContext)
    }

    @Singleton
    @Provides
    fun provideShowConnectStatus(
        @ApplicationContext applicationContext: Context
    ): ShowConnectStatus {
        return ShowConnectStatus(applicationContext)
    }


}