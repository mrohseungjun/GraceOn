package com.graceon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.graceon.core.ui.theme.GraceOnTheme

@Composable
fun App() {
    GraceOnTheme {
        Surface(
            modifier = Modifier,
            color = MaterialTheme.colorScheme.background
        ) {
            PlatformAppContent()
        }
    }
}

@Composable
internal expect fun PlatformAppContent()
