package hu.mostoha.mobile.kmp.huki.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Android-only colors.
 */

val LightBackground = Color(0xFFFFFFF7)
val DarkBackground = Color(0xFF2D2D2D)

val LightSurfaceVariant = Color(0xFFF6FBF4)
val DarkSurfaceVariant = Color(0xFF121712)

val ColorScheme.dividerColor: Color
    get() = outlineVariant.copy(alpha = 0.2f)
