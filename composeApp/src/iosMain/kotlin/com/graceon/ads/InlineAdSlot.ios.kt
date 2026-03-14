package com.graceon.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal actual object InlineAdSlotRenderer {
    actual val isEnabled: Boolean = false
    actual fun currentLoadState(placement: InlineAdPlacement): InlineAdLoadState = InlineAdLoadState.Loading

    @Composable
    actual fun Banner(
        placement: InlineAdPlacement,
        modifier: Modifier,
        onLoadStateChanged: (InlineAdLoadState) -> Unit
    ) = Unit
}
