package com.graceon

import androidx.compose.runtime.Composable

@Composable
fun App(
    apiBaseUrl: String = "",
    supabaseAnonKey: String = "",
    appVersion: String = "",
    onShareText: (String) -> Unit = {},
    onToggleDailyVerseNotification: (Boolean) -> Unit = {}
) {
    PlatformAppContent(
        apiBaseUrl = apiBaseUrl,
        supabaseAnonKey = supabaseAnonKey,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification
    )
}

@Composable
internal expect fun PlatformAppContent(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit
)
