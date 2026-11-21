package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private const val PLACEHOLDER_ALPHA = 0.45f
private const val OUTLINE_ALPHA = 0.1f

/**
 * The shared look of HuKi's text fields: a white pill with a hairline outline that turns primary while focused.
 */
object HuKiTextFieldDefaults {
    val singleLineShape: Shape = CircleShape
    val multiLineShape: Shape = RoundedCornerShape(24.dp)

    /**
     * Colors for an [androidx.compose.material3.OutlinedTextField].
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor: Color = outlineColor,
    ): TextFieldColors {
        // Clearly lighter than typed text, so a hint never reads as a filled in value
        val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PLACEHOLDER_ALPHA)
        return OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
        )
    }

    /**
     * The hairline outline of an unfocused field, light enough to read on white and on tinted sheets alike.
     */
    val outlineColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface.copy(alpha = OUTLINE_ALPHA)
}
