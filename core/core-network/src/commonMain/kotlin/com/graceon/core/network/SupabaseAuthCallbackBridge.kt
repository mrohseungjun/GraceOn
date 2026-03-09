package com.graceon.core.network

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

const val SUPABASE_AUTH_REDIRECT_URL = "graceon://auth/callback"

internal object SupabaseAuthCallbackBridge {
    private val _callbackUrls = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val callbackUrls: SharedFlow<String> = _callbackUrls

    fun emit(url: String) {
        if (url.isNotBlank()) {
            _callbackUrls.tryEmit(url)
        }
    }
}

fun handleSupabaseAuthCallbackUrl(url: String) {
    SupabaseAuthCallbackBridge.emit(url)
}
