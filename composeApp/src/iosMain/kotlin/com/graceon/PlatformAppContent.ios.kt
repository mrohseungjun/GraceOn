package com.graceon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun PlatformAppContent(apiKey: String) {
    val dependencies = remember(apiKey) { createGraceOnIosDependencies(apiKey = apiKey) }
    GraceOnRoot(dependencies = dependencies)
}
