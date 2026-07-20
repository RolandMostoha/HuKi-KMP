package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import android.graphics.Color as AndroidColor

private const val DARK_BRIGHTNESS_BOOST = 0.30f
private const val DARK_SATURATION_DROP = 0.12f
private const val LIGHT_BRIGHTNESS_DROP = 0.18f

/**
 * Brightens on a dark (near-black), darkens on a light (near-white) surface.
 */
@Composable
@ReadOnlyComposable
fun Color.adaptiveTint(): Color {
    val onDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (onDarkSurface) {
        adjustHsv(saturationDelta = -DARK_SATURATION_DROP, valueDelta = DARK_BRIGHTNESS_BOOST)
    } else {
        adjustHsv(saturationDelta = 0f, valueDelta = -LIGHT_BRIGHTNESS_DROP)
    }
}

private fun Color.adjustHsv(saturationDelta: Float, valueDelta: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(toArgb(), hsv)
    hsv[1] = (hsv[1] + saturationDelta).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] + valueDelta).coerceIn(0f, 1f)
    return Color(AndroidColor.HSVToColor(hsv))
}
