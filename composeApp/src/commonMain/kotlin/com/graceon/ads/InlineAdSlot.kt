package com.graceon.ads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurface

internal enum class InlineAdPlacement {
    HomeFeed,
    ResultContent
}

internal enum class InlineAdLoadState {
    Loading,
    Loaded,
    Failed
}

internal expect object InlineAdSlotRenderer {
    val isEnabled: Boolean
    fun currentLoadState(placement: InlineAdPlacement): InlineAdLoadState

    @Composable
    fun Banner(
        placement: InlineAdPlacement,
        modifier: Modifier = Modifier,
        onLoadStateChanged: (InlineAdLoadState) -> Unit = {}
    )
}

@Composable
internal fun GraceOnInlineAdSlot(
    placement: InlineAdPlacement,
    modifier: Modifier = Modifier
) {
    if (!InlineAdSlotRenderer.isEnabled) return

    var loadState by remember(placement) {
        mutableStateOf(InlineAdSlotRenderer.currentLoadState(placement))
    }
    if (loadState == InlineAdLoadState.Failed) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "후원 광고",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
            ) {
                InlineAdSlotRenderer.Banner(
                    placement = placement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (loadState == InlineAdLoadState.Loaded) 1f else 0f),
                    onLoadStateChanged = { nextState ->
                        loadState = nextState
                    }
                )

                if (loadState != InlineAdLoadState.Loaded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "광고 불러오는 중...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
