package com.graceon.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
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

        throw Exception("로그인이 필요합니다. 메일 또는 Google 계정으로 로그인해주세요.")
    }

    suspend fun signInWithEmail(email: String, password: String) {
        require(supabaseUrl.isNotBlank()) {
            "Supabase project URL is missing. Check GRACEON_API_BASE_URL."
        }
        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        val response = client.post("$supabaseUrl/auth/v1/token?grant_type=password") {
            applySupabaseHeaders()
            setBody(SupabasePasswordLoginRequest(email = email.trim(), password = password))
        }

        if (!response.status.isSuccess()) {
            throw createAuthException(response.bodyAsText())
        }

        mutex.withLock {
            sessionStore.save(response.body<SupabaseAuthResponse>().toSession())
        }
    }

    suspend fun resendConfirmationEmail(email: String) {
        require(supabaseUrl.isNotBlank()) {
            "Supabase project URL is missing. Check GRACEON_API_BASE_URL."
        }
        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        val response = client.post("$supabaseUrl/auth/v1/resend") {
            applySupabaseHeaders()
            setBody(SupabaseResendOtpRequest(type = "signup", email = email.trim()))
        }

        if (!response.status.isSuccess()) {
            throw createAuthException(response.bodyAsText())
        }
    }

    suspend fun sendPasswordResetEmail(email: String) {
        require(supabaseUrl.isNotBlank()) {
            "Supabase project URL is missing. Check GRACEON_API_BASE_URL."
        }
        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        val response = client.post("$supabaseUrl/auth/v1/recover") {
            applySupabaseHeaders()
            setBody(
                SupabaseRecoverPasswordRequest(
                    email = email.trim(),
                    redirectTo = SUPABASE_AUTH_REDIRECT_URL
                )
            )
        }

        if (!response.status.isSuccess()) {
            throw createAuthException(response.bodyAsText())
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): EmailSignUpResult {
        require(supabaseUrl.isNotBlank()) {
            "Supabase project URL is missing. Check GRACEON_API_BASE_URL."
        }
        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        val response = client.post("$supabaseUrl/auth/v1/signup") {
            applySupabaseHeaders()
            setBody(SupabaseEmailSignUpRequest(email = email.trim(), password = password))
        }

        if (!response.status.isSuccess()) {
            throw createAuthException(response.bodyAsText())
        }

        return EmailSignUpResult.EmailConfirmationRequired
    }

    suspend fun resetSession() {
        mutex.withLock {
            sessionStore.clear()
        }
    }

    suspend fun signInWithGoogle(openUrl: (String) -> Unit) {
        require(supabaseUrl.isNotBlank()) {
            "Supabase project URL is missing. Check GRACEON_API_BASE_URL."
        }
        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Configure SUPABASE_ANON_KEY for this platform."
        }

        val authorizeUrl = buildString {
            append("$supabaseUrl/auth/v1/authorize")
            append("?provider=google")
            append("&redirect_to=")
            append(SUPABASE_AUTH_REDIRECT_URL.encodeURLQueryComponent())
        }

        val callbackUrl = coroutineScope {
            SupabaseAuthCallbackBridge.clear()
            val callbackWaiter = async {
                SupabaseAuthCallbackBridge.callbackUrl.first { url ->
                    url?.startsWith(SUPABASE_AUTH_REDIRECT_URL.substringBefore('?')) == true
                }.orEmpty()
            }
            openUrl(authorizeUrl)
            withTimeoutOrNull(180_000L) {
                callbackWaiter.await()
            }
        } ?: throw Exception("Google 로그인 시간이 초과되었습니다. 다시 시도해주세요.")

        val session = parseSessionFromCallbackUrl(callbackUrl)
        mutex.withLock {
            sessionStore.save(session)
        }
        SupabaseAuthCallbackBridge.clear()
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

        val normalizedBody = rawBody.lowercase()
        val message = error?.errorCode?.let { code ->
            when (code) {
                "email_exists", "user_already_exists" -> "이미 가입된 이메일입니다. 로그인으로 진행해주세요."
                "email_not_confirmed" -> "이메일 인증이 아직 완료되지 않았습니다. 메일함을 확인해주세요."
                "invalid_credentials" -> "이메일 또는 비밀번호가 올바르지 않습니다."
                else -> null
            }
        }
            ?: error?.message()?.let { messageText ->
                when {
                    "Invalid login credentials".equals(messageText, ignoreCase = true) ->
                        "이메일 또는 비밀번호가 올바르지 않습니다."
                    "Email not confirmed".equals(messageText, ignoreCase = true) ->
                        "이메일 인증이 아직 완료되지 않았습니다. 메일함을 확인해주세요."
                    "User already registered".equals(messageText, ignoreCase = true) ->
                        "이미 가입된 이메일입니다. 로그인으로 진행해주세요."
                    else -> messageText
                }
            }
            ?: when {
                "invalid login credentials" in normalizedBody ->
                    "이메일 또는 비밀번호가 올바르지 않습니다."
                "email not confirmed" in normalizedBody ->
                    "이메일 인증이 아직 완료되지 않았습니다. 메일함을 확인해주세요."
                "user already registered" in normalizedBody || "email exists" in normalizedBody ->
                    "이미 가입된 이메일입니다. 로그인으로 진행해주세요."
                else -> null
            }
            ?: if ("anonymous_provider_disabled" in rawBody) {
                "Supabase anonymous sign-ins are disabled. Enable Anonymous provider in Supabase Auth settings."
            } else {
                "Supabase 인증에 실패했습니다."
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

    private fun parseSessionFromCallbackUrl(url: String): SupabaseSession {
        val params = buildMap {
            parseKeyValuePairs(url.substringAfter('?', "")).forEach { (key, value) ->
                put(key, value)
            }
            parseKeyValuePairs(url.substringAfter('#', "")).forEach { (key, value) ->
                put(key, value)
            }
        }

        val errorDescription = params["error_description"] ?: params["error"]
        if (!errorDescription.isNullOrBlank()) {
            throw Exception(errorDescription)
        }

        val accessToken = params["access_token"].orEmpty().trim()
        val refreshToken = params["refresh_token"].orEmpty().trim()
        val expiresAt = params["expires_at"]?.toLongOrNull()
            ?: params["expires_in"]?.toLongOrNull()?.let { currentEpochSeconds() + it }
            ?: currentEpochSeconds() + 3600

        require(accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            "Supabase OAuth callback is missing session tokens."
        }

        return SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt
        )
    }

    private fun parseKeyValuePairs(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()

        return raw.split("&")
            .mapNotNull { entry ->
                val separatorIndex = entry.indexOf('=')
                if (separatorIndex <= 0) return@mapNotNull null
                val key = entry.substring(0, separatorIndex).decodeURLQueryComponent()
                val value = entry.substring(separatorIndex + 1).decodeURLQueryComponent()
                key to value
            }
            .toMap()
    }

    private fun currentEpochSeconds(): Long = kotlin.time.Clock.System.now().epochSeconds

    private fun HttpRequestBuilder.applySupabaseHeaders() {
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

internal enum class EmailSignUpResult {
    SignedIn,
    EmailConfirmationRequired
}

@Serializable
private data class SupabaseRefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
private data class SupabaseEmailSignUpRequest(
    val email: String,
    val password: String
)

@Serializable
private data class SupabasePasswordLoginRequest(
    val email: String,
    val password: String
)

@Serializable
private data class SupabaseResendOtpRequest(
    val type: String,
    val email: String
)

@Serializable
private data class SupabaseRecoverPasswordRequest(
    val email: String,
    @SerialName("redirect_to")
    val redirectTo: String
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
    @SerialName("code")
    val errorCode: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
    val msg: String? = null
) {
    fun message(): String? = errorDescription ?: msg ?: error
}
