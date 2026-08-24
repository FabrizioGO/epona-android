package com.fabriziogo.epona.core.data.di

import com.fabriziogo.epona.core.data.repository.AlertRepositoryImpl
import com.fabriziogo.epona.core.data.repository.AuthRepositoryImpl
import com.fabriziogo.epona.core.data.repository.LocationRepositoryImpl
import com.fabriziogo.epona.core.data.repository.NotificationRepositoryImpl
import com.fabriziogo.epona.core.data.repository.PetRepositoryImpl
import com.fabriziogo.epona.core.data.repository.SightingRepositoryImpl
import com.fabriziogo.epona.core.data.repository.UserRepositoryImpl
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import com.fabriziogo.epona.core.domain.repository.NotificationRepository
import com.fabriziogo.epona.core.domain.repository.PetRepository
import com.fabriziogo.epona.core.domain.repository.SightingRepository
import com.fabriziogo.epona.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(
        impl: PetRepositoryImpl
    ): PetRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        impl: AlertRepositoryImpl
    ): AlertRepository

    @Binds
    @Singleton
    abstract fun bindSightingRepository(
        impl: SightingRepositoryImpl
    ): SightingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository
}