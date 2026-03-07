package com.graceon.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

private val GraceOnLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F6FC),
    onPrimaryContainer = Color(0xFF0A4E5C),
    secondary = Color(0xFF5F6F84),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7EEF7),
    onSecondaryContainer = Color(0xFF23313F),
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDF9D1),
    onTertiaryContainer = Color(0xFF314405),
    background = Color(0xFFF6F8FB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFD6DEE8),
    outlineVariant = Color(0xFFE5EAF0),
    error = ErrorColor,
    onError = Color.White,
)

@Composable
fun GraceOnTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) GraceOnColorScheme else GraceOnLightColorScheme,
        typography = Typography,
        content = content
    )
}
