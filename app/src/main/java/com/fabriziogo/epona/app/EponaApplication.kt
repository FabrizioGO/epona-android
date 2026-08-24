package com.fabriziogo.epona.app

import android.app.Application
import com.fabriziogo.epona.BuildConfig
import com.fabriziogo.epona.core.network.SupabaseProvider
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class EponaApplication : Application() {

    @Inject
    lateinit var supabaseProvider: SupabaseProvider

    override fun onCreate() {
        super.onCreate()

        // Timber logging (debug only)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Supabase client
        supabaseProvider.initialize(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY
        )

        Timber.d("Epona initialized — Supabase connected")
    }
}