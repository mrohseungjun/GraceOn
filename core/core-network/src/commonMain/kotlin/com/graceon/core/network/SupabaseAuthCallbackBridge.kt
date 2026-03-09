package com.graceon.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val SUPABASE_AUTH_REDIRECT_URL = "graceon://auth/callback"

internal object SupabaseAuthCallbackBridge {
    private val _callbackUrl = MutableStateFlow<String?>(null)

    val callbackUrl: StateFlow<String?> = _callbackUrl

    fun emit(url: String) {
        if (url.isNotBlank()) {
            _callbackUrl.value = url
        }
    }

    fun clear() {
        _callbackUrl.value = null
    }
}

fun handleSupabaseAuthCallbackUrl(url: String) {
    SupabaseAuthCallbackBridge.emit(url)
}
