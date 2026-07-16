package hu.mostoha.mobile.kmp.huki.ui.features.gpx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.ui.components.PrimaryButton
import hu.mostoha.mobile.kmp.huki.ui.components.SecondaryButton
import hu.mostoha.mobile.kmp.huki.ui.components.StatChip
import hu.mostoha.mobile.kmp.huki.ui.components.StatChipStyle
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun GpxDetailsBottomSheet(
    gpxDetails: GpxDetails,
    onStartClick: () -> Unit,
    onNavigateToStart: () -> Unit,
    onNavigateToEnd: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCollapsedHeightMeasured: (Dp) -> Unit = {},
) {
    val density = LocalDensity.current
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(
            topStart = Dimens.ExtraLarge,
            topEnd = Dimens.ExtraLarge,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.Small,
        ),
    ) {
        Column(
            modifier = Modifier.padding(top = Dimens.Small),
        ) {
            Column(
                modifier = Modifier
                    .onGloballyPositioned {
                        onCollapsedHeightMeasured(
                            with(density) { it.size.height.toDp() } + navBarInset +
                                Dimens.Small + Dimens.Small + Dimens.GpxNavButtonPeek,
                        )
                    },
            ) {
                DragHandle(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalPadding = Dimens.SmallMedium,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Dimens.ExtraLarge,
                            end = Dimens.Large,
                            bottom = Dimens.Medium,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
                    ) {
                        if (gpxDetails.title != null) {
                            Text(
                                text = gpxDetails.fileName,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = gpxDetails.title ?: gpxDetails.fileName,
                            modifier = Modifier.testTag(TestTags.GPX_DETAILS_TITLE),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.testTag(TestTags.GPX_DETAILS_CLOSE_BUTTON),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                            contentDescription = mokoString(SharedRes.strings.a11y_close),
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.82f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(top = Dimens.ExtraSmall),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
                ) {
                    StatChip(
                        iconResId = R.drawable.ic_clock,
                        value = mokoString(TravelTimeFormatter.formatTravelTime(gpxDetails.travelTime)),
                        label = mokoString(SharedRes.strings.gpx_details_travel_time),
                        style = StatChipStyle.Large,
                        modifier = Modifier.weight(1f),
                    )
                    StatChip(
                        iconResId = R.drawable.ic_place_circle,
                        value = DistanceFormatter.formatDistance(gpxDetails.totalDistance),
                        label = mokoString(SharedRes.strings.gpx_details_distance),
                        style = StatChipStyle.Large,
                        modifier = Modifier.weight(1f),
                    )
                    StatChip(
                        iconResId = R.drawable.ic_up_double,
                        value = DistanceFormatter.formatMeters(gpxDetails.incline),
                        label = mokoString(SharedRes.strings.gpx_details_incline),
                        style = StatChipStyle.Large,
                        modifier = Modifier.weight(1f),
                    )
                    StatChip(
                        iconResId = R.drawable.ic_down_double,
                        value = DistanceFormatter.formatMeters(gpxDetails.decline),
                        label = mokoString(SharedRes.strings.gpx_details_decline),
                        style = StatChipStyle.Large,
                        modifier = Modifier.weight(1f),
                    )
                }
                PrimaryButton(
                    iconResId = R.drawable.ic_fab_my_location_live_compass,
                    text = mokoString(SharedRes.strings.gpx_details_start),
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.GPX_DETAILS_START_BUTTON)
                        .padding(
                            start = Dimens.ExtraLarge,
                            top = Dimens.Large,
                            end = Dimens.ExtraLarge,
                        ),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.ExtraLarge,
                        top = Dimens.Medium,
                        end = Dimens.ExtraLarge,
                        bottom = navBarInset + Dimens.Small,
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
            ) {
                SecondaryButton(
                    iconResId = SharedRes.images.ic_maps_navigation.drawableResId,
                    text = mokoString(SharedRes.strings.gpx_details_navigation_to_start),
                    onClick = onNavigateToStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.GPX_DETAILS_NAV_START_BUTTON),
                )
                if (gpxDetails.waypoints.firstOrNull { it.type == WaypointType.END }?.location != null) {
                    SecondaryButton(
                        iconResId = SharedRes.images.ic_maps_navigation.drawableResId,
                        text = mokoString(SharedRes.strings.gpx_details_navigation_to_end),
                        onClick = onNavigateToEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.GPX_DETAILS_NAV_END_BUTTON),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GpxDetailsBottomSheetPreview() {
    HuKiTheme {
        GpxDetailsBottomSheet(
            gpxDetails = GpxDetails(
                fileName = "3_Kossuth__Fenyves_vo_HuKi018.gpx",
                fileUri = "",
                title = "OKT-15 - Rozalia teglagyar - Dobogoko",
                locations = emptyList(),
                waypoints = listOf(
                    GpxWaypoint(Location(47.0, 19.0), WaypointType.START),
                    GpxWaypoint(Location(48.0, 20.0), WaypointType.END),
                ),
                bounds = emptyList(),
                totalDistance = 6.5.kilometers,
                travelTime = 2.hours.plus(15.minutes),
                altitudeRange = 63 to 313,
                incline = 250,
                decline = 63,
            ),
            onStartClick = {},
            onNavigateToStart = {},
            onNavigateToEnd = {},
            onCloseClick = {},
        )
    }
}
