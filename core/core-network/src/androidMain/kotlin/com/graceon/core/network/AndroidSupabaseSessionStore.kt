package com.graceon.core.network

import android.content.Context

class AndroidSupabaseSessionStore(
    context: Context
) : SupabaseSessionStore {
    private val preferences = context.getSharedPreferences("supabase_auth_session", Context.MODE_PRIVATE)

    override fun load(): SupabaseSession? {
        val accessToken = preferences.getString("access_token", null).orEmpty()
        val refreshToken = preferences.getString("refresh_token", null).orEmpty()
        val expiresAt = preferences.getLong("expires_at_epoch_seconds", 0L)
        val email = preferences.getString("email", null)

        if (accessToken.isBlank() || refreshToken.isBlank() || expiresAt <= 0L) return null

        return SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            email = email
        )
    }

    override fun save(session: SupabaseSession) {
        preferences.edit()
            .putString("access_token", session.accessToken)
            .putString("refresh_token", session.refreshToken)
            .putLong("expires_at_epoch_seconds", session.expiresAtEpochSeconds)
            .putString("email", session.email)
            .apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }
}
