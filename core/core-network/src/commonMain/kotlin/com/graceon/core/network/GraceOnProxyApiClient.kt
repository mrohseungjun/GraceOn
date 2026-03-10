package com.graceon.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
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
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GraceOnProxyApiClient(
    private val baseUrl: String,
    private val supabaseAnonKey: String,
    sessionStore: SupabaseSessionStore
) {
    private val authManager = SupabaseAuthManager(
        apiBaseUrl = baseUrl,
        supabaseAnonKey = supabaseAnonKey,
        sessionStore = sessionStore
    )

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

        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        suspend fun performRequest(accessToken: String) = client.post(baseUrl) {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header("apikey", supabaseAnonKey)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(ProxyGenerateRequest(prompt = prompt))
        }

        var response = performRequest(authManager.getAccessToken())

        if (response.status.value == 401) {
            authManager.resetSession()
            response = performRequest(authManager.getAccessToken())
        }

        if (!response.status.isSuccess()) {
            val errorPayload = runCatching { response.body<ProxyErrorResponse>() }.getOrNull()
            throw GraceOnProxyException(
                message = errorPayload?.error ?: "GraceOn proxy request failed",
                statusCode = response.status.value,
                rewardedEligible = errorPayload?.rewardedEligible ?: false
            )
        }

        val payload: ProxyGenerateResponse = response.body()
        return payload.text.ifBlank {
            throw Exception("Empty response from GraceOn proxy")
        }
    }

    suspend fun getDailyFreeUsage(): DailyFreeUsageStatus {
        require(baseUrl.isNotBlank()) {
            "GraceOn proxy URL is missing. Configure GRACEON_API_BASE_URL for this platform."
        }

        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        suspend fun performRequest(accessToken: String) = client.get(baseUrl) {
            header("apikey", supabaseAnonKey)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        var response = performRequest(authManager.getAccessToken())

        if (response.status.value == 401) {
            authManager.resetSession()
            response = performRequest(authManager.getAccessToken())
        }

        if (!response.status.isSuccess()) {
            val errorPayload = runCatching { response.body<ProxyErrorResponse>() }.getOrNull()
            throw GraceOnProxyException(
                message = errorPayload?.error ?: "GraceOn proxy usage request failed",
                statusCode = response.status.value
            )
        }

        return response.body()
    }

    suspend fun grantRewardedCredit() {
        require(baseUrl.isNotBlank()) {
            "GraceOn proxy URL is missing. Configure GRACEON_API_BASE_URL for this platform."
        }

        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        suspend fun performRequest(accessToken: String) = client.post(baseUrl) {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header("apikey", supabaseAnonKey)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(ProxyRewardGrantRequest(action = "grant_reward"))
        }

        var response = performRequest(authManager.getAccessToken())

        if (response.status.value == 401) {
            authManager.resetSession()
            response = performRequest(authManager.getAccessToken())
        }

        if (!response.status.isSuccess()) {
            val errorPayload = runCatching { response.body<ProxyErrorResponse>() }.getOrNull()
            throw GraceOnProxyException(
                message = errorPayload?.error ?: "GraceOn proxy reward request failed",
                statusCode = response.status.value,
                rewardedEligible = errorPayload?.rewardedEligible ?: false
            )
        }
    }

    fun close() {
        client.close()
    }

    suspend fun logout() {
        authManager.resetSession()
    }

    suspend fun signInWithGoogle(openUrl: (String) -> Unit) {
        authManager.signInWithGoogle(openUrl)
    }

    suspend fun signInWithEmail(email: String, password: String) {
        authManager.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String): Boolean {
        return authManager.signUpWithEmail(email, password) == EmailSignUpResult.SignedIn
    }

    suspend fun resendConfirmationEmail(email: String) {
        authManager.resendConfirmationEmail(email)
    }

    suspend fun sendPasswordResetEmail(email: String) {
        authManager.sendPasswordResetEmail(email)
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

@Serializable
data class DailyFreeUsageStatus(
    val dailyLimit: Int,
    val usedToday: Int,
    val remainingToday: Int,
    val rewardedCredits: Int = 0,
    val rewardedAvailableToday: Int = 0
)

@Serializable
private data class ProxyErrorResponse(
    val error: String? = null,
    val rewardedEligible: Boolean? = null
)

@Serializable
private data class ProxyRewardGrantRequest(
    val action: String
)

class GraceOnProxyException(
    message: String,
    val statusCode: Int,
    val rewardedEligible: Boolean = false
) : Exception(message)
