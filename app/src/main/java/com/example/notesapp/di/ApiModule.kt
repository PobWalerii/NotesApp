package com.example.notesapp.di

import android.content.Context
import androidx.room.Room
import com.example.notesapp.constants.KeyConstants
import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.apiservice.ApiServiceImpl
import com.example.notesapp.data.apiservice.database.RemoteDao
import com.example.notesapp.data.apiservice.database.RemoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): RemoteDatabase {
        return Room.databaseBuilder(
            applicationContext,
            RemoteDatabase::class.java,
            KeyConstants.REMOTE_DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRemoteDao(database: RemoteDatabase): RemoteDao {
        return database.remoteDao()
    }

    @Singleton
    @Provides
    fun provideApiService(remoteDao: RemoteDao): ApiService {
        return ApiServiceImpl(remoteDao)
    }

}