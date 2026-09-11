package com.felixj.moneta.shared.room.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.felixj.moneta.shared.room.db.MonetaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Provides
    @Singleton
    fun provideMonetaDatabase(
        @ApplicationContext applicationContext: Context
    ) = Room.databaseBuilder<MonetaDatabase>(applicationContext, "moneta_db")
        .setDriver(AndroidSQLiteDriver())
        .build()

    @Provides
    @Singleton
    fun provideActivityDao(database: MonetaDatabase) = database.activityDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: MonetaDatabase) = database.categoryDao()
}