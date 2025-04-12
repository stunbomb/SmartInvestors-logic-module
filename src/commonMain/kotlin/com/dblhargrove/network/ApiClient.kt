package com.dblhargrove.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ApiResponse(
    val api_key: String,
    val supabase_url: String
)

// Multiplatform-compatible client (actual engine will be injected)
lateinit var httpClient: HttpClient

fun initHttpClient(engine: HttpClientEngine) {
    httpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }
}

suspend fun fetchKey(): ApiResponse? {
    return withContext(Dispatchers.Default) {
        repeat(3) { attempt ->
            try {
                val response: HttpResponse = httpClient.get("https://h33rfx5u94.execute-api.us-east-1.amazonaws.com/getKey") {
                    contentType(ContentType.Application.Json)
                }

                return@withContext response.body<ApiResponse>()

            } catch (e: Exception) {
                println("Attempt $attempt failed: ${e.message}")
            }
            delay(1000)
        }
        null
    }
}
