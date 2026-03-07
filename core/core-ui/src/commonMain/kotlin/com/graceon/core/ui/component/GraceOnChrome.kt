package com.graceon.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurfaceStrong
import com.graceon.core.ui.theme.Primary

enum class GraceOnBottomTab {
    Home,
    Word,
    Saved
}

@Composable
fun GraceOnAmbientBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp, start = 12.dp)
                .size(260.dp)
                .blur(120.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
                .size(220.dp)
                .blur(110.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2563EB).copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
fun GraceOnBottomBar(
    activeTab: GraceOnBottomTab,
    onHomeClick: () -> Unit,
    onWordClick: () -> Unit,
    onSavedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 24.dp,
            shape = RoundedCornerShape(999.dp),
            clip = false
        ),
        color = Color(0xFF0D141D).copy(alpha = 0.96f),
        shape = RoundedCornerShape(999.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomBarButton(
                label = "홈",
                active = activeTab == GraceOnBottomTab.Home,
                onClick = onHomeClick,
                iconLabel = "H"
            )
            BottomBarButton(
                label = "말씀",
                active = activeTab == GraceOnBottomTab.Word,
                onClick = onWordClick,
                iconLabel = "W"
            )
            BottomBarButton(
                label = "저장",
                active = activeTab == GraceOnBottomTab.Saved,
                onClick = onSavedClick,
                iconLabel = "S"
            )
        }
    }
}

@Composable
private fun BottomBarButton(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    iconLabel: String
) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = if (active) Primary.copy(alpha = 0.22f) else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
