package com.graceon.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GraceOnProxyApiClient(
    private val baseUrl: String
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
        require(baseUrl.isNotBlank()) {
            "GraceOn proxy URL is missing. Configure GRACEON_API_BASE_URL for this platform."
        }

        val response = client.post(baseUrl) {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(ProxyGenerateRequest(prompt = prompt))
        }

        val payload: ProxyGenerateResponse = response.body()
        return payload.text.ifBlank {
            throw Exception("Empty response from GraceOn proxy")
        }
    }

    fun close() {
        client.close()
    }
}

@Serializable
private data class ProxyGenerateRequest(
    val prompt: String
)

@Serializable
private data class ProxyGenerateResponse(
    val text: String
)
