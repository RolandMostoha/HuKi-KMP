package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun InfoWindowPlaceDetailsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val primary = mokoColor(SharedRes.colors.primary)
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = primary.copy(alpha = 0.15f),
            contentColor = primary,
        ),
        modifier = modifier.size(44.dp),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_directions_filled),
            contentDescription = mokoString(SharedRes.strings.okt_info_window_navigate),
        )
    }
}

@Preview
@Composable
private fun InfoWindowPlaceDetailsButtonPreview() {
    HuKiTheme {
        InfoWindowPlaceDetailsButton(onClick = {})
    }
}
