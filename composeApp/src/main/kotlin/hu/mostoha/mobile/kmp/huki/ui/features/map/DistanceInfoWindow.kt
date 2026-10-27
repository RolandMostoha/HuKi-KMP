package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.DarkMapInfoWindow
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.LightMapInfoWindow
import hu.mostoha.mobile.kmp.huki.theme.LocalIsDarkTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@Composable
fun DistanceInfoWindow(info: DistanceInfoWindowData, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .wrapContentSize()
            .testTagAsResourceId(TestTags.MAP_DISTANCE_INFO_WINDOW)
            .semantics(mergeDescendants = true) {},
        shape = InfoWindowShape(
            cornerRadius = Dimens.Large,
            tailWidth = Dimens.InfoWindowTailWidth,
            tailHeight = Dimens.InfoWindowTailHeight,
        ),
        color = if (LocalIsDarkTheme.current) DarkMapInfoWindow else LightMapInfoWindow,
        shadowElevation = Dimens.Small,
    ) {
        Column(
            modifier = Modifier.padding(
                start = 8.dp,
                top = 5.dp,
                end = 8.dp,
                bottom = 5.dp + Dimens.InfoWindowTailHeight,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            info.name?.let { DistanceInfoWindowTitle(it) }
            info.label?.let { DistanceInfoWindowTitle(mokoString(it)) }
            val distance = info.distance
            val travelTime = info.travelTime
            if (distance != null && travelTime != null) {
                DistanceInfoWindowDetails(distance = distance, travelTime = mokoString(travelTime))
            }
        }
    }
}

@Composable
private fun DistanceInfoWindowTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.widthIn(max = Dimens.InfoWindowNameMaxWidth),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun DistanceInfoWindowDetails(distance: String, travelTime: String) {
    val style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall)) {
        Text(
            text = distance,
            color = mokoColor(SharedRes.colors.primaryStrong),
            style = style.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = "·",
            modifier = Modifier.clearAndSetSemantics {},
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = style,
        )
        Text(text = travelTime, color = MaterialTheme.colorScheme.onSurfaceVariant, style = style)
    }
}

@Preview
@Composable
private fun DistanceInfoWindowPreview() {
    HuKiTheme {
        DistanceInfoWindow(
            info = DistanceInfoWindowData(
                id = 0,
                location = Location(47.5, 19.0),
                name = "Dobogókő",
                label = null,
                distance = "4.2 km",
                travelTime = StringDesc.ResourceFormatted(
                    SharedRes.strings.travel_time_hours_minutes_pattern,
                    1,
                    20,
                ),
                priority = 3,
            ),
        )
    }
}

@Preview
@Composable
private fun DistanceInfoWindowStartPreview() {
    HuKiTheme {
        DistanceInfoWindow(
            info = DistanceInfoWindowData(
                id = 1,
                location = Location(47.5, 19.0),
                name = "Rám-szakadék alsó bejárata, Dömös felől",
                label = StringDesc.Resource(SharedRes.strings.gpx_distance_info_window_start),
                distance = null,
                travelTime = null,
                priority = 2,
            ),
        )
    }
}
