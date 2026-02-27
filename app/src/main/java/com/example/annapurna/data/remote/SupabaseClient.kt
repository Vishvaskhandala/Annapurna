package com.example.annapurna.data.remote

import android.util.Log
import com.example.annapurna.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    private const val TAG = "SupabaseClientProvider"

    // ✅ Correct values from BuildConfig
    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    init {
        Log.d(TAG, "SUPABASE_URL: $SUPABASE_URL")
        Log.d(TAG, "SUPABASE_KEY: $SUPABASE_KEY")

        // Add validation
        if (SUPABASE_URL.isEmpty() || SUPABASE_KEY.isEmpty()) {
            Log.e(TAG, "❌ SUPABASE_URL or SUPABASE_KEY is empty!")
        }

        if (SUPABASE_URL == "localhost" || SUPABASE_URL.contains("localhost")) {
            Log.e(TAG, "❌ SUPABASE_URL is pointing to localhost! Should be: ${SUPABASE_URL}")
        }
    }

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth) {
                autoLoadFromStorage = true
                autoSaveToStorage = true
            }
            install(Postgrest)
            install(Storage)
        }
    }
}