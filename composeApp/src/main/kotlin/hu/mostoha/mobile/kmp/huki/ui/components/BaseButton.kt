package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import hu.mostoha.mobile.kmp.huki.theme.Dimens

@Composable
fun BaseButton(
    @DrawableRes iconResId: Int,
    text: String,
    colors: ButtonColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.REGULAR,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides size.minimumInteractiveSize()) {
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = size.minHeight),
            enabled = enabled,
            shape = RoundedCornerShape(percent = 50),
            colors = colors,
            contentPadding = PaddingValues(horizontal = Dimens.ExtraLarge, vertical = size.verticalPadding),
        ) {
            Icon(
                modifier = Modifier.size(size.iconSize),
                imageVector = ImageVector.vectorResource(iconResId),
                contentDescription = contentDescription,
            )
            Spacer(modifier = Modifier.width(size.iconSpacing))
            Text(
                text = text,
                style = size.textStyle().copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

enum class ButtonSize(
    val minHeight: Dp,
    val verticalPadding: Dp,
    val iconSize: Dp,
    val iconSpacing: Dp,
) {
    REGULAR(
        minHeight = ButtonDefaults.MinHeight,
        verticalPadding = Dimens.Medium,
        iconSize = Dimens.IconSmall,
        iconSpacing = Dimens.Medium,
    ),
    SMALL(
        minHeight = Dimens.ButtonSmallMinHeight,
        verticalPadding = Dimens.ExtraSmall,
        iconSize = Dimens.IconExtraSmall,
        iconSpacing = Dimens.SmallMedium,
    ),
}

@Composable
private fun ButtonSize.textStyle(): TextStyle =
    when (this) {
        ButtonSize.REGULAR -> MaterialTheme.typography.titleMedium
        ButtonSize.SMALL -> MaterialTheme.typography.titleSmall
    }

/**
 * A small button is drawn at its own height, without Material's 48 dp touch-target margin around it.
 */
@Composable
private fun ButtonSize.minimumInteractiveSize(): Dp =
    when (this) {
        ButtonSize.REGULAR -> LocalMinimumInteractiveComponentSize.current
        ButtonSize.SMALL -> Dp.Unspecified
    }
