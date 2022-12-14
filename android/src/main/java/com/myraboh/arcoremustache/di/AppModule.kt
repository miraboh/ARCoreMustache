package com.myraboh.arcoremustache.di

import android.content.Context
import androidx.room.Room
import com.myraboh.arcoremustache.data.Repository
import com.myraboh.arcoremustache.data.RepositoryImpl
import com.myraboh.arcoremustache.data.source.local.VideoDatabase
import com.myraboh.arcoremustache.data.source.local.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Myraboh on 12/13/22.
 */
@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideVideoDatabase(
        @ApplicationContext context: Context
    ): VideoDatabase = Room.databaseBuilder(
        context,
        VideoDatabase::class.java,
        "videodb"
    ).build()

    @Singleton
    @Provides
    fun provideVideoDao(videoDatabase: VideoDatabase) = videoDatabase.getVideos()

    @Singleton
    @Provides
    fun provideRepository(
        videoDao: VideoDao
    ): Repository =  RepositoryImpl(videoDao)
}