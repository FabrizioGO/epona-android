package com.fabriziogo.epona.app

import android.app.Application
import com.fabriziogo.epona.BuildConfig
import com.fabriziogo.epona.core.media.ImageProcessor
import com.fabriziogo.epona.core.network.SupabaseProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class EponaApplication : Application() {

    @Inject
    lateinit var supabaseProvider: SupabaseProvider

    @Inject
    lateinit var imageProcessor: ImageProcessor

    override fun onCreate() {
        super.onCreate()

        // A form abandoned mid-edit — or a process killed while the camera app was in
        // front — leaves its staged JPEGs behind with nothing else to collect them.
        CoroutineScope(Dispatchers.IO).launch { imageProcessor.pruneWorkspace() }

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