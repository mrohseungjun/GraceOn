package com.graceon

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(
    apiBaseUrl: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit
) = ComposeUIViewController {
    App(
        apiBaseUrl = apiBaseUrl,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification
    )
}
