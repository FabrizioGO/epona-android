package com.fabriziogo.epona.core.network.di

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.service.AlertService
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.NotificationService
import com.fabriziogo.epona.core.network.service.PetService
import com.fabriziogo.epona.core.network.service.SightingService
import com.fabriziogo.epona.core.network.service.StorageService
import com.fabriziogo.epona.core.network.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseProvider(): SupabaseProvider =
        SupabaseProvider()

    @Provides
    @Singleton
    fun provideAuthService(
        supabase: SupabaseProvider
    ): AuthService = AuthService(supabase)

    @Provides
    @Singleton
    fun provideUserService(
        supabase: SupabaseProvider
    ): UserService = UserService(supabase)

    @Provides
    @Singleton
    fun providePetService(
        supabase: SupabaseProvider
    ): PetService = PetService(supabase)

    @Provides
    @Singleton
    fun provideAlertService(
        supabase: SupabaseProvider
    ): AlertService = AlertService(supabase)

    @Provides
    @Singleton
    fun provideSightingService(
        supabase: SupabaseProvider
    ): SightingService = SightingService(supabase)

    @Provides
    @Singleton
    fun provideNotificationService(
        supabase: SupabaseProvider
    ): NotificationService = NotificationService(supabase)

    @Provides
    @Singleton
    fun provideStorageService(
        supabase: SupabaseProvider
    ): StorageService = StorageService(supabase)
}