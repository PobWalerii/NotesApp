package com.example.notesapp.di

import com.example.notesapp.data.apiservice.ApiService
import com.example.notesapp.data.apiservice.ApiServiceImpl
import com.example.notesapp.data.database.dao.NotesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideApiService(notesDao: NotesDao): ApiService {
        return ApiServiceImpl(notesDao)
    }

}