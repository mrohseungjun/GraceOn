package com.graceon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun PlatformAppContent() {
    val dependencies = remember { createGraceOnIosDependencies(apiKey = "") }
    GraceOnRoot(dependencies = dependencies)
}
