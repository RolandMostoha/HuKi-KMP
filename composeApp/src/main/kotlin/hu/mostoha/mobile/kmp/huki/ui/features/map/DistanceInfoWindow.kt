package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@Composable
fun DistanceInfoWindow(info: DistanceInfoWindowData, modifier: Modifier = Modifier) {
    val contentDescription = mokoString(SharedRes.strings.gpx_distance_info_window_a11y)
    Surface(
        modifier = modifier
            .wrapContentSize()
            .testTagAsResourceId(TestTags.MAP_DISTANCE_INFO_WINDOW)
            .semantics { this.contentDescription = contentDescription },
        shape = InfoWindowShape(
            cornerRadius = Dimens.Large,
            tailWidth = Dimens.InfoWindowTailWidth,
            tailHeight = Dimens.InfoWindowTailHeight,
        ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimens.Small,
    ) {
        Column(
            modifier = Modifier.padding(
                start = 10.dp,
                top = 6.dp,
                end = 10.dp,
                bottom = 6.dp + Dimens.InfoWindowTailHeight,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = info.distance,
                color = mokoColor(SharedRes.colors.primaryStrong),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 14.sp,
            )
            Text(
                text = mokoString(info.travelTime),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Preview
@Composable
private fun DistanceInfoWindowPreview() {
    HuKiTheme {
        DistanceInfoWindow(
            info = DistanceInfoWindowData(
                location = Location(47.5, 19.0),
                distance = "4.2 km",
                travelTime = StringDesc.ResourceFormatted(
                    SharedRes.strings.travel_time_hours_minutes_pattern,
                    1,
                    20,
                ),
            ),
        )
    }
}
