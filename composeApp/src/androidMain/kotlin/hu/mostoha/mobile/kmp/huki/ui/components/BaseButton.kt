package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import hu.mostoha.mobile.kmp.huki.theme.Dimens

@Composable
fun BaseButton(
    @DrawableRes iconResId: Int,
    text: String,
    colors: ButtonColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        colors = colors,
        contentPadding = PaddingValues(horizontal = Dimens.ExtraLarge, vertical = Dimens.Medium),
    ) {
        Icon(
            modifier = Modifier.size(Dimens.IconSmall),
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = contentDescription,
        )
        Spacer(modifier = Modifier.width(Dimens.Medium))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
