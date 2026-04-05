package com.example.mobileinventory.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ===== CALMING GREEN PALETTE — Premium & Professional =====

// Primary family — Sage / Forest
val GreenPrimary = Color(0xFF2E7D5B)            // Deep calming sage
val GreenPrimaryDark = Color(0xFF1B5E40)        // Darker forest
val GreenOnPrimary = Color(0xFFFFFFFF)
val GreenPrimaryContainer = Color(0xFFCBEED9)   // Very soft mint container
val GreenOnPrimaryContainer = Color(0xFF0C2818)

// Secondary family — Teal-leaning green
val GreenSecondary = Color(0xFF4A9E7E)          // Muted teal-green
val GreenOnSecondary = Color(0xFFFFFFFF)
val GreenSecondaryContainer = Color(0xFFD4F2E4) // Pastel mint
val GreenOnSecondaryContainer = Color(0xFF0E2A1C)

// Tertiary — Warm sage for accents
val GreenTertiary = Color(0xFF6B8F71)           // Muted olive-sage
val GreenTertiaryContainer = Color(0xFFE8F5E9)

// Surfaces & backgrounds
val GreenBackground = Color(0xFFF6FAF7)         // Almost-white with a hint of green
val GreenSurface = Color(0xFFFFFFFF)
val GreenSurfaceVariant = Color(0xFFE8F0EB)     // Light sage gray
val GreenOnBackground = Color(0xFF1A2E22)       // Deep forest text
val GreenOnSurface = Color(0xFF1A2E22)
val GreenOnSurfaceVariant = Color(0xFF3F5A48)   // Muted green-gray text
val GreenOutline = Color(0xFFAEC4B5)            // Soft green border
val GreenOutlineVariant = Color(0xFFD0DDD4)

// Error
val GreenError = Color(0xFFBF4040)
val GreenErrorContainer = Color(0xFFFCE4E4)
val GreenOnError = Color(0xFFFFFFFF)
val GreenOnErrorContainer = Color(0xFF410002)

// Special surface tints
val GreenSurfaceTint = Color(0xFF2E7D5B)
val GreenInverseSurface = Color(0xFF2B3530)
val GreenInverseOnSurface = Color(0xFFEFF5F0)
val GreenInversePrimary = Color(0xFF8FD8AF)

// ===== DARK THEME — Forest Night =====
val DarkGreenPrimary = Color(0xFF8FD8AF)        // Soft glowing green
val DarkGreenOnPrimary = Color(0xFF003920)
val DarkGreenPrimaryContainer = Color(0xFF1B5E40)
val DarkGreenOnPrimaryContainer = Color(0xFFCBEED9)

val DarkGreenSecondary = Color(0xFFA5D4BD)
val DarkGreenOnSecondary = Color(0xFF003826)
val DarkGreenSecondaryContainer = Color(0xFF285A40)
val DarkGreenOnSecondaryContainer = Color(0xFFD4F2E4)

val DarkGreenBackground = Color(0xFF0E1712)     // Very deep forest
val DarkGreenSurface = Color(0xFF151E18)         // Dark card surface
val DarkGreenSurfaceVariant = Color(0xFF273D2E)  // Elevated dark surface
val DarkGreenOnBackground = Color(0xFFDAE8DF)
val DarkGreenOnSurface = Color(0xFFDAE8DF)
val DarkGreenOnSurfaceVariant = Color(0xFFBDCEC4)
val DarkGreenOutline = Color(0xFF6A8572)
val DarkGreenOutlineVariant = Color(0xFF3D5245)

// ===== CUSTOM COLORS (for gradient / login accents) =====
val LoginGradientStart = Color(0xFF1B5E40)      // Deep emerald
val LoginGradientEnd = Color(0xFF2E7D5B)        // Sage
val LoginCardBackground = Color(0xFFFDFFFE)     // Crisp white with warmth
val LoginSubtleAccent = Color(0xFFE0F2EB)       // Subtle mint wash

// ===== COLOR SCHEMES =====

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenOnSecondaryContainer,
    tertiary = GreenTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    background = GreenBackground,
    onBackground = GreenOnBackground,
    surface = GreenSurface,
    onSurface = GreenOnSurface,
    surfaceVariant = GreenSurfaceVariant,
    onSurfaceVariant = GreenOnSurfaceVariant,
    outline = GreenOutline,
    outlineVariant = GreenOutlineVariant,
    error = GreenError,
    onError = GreenOnError,
    errorContainer = GreenErrorContainer,
    onErrorContainer = GreenOnErrorContainer,
    surfaceTint = GreenSurfaceTint,
    inverseSurface = GreenInverseSurface,
    inverseOnSurface = GreenInverseOnSurface,
    inversePrimary = GreenInversePrimary,
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = DarkGreenOnPrimary,
    primaryContainer = DarkGreenPrimaryContainer,
    onPrimaryContainer = DarkGreenOnPrimaryContainer,
    secondary = DarkGreenSecondary,
    onSecondary = DarkGreenOnSecondary,
    secondaryContainer = DarkGreenSecondaryContainer,
    onSecondaryContainer = DarkGreenOnSecondaryContainer,
    background = DarkGreenBackground,
    onBackground = DarkGreenOnBackground,
    surface = DarkGreenSurface,
    onSurface = DarkGreenOnSurface,
    surfaceVariant = DarkGreenSurfaceVariant,
    onSurfaceVariant = DarkGreenOnSurfaceVariant,
    outline = DarkGreenOutline,
    outlineVariant = DarkGreenOutlineVariant,
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFCE4E4),
    inverseSurface = Color(0xFFDAE8DF),
    inverseOnSurface = Color(0xFF2B3530),
    inversePrimary = Color(0xFF2E7D5B)
)

// ===== TYPOGRAPHY =====

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun InventoryProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use a darker green for the status bar for a cleaner look
            window.statusBarColor = if (darkTheme) {
                DarkGreenBackground.toArgb()
            } else {
                GreenPrimaryDark.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
