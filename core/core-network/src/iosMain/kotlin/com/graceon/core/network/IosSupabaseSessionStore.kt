package com.graceon.core.network

import platform.Foundation.NSUserDefaults

class IosSupabaseSessionStore : SupabaseSessionStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun load(): SupabaseSession? {
        val accessToken = defaults.stringForKey("supabase_access_token").orEmpty()
        val refreshToken = defaults.stringForKey("supabase_refresh_token").orEmpty()
        val expiresAt = defaults.integerForKey("supabase_expires_at_epoch_seconds")
        val email = defaults.stringForKey("supabase_email")

        if (accessToken.isBlank() || refreshToken.isBlank() || expiresAt <= 0L) return null

        return SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            email = email
        )
    }

    override fun save(session: SupabaseSession) {
        defaults.setObject(session.accessToken, forKey = "supabase_access_token")
        defaults.setObject(session.refreshToken, forKey = "supabase_refresh_token")
        defaults.setInteger(session.expiresAtEpochSeconds, forKey = "supabase_expires_at_epoch_seconds")
        defaults.setObject(session.email, forKey = "supabase_email")
    }

    override fun clear() {
        defaults.removeObjectForKey("supabase_access_token")
        defaults.removeObjectForKey("supabase_refresh_token")
        defaults.removeObjectForKey("supabase_expires_at_epoch_seconds")
        defaults.removeObjectForKey("supabase_email")
    }
}
