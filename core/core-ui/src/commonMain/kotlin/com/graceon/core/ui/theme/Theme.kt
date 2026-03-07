package com.graceon.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GraceOnColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF031319),
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryLight,
    secondary = Secondary,
    onSecondary = TextOnDark,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = SecondaryLight,
    tertiary = Tertiary,
    onTertiary = Color(0xFF11190A),
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = TertiaryLight,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardElevated,
    onSurfaceVariant = TextSecondary,
    outline = TextTertiary,
    outlineVariant = GlassBorder,
    error = ErrorColor,
    onError = TextOnPrimary,
)

@Composable
fun GraceOnTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GraceOnColorScheme,
        typography = Typography,
        content = content
    )
}
