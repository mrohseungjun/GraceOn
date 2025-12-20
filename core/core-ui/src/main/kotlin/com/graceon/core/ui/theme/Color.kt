package com.graceon.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================
// Primary Brand Colors - Teal/Cyan 기반 모던 팔레트
// ============================================
val Primary = Color(0xFF0D9488)           // Teal 600 - 메인 브랜드 컬러
val PrimaryLight = Color(0xFF14B8A6)      // Teal 500
val PrimaryDark = Color(0xFF0F766E)       // Teal 700
val PrimaryContainer = Color(0xFFCCFBF1)  // Teal 100

val Secondary = Color(0xFF0EA5E9)         // Sky 500 - 보조 컬러
val SecondaryLight = Color(0xFF38BDF8)    // Sky 400
val SecondaryDark = Color(0xFF0284C7)     // Sky 600
val SecondaryContainer = Color(0xFFE0F2FE) // Sky 100

val Tertiary = Color(0xFFF59E0B)          // Amber 500 - 강조 컬러
val TertiaryLight = Color(0xFFFBBF24)     // Amber 400
val TertiaryContainer = Color(0xFFFEF3C7) // Amber 100

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

// Teal (추가)
val TealGradientStart = Color(0xFF5EEAD4)
val TealGradientEnd = Color(0xFF14B8A6)

// ============================================
// Background & Surface
// ============================================
val BackgroundLight = Color(0xFFF8FAFC)   // Slate 50
val BackgroundDark = Color(0xFF0F172A)    // Slate 900
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E293B)       // Slate 800

// Card Colors
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E293B)
val CardElevated = Color(0xFFF1F5F9)      // Slate 100

// ============================================
// Text Colors
// ============================================
val TextPrimary = Color(0xFF0F172A)       // Slate 900
val TextSecondary = Color(0xFF475569)     // Slate 600
val TextTertiary = Color(0xFF94A3B8)      // Slate 400
val TextOnDark = Color(0xFFF8FAFC)        // Slate 50
val TextOnPrimary = Color.White

// ============================================
// Glassmorphism & Effects
// ============================================
val GlassWhite = Color(0x80FFFFFF)
val GlassBlack = Color(0x40000000)
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
