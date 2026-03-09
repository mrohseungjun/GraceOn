package com.graceon

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit,
    onOpenUrl: (String) -> Unit
) = ComposeUIViewController {
    App(
        apiBaseUrl = apiBaseUrl,
        supabaseAnonKey = supabaseAnonKey,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification,
        onOpenUrl = onOpenUrl
    )
}
