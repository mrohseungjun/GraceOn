package com.graceon

import androidx.compose.runtime.Composable

@Composable
fun App(
    apiKey: String = "",
    appVersion: String = "",
    onShareText: (String) -> Unit = {},
    onToggleDailyVerseNotification: (Boolean) -> Unit = {}
) {
    PlatformAppContent(
        apiKey = apiKey,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification
    )
}

@Composable
internal expect fun PlatformAppContent(
    apiKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit
)
