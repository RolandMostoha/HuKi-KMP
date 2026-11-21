package hu.mostoha.mobile.kmp.huki.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Android-only colors.
 */

val LightBackground = Color(0xFFFFFFF7)
val DarkBackground = Color(0xFF2D2D2D)

val LightSurfaceVariant = Color(0xFFF6FBF4)
val DarkSurfaceVariant = Color(0xFF121712)

private val LightSecondarySheet = Color(0xFFFAFAFA)
private val DarkSecondarySheet = Color(0xFF1C1C1C)

/**
 * Background of a sheet opened on top of another sheet, a soft grey that sets it apart and lets white fields stand out.
 */
val secondarySheetColor: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSecondarySheet else LightSecondarySheet

val LightMapInfoWindow = Color.White
val DarkMapInfoWindow = Color.Black

val ColorScheme.dividerColor: Color
    get() = outlineVariant.copy(alpha = 0.2f)
