package com.graceon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun PlatformAppContent(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit
) {
    val dependencies = remember(apiBaseUrl, supabaseAnonKey) {
        createGraceOnIosDependencies(
            apiBaseUrl = apiBaseUrl,
            supabaseAnonKey = supabaseAnonKey
        )
    }
    GraceOnRoot(
        dependencies = dependencies,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification
    )
}
