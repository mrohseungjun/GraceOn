package com.graceon.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class SupabaseAuthManager(
    private val apiBaseUrl: String,
    private val supabaseAnonKey: String,
    private val sessionStore: SupabaseSessionStore
) {
    private val mutex = Mutex()
    private val supabaseUrl = apiBaseUrl.substringBefore("/functions/").trimEnd('/')
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(platformHttpClientEngineFactory()) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun getAccessToken(): String = mutex.withLock {
        require(supabaseUrl.isNotBlank()) {
            "Supabase project URL is missing. Check GRACEON_API_BASE_URL."
        }
        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        val existingSession = sessionStore.load()
        if (existingSession != null) {
            if (existingSession.expiresAtEpochSeconds > currentEpochSeconds() + 60) {
                return@withLock existingSession.accessToken
            }

            val refreshedSession = runCatching { refreshSession(existingSession.refreshToken) }.getOrNull()
            if (refreshedSession != null) {
                sessionStore.save(refreshedSession)
                return@withLock refreshedSession.accessToken
            }

            sessionStore.clear()
        }

        val session = createAnonymousSession()
        sessionStore.save(session)
        session.accessToken
    }

    suspend fun resetSession() {
        mutex.withLock {
            sessionStore.clear()
        }
    }

    private suspend fun createAnonymousSession(): SupabaseSession {
        val response = client.post("$supabaseUrl/auth/v1/signup") {
            applySupabaseHeaders()
            setBody(SupabaseAnonymousSignInRequest(data = mapOf("source" to "graceon")))
        }

        if (!response.status.isSuccess()) {
            throw createAuthException(response.bodyAsText())
        }

        return response.body<SupabaseAuthResponse>().toSession()
    }

    private suspend fun refreshSession(refreshToken: String): SupabaseSession {
        val response = client.post("$supabaseUrl/auth/v1/token?grant_type=refresh_token") {
            applySupabaseHeaders()
            setBody(SupabaseRefreshTokenRequest(refreshToken = refreshToken))
        }

        if (!response.status.isSuccess()) {
            throw createAuthException(response.bodyAsText())
        }

        return response.body<SupabaseAuthResponse>().toSession()
    }

    private fun createAuthException(rawBody: String): Exception {
        val error = runCatching {
            json.decodeFromString<SupabaseAuthErrorResponse>(rawBody)
        }.getOrNull()

        val message = error?.message()
            ?: if ("anonymous_provider_disabled" in rawBody) {
                "Supabase anonymous sign-ins are disabled. Enable Anonymous provider in Supabase Auth settings."
            } else {
                "Supabase anonymous authentication failed."
            }

        return Exception(message)
    }

    private fun SupabaseAuthResponse.toSession(): SupabaseSession {
        val accessToken = accessToken?.trim().orEmpty()
        val refreshToken = refreshToken?.trim().orEmpty()
        val expiresAtEpochSeconds = expiresAt
            ?: expiresIn?.let { currentEpochSeconds() + it }
            ?: currentEpochSeconds() + 3600

        require(accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            "Supabase session response is missing tokens."
        }

        return SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAtEpochSeconds
        )
    }

    private fun currentEpochSeconds(): Long = Clock.System.now().epochSeconds

    private fun io.ktor.client.request.HttpRequestBuilder.applySupabaseHeaders() {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        header("apikey", supabaseAnonKey)
        header(HttpHeaders.Authorization, "Bearer $supabaseAnonKey")
    }
}

interface SupabaseSessionStore {
    fun load(): SupabaseSession?
    fun save(session: SupabaseSession)
    fun clear()
}

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long
)

@Serializable
private data class SupabaseAnonymousSignInRequest(
    val data: Map<String, String> = emptyMap()
)

@Serializable
private data class SupabaseRefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
private data class SupabaseAuthResponse(
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    @SerialName("expires_in")
    val expiresIn: Long? = null
)

@Serializable
private data class SupabaseAuthErrorResponse(
    val error: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
    val msg: String? = null
) {
    fun message(): String? = errorDescription ?: msg ?: error
}
