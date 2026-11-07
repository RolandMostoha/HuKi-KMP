package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun PoweredByAttribution(
    logo: Painter,
    logoHeight: Dp,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    logoTint: Color = Color.Unspecified,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { this.contentDescription = contentDescription }
            .padding(Dimens.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mokoString(SharedRes.strings.search_powered_by),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(Dimens.Small))
        Icon(
            painter = logo,
            contentDescription = null,
            modifier = Modifier.height(logoHeight),
            tint = logoTint,
        )
    }
}

@Preview
@Composable
private fun PoweredByAttributionPreview() {
    HuKiTheme {
        PoweredByAttribution(
            logo = painterResource(id = SharedRes.images.img_graphhopper_logo.drawableResId),
            logoHeight = 12.dp,
            logoTint = MaterialTheme.colorScheme.onSurface,
            contentDescription = "",
            onClick = {},
        )
    }
}
