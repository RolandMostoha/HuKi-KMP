package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import hu.mostoha.mobile.android.huki.R

private const val UNCHECKED_TRACK_ALPHA = 0.1f
private const val UNCHECKED_THUMB_ALPHA = 0.6f
private const val UNCHECKED_BORDER_ALPHA = 0.6f

/**
 * The shared look of HuKi's switches: primary while on, a neutral grey instead of the theme's tinted one while off.
 */
object HuKiSwitchDefaults {
    /**
     * Colors for an [androidx.compose.material3.Switch]. With [keepsColorsWhenDisabled] a disabled switch keeps them,
     * for a switch whose whole row fades when disabled.
     */
    @Composable
    fun colors(keepsColorsWhenDisabled: Boolean = false): SwitchColors {
        val primary = MaterialTheme.colorScheme.primary
        val neutral = MaterialTheme.colorScheme.onSurface
        val uncheckedTrack = neutral.copy(alpha = UNCHECKED_TRACK_ALPHA)
        val uncheckedThumb = neutral.copy(alpha = UNCHECKED_THUMB_ALPHA)
        val uncheckedBorder = neutral.copy(alpha = UNCHECKED_BORDER_ALPHA)
        val colors = SwitchDefaults.colors(
            checkedTrackColor = primary,
            checkedIconColor = primary,
            uncheckedTrackColor = uncheckedTrack,
            uncheckedThumbColor = uncheckedThumb,
            uncheckedBorderColor = uncheckedBorder,
        )
        return if (keepsColorsWhenDisabled) {
            colors.copy(
                disabledCheckedTrackColor = primary,
                disabledCheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                disabledCheckedIconColor = primary,
                disabledUncheckedTrackColor = uncheckedTrack,
                disabledUncheckedThumbColor = uncheckedThumb,
                disabledUncheckedBorderColor = uncheckedBorder,
            )
        } else {
            colors
        }
    }

    /**
     * The check shown in the thumb while the switch is on, for the [androidx.compose.material3.Switch] `thumbContent`.
     */
    fun thumbContent(checked: Boolean): (@Composable () -> Unit)? =
        if (checked) {
            {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
}
