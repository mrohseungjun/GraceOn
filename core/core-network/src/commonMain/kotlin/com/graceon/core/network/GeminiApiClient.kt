package com.graceon.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Gemini API Client using Ktor.
 */
class GeminiApiClient(
    private val apiKey: String
) {
    private val client = HttpClient(platformHttpClientEngineFactory()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }
    }

    suspend fun generateContent(prompt: String): String {
        val response = client.post {
            url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(
                GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = prompt))
                        )
                    )
                )
            )
        }

        val geminiResponse: GeminiResponse = response.body()
        return geminiResponse.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini API")
    }

    fun close() {
        client.close()
    }
}

@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)
