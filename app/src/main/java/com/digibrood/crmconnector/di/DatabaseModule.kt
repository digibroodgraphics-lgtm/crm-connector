package com.digibrood.crmconnector.di

import android.content.Context
import androidx.room.Room
import com.digibrood.crmconnector.data.local.AppDatabase
import com.digibrood.crmconnector.data.local.dao.CallDao
import com.digibrood.crmconnector.data.local.dao.RecordingDao
import com.digibrood.crmconnector.data.local.dao.RemarkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the Room database and its DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCallDao(db: AppDatabase): CallDao = db.callDao()

    @Provides
    fun provideRecordingDao(db: AppDatabase): RecordingDao = db.recordingDao()

    @Provides
    fun provideRemarkDao(db: AppDatabase): RemarkDao = db.remarkDao()
}
