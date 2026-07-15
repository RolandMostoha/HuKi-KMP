package hu.mostoha.mobile.kmp.huki.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun MapZoomControls(onZoomInClicked: () -> Unit, onZoomOutClicked: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.width(Dimens.ZoomControlWidth),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = Dimens.FloatingActionElevation,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                modifier = Modifier
                    .testTag(TestTags.MAIN_FAB_ZOOM_IN_BUTTON)
                    .size(Dimens.ZoomControlIconSize),
                onClick = onZoomInClicked,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_zoom_in),
                    contentDescription = mokoString(SharedRes.strings.map_zoom_in_a11y),
                )
            }
            HorizontalDivider(
                modifier = Modifier.size(width = 28.dp, height = 1.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            IconButton(
                modifier = Modifier
                    .testTag(TestTags.MAIN_FAB_ZOOM_OUT_BUTTON)
                    .size(Dimens.ZoomControlIconSize),
                onClick = onZoomOutClicked,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_zoom_out),
                    contentDescription = mokoString(SharedRes.strings.map_zoom_out_a11y),
                )
            }
        }
    }
}

@Preview
@Composable
private fun MapZoomControlsPreview() {
    HuKiTheme {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            MapZoomControls(
                onZoomInClicked = {},
                onZoomOutClicked = {},
            )
        }
    }
}
