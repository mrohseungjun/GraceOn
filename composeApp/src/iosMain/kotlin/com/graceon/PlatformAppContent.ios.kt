package com.graceon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun PlatformAppContent(
    apiBaseUrl: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit
) {
    val dependencies = remember(apiBaseUrl) { createGraceOnIosDependencies(apiBaseUrl = apiBaseUrl) }
    GraceOnRoot(
        dependencies = dependencies,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification
    )
}
