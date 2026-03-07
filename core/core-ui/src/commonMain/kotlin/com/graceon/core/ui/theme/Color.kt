package com.graceon.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================
// Primary Brand Colors - Dark glass palette
// ============================================
val Primary = Color(0xFF13C8EC)
val PrimaryLight = Color(0xFF6EE5FF)
val PrimaryDark = Color(0xFF0C8FAA)
val PrimaryContainer = Color(0xFF0D1B24)

val Secondary = Color(0xFF8EA0B7)
val SecondaryLight = Color(0xFFB8C6D9)
val SecondaryDark = Color(0xFF0B1017)
val SecondaryContainer = Color(0xFF131B27)

val Tertiary = Color(0xFFA3E635)
val TertiaryLight = Color(0xFFD9F99D)
val TertiaryContainer = Color(0xFF1C2615)

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
val CategoryBlue = Color(0xFF3B82F6)
val CategoryBlueBg = Color(0xFFDBEAFE)
val BlueGradientStart = Color(0xFF60A5FA)
val BlueGradientEnd = Color(0xFF3B82F6)

// 관계 - Rose/Pink
val CategoryPink = Color(0xFFEC4899)
val CategoryPinkBg = Color(0xFFFCE7F3)
val PinkGradientStart = Color(0xFFF472B6)
val PinkGradientEnd = Color(0xFFEC4899)

// 일상/건강 - Amber/Yellow
val CategoryAmber = Color(0xFFF59E0B)
val CategoryAmberBg = Color(0xFFFEF3C7)
val YellowGradientStart = Color(0xFFFBBF24)
val YellowGradientEnd = Color(0xFFF59E0B)

// 사랑/감정 - Purple
val CategoryPurple = Color(0xFF8B5CF6)
val CategoryPurpleBg = Color(0xFFEDE9FE)
val PurpleGradientStart = Color(0xFFA78BFA)
val PurpleGradientEnd = Color(0xFF8B5CF6)

// Cyan gradient helpers
val TealGradientStart = Color(0xFF67E8F9)
val TealGradientEnd = Color(0xFF13C8EC)

// ============================================
// Background & Surface
// ============================================
val BackgroundLight = Color(0xFF05070A)
val BackgroundDark = Color(0xFF05070A)
val SurfaceLight = Color(0xFF0B1017)
val SurfaceDark = Color(0xFF0B1017)

// Card Colors
val CardLight = Color(0xFF111827)
val CardDark = Color(0xFF111827)
val CardElevated = Color(0xFF121923)

// ============================================
// Text Colors
// ============================================
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)
val TextOnDark = Color(0xFFF8FAFC)
val TextOnPrimary = Color.White

// ============================================
// Glassmorphism & Effects
// ============================================
val GlassWhite = Color(0x12FFFFFF)
val GlassBlack = Color(0x66000000)
val GlassSurface = Color(0x0FFFFFFF)
val GlassSurfaceStrong = Color(0x16FFFFFF)
val GlassBorder = Color(0x18FFFFFF)
val Scrim = Color(0x52000000)

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
