package com.graceon

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(
    apiKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit
) = ComposeUIViewController {
    App(
        apiKey = apiKey,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification
    )
}
