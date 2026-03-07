package com.graceon.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun NetworkHeroImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
)
