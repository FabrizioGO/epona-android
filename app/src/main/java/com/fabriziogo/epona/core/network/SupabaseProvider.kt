package com.fabriziogo.epona.core.network

import com.fabriziogo.epona.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseProvider @Inject constructor() {

    lateinit var client: SupabaseClient
        private set

    fun initialize(url: String, anonKey: String) {
        client = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            // Release builds log nothing: supabase-kt's default level can print request
            // details, and logcat is readable by anyone with the device on a cable.
            if (!BuildConfig.DEBUG) defaultLogLevel = LogLevel.NONE
            install(Auth) {
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
            install(Functions)
        }
    }

    val auth get() = client.auth
    val postgrest get() = client.postgrest
    val storage get() = client.storage
    val realtime get() = client.realtime
    val functions get() = client.functions
}