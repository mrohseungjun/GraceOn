package com.graceon.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GraceOnColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryLight,
    secondary = Secondary,
    onSecondary = TextOnDark,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = SecondaryLight,
    tertiary = Tertiary,
    onTertiary = TextOnPrimary,
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
    onPrimary = TextOnPrimary,
    primaryContainer = Color(0xFFEBD8C7),
    onPrimaryContainer = Color(0xFF5A3F30),
    secondary = Secondary,
    onSecondary = TextOnPrimary,
    secondaryContainer = Color(0xFFF0DFD0),
    onSecondaryContainer = Color(0xFF573F31),
    tertiary = Tertiary,
    onTertiary = TextOnPrimary,
    tertiaryContainer = Color(0xFFF2E0CC),
    onTertiaryContainer = Color(0xFF624531),
    background = BackgroundLight,
    onBackground = Color(0xFF3D2A1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF3D2A1F),
    surfaceVariant = Color(0xFFEFDFD1),
    onSurfaceVariant = Color(0xFF866858),
    outline = Color(0xFFD5BDAA),
    outlineVariant = Color(0xFFE6D5C6),
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
