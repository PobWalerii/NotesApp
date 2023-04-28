package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.eventregister.EventRegister
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventRegisterModule {

    @Singleton
    @Provides
    fun provideEventRegister(
        @ApplicationContext applicationContext: Context,
    ): EventRegister {
        return EventRegister(applicationContext)
    }

}