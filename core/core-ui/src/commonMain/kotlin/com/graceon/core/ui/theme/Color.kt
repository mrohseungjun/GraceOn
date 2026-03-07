package com.graceon.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================
// Primary Brand Colors - Warm grace palette
// ============================================
val Primary = Color(0xFFAA7A59)
val PrimaryLight = Color(0xFFE7D1BF)
val PrimaryDark = Color(0xFF7A543D)
val PrimaryContainer = Color(0xFF3B2A21)

val Secondary = Color(0xFFC3A089)
val SecondaryLight = Color(0xFFECDCCD)
val SecondaryDark = Color(0xFF261B16)
val SecondaryContainer = Color(0xFF31241E)

val Tertiary = Color(0xFFD2AE88)
val TertiaryLight = Color(0xFFF1E0CD)
val TertiaryContainer = Color(0xFF473428)

// Legacy aliases (기존 코드 호환용)
val IndigoPrimary = Primary
val IndigoSecondary = PrimaryLight
val PurplePrimary = Secondary
val PurpleSecondary = SecondaryLight
val AccentTeal = Primary
val AccentCoral = Tertiary
val AccentAmber = Tertiary

// ============================================
// Category Colors - 각 카테고리별 고유 색상
// ============================================
// 일/진로 - Blue
val CategoryBlue = Color(0xFF936C52)
val CategoryBlueBg = Color(0xFFF4E7DA)
val BlueGradientStart = Color(0xFFC39A79)
val BlueGradientEnd = Color(0xFF8B6A56)

// 관계 - Rose/Pink
val CategoryPink = Color(0xFFB37669)
val CategoryPinkBg = Color(0xFFF6E3DB)
val PinkGradientStart = Color(0xFFCD9588)
val PinkGradientEnd = Color(0xFFA96E63)

// 일상/건강 - Amber/Yellow
val CategoryAmber = Color(0xFFC9985B)
val CategoryAmberBg = Color(0xFFF8EBD8)
val YellowGradientStart = Color(0xFFE0B57B)
val YellowGradientEnd = Color(0xFFC89A61)

// 사랑/감정 - Purple
val CategoryPurple = Color(0xFF9A7368)
val CategoryPurpleBg = Color(0xFFF3E5DE)
val PurpleGradientStart = Color(0xFFC0998D)
val PurpleGradientEnd = Color(0xFF8D6B63)

// Warm gradient helpers
val TealGradientStart = Color(0xFFE4C4A5)
val TealGradientEnd = Color(0xFFAA7A59)

// ============================================
// Background & Surface
// ============================================
val BackgroundLight = Color(0xFFF7EFE6)
val BackgroundDark = Color(0xFF130F0D)
val SurfaceLight = Color(0xFFFCF5EE)
val SurfaceDark = Color(0xFF1C1512)

// Card Colors
val CardLight = Color(0xFFF0E4D7)
val CardDark = Color(0xFF241B17)
val CardElevated = Color(0xFF2B211D)

// ============================================
// Text Colors
// ============================================
val TextPrimary = Color(0xFFF7EFE8)
val TextSecondary = Color(0xFFC5B0A2)
val TextTertiary = Color(0xFF917B6E)
val TextOnDark = Color(0xFFF7EFE8)
val TextOnPrimary = Color(0xFFFFFBF8)

// ============================================
// Glassmorphism & Effects
// ============================================
val GlassWhite = Color(0x1AF7EBDD)
val GlassBlack = Color(0x6633251D)
val GlassSurface = Color(0x24EEDCCB)
val GlassSurfaceStrong = Color(0x38E8D2BD)
val GlassBorder = Color(0x30D0B49C)
val Scrim = Color(0x66403027)

// ============================================
// Status Colors
// ============================================
val SuccessColor = Color(0xFF10B981)      // Emerald 500
val WarningColor = Color(0xFFF59E0B)      // Amber 500
val ErrorColor = Color(0xFFEF4444)        // Red 500
val InfoColor = Color(0xFF3B82F6)         // Blue 500

// ============================================
// Gradients
// ============================================
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Primary, Secondary)
)

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(Secondary, Tertiary)
)
