package com.dblhargrove.network

import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.websocket.WebSockets

object SupabaseClient {
    private var supabaseUrl: String = ""
    private var supabaseKey: String = ""

    private var _client: io.github.jan.supabase.SupabaseClient? = null

    val client: io.github.jan.supabase.SupabaseClient
        get() = _client ?: throw IllegalStateException("Supabase client is not initialized")

    @OptIn(SupabaseInternal::class)
    fun initialize(url: String, key: String) {
        supabaseUrl = url
        supabaseKey = key
        _client = createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
            httpConfig {
                this.install(WebSockets)
            }
        }
    }
}


// Test the client immediately
suspend fun testClient() {
    try {
        val response = SupabaseClient.client.postgrest["users"].select()
        log(LogLevel.DEBUG,"TestResponse", "$response")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"TestResponse", "${e.message}")
    }
}



