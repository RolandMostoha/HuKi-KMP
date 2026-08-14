package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val EMPTY_VALUE = "-"

@Composable
fun RouteStatsRow(routeStats: RouteStats?, modifier: Modifier = Modifier, style: StatChipStyle = StatChipStyle.Large) {
    RouteStatsRow(
        travelTime = routeStats?.let { mokoString(TravelTimeFormatter.formatTravelTime(it.travelTime)) } ?: EMPTY_VALUE,
        distance = routeStats?.let { DistanceFormatter.formatDistance(it.distance) } ?: EMPTY_VALUE,
        incline = routeStats?.let { DistanceFormatter.formatMeters(it.incline) } ?: EMPTY_VALUE,
        decline = routeStats?.let { DistanceFormatter.formatMeters(it.decline) } ?: EMPTY_VALUE,
        modifier = modifier,
        style = style,
    )
}

@Composable
fun RouteStatsRow(
    travelTime: String,
    distance: String,
    incline: String,
    decline: String,
    modifier: Modifier = Modifier,
    style: StatChipStyle = StatChipStyle.Large,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            if (style == StatChipStyle.Large) Dimens.Medium else Dimens.SmallMedium,
        ),
    ) {
        StatChip(
            iconResId = R.drawable.ic_clock,
            value = travelTime,
            label = mokoString(SharedRes.strings.gpx_details_travel_time),
            style = style,
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = R.drawable.ic_place_circle,
            value = distance,
            label = mokoString(SharedRes.strings.gpx_details_distance),
            style = style,
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = R.drawable.ic_up_double,
            value = incline,
            label = mokoString(SharedRes.strings.gpx_details_incline),
            style = style,
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = R.drawable.ic_down_double,
            value = decline,
            label = mokoString(SharedRes.strings.gpx_details_decline),
            style = style,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun RouteStatsRowPreview() {
    HuKiTheme {
        RouteStatsRow(
            travelTime = "7h 28m",
            distance = "24.6 km",
            incline = "820 m",
            decline = "760 m",
        )
    }
}

@Preview
@Composable
private fun RouteStatsRowCompactPreview() {
    HuKiTheme {
        RouteStatsRow(
            travelTime = "7h 28m",
            distance = "24.6 km",
            incline = "820 m",
            decline = "760 m",
            style = StatChipStyle.Compact,
        )
    }
}

@Preview
@Composable
private fun RouteStatsRowEmptyPreview() {
    HuKiTheme {
        RouteStatsRow(routeStats = null, style = StatChipStyle.Compact)
    }
}
