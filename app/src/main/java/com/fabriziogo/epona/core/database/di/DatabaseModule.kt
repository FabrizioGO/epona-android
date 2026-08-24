package com.fabriziogo.epona.core.database.di

import android.content.Context
import androidx.room.Room
import com.fabriziogo.epona.core.database.EponaDatabase
import com.fabriziogo.epona.core.database.dao.AlertDao
import com.fabriziogo.epona.core.database.dao.NotificationDao
import com.fabriziogo.epona.core.database.dao.PetDao
import com.fabriziogo.epona.core.database.dao.SightingDao
import com.fabriziogo.epona.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): EponaDatabase =
        Room.databaseBuilder(
            context,
            EponaDatabase::class.java,
            EponaDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideUserDao(db: EponaDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun providePetDao(db: EponaDatabase): PetDao = db.petDao()

    @Provides
    @Singleton
    fun provideAlertDao(db: EponaDatabase): AlertDao = db.alertDao()

    @Provides
    @Singleton
    fun provideSightingDao(db: EponaDatabase): SightingDao = db.sightingDao()

    @Provides
    @Singleton
    fun provideNotificationDao(db: EponaDatabase): NotificationDao = db.notificationDao()
}