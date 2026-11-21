package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiState
import hu.mostoha.mobile.kmp.huki.model.domain.RoundTripOptions
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerMode
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.PrimaryButton
import hu.mostoha.mobile.kmp.huki.ui.components.SecondaryButton
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlin.math.roundToInt

private const val DISABLED_ALPHA = 0.5f
private const val BLOCK_ALPHA = 0.5f
private const val INACTIVE_TRACK_ALPHA = 0.15f
private val DistanceTicksKm = listOf(1, 10, 20, 30)

@Composable
fun RoundTripWizardStartCard(
    start: RoutePlannerWaypoint,
    isExpanded: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Without a location there is nothing to move yet, so the start is picked on the map instead
    val firstAction = if (start.location == null) {
        RoutePlannerStopCardAction(
            iconResId = R.drawable.ic_recenter,
            title = mokoString(SharedRes.strings.route_planner_empty_stop_pick_on_map),
            testTag = TestTags.routePlannerEmptyStopPickOnMap(0),
            onClick = { onEvent(RoutePlannerUiEvents.PickOnMapClicked(start.id)) },
        )
    } else {
        RoutePlannerStopCardAction(
            iconResId = R.drawable.ic_open_with,
            title = mokoString(SharedRes.strings.route_planner_stop_move),
            testTag = TestTags.ROUTE_PLANNER_STOP_MOVE,
            onClick = { onEvent(RoutePlannerUiEvents.StopMoveClicked(start.id)) },
        )
    }
    RoutePlannerStopCard(
        title = start.displayName?.let { mokoString(it) } ?: mokoString(SharedRes.strings.route_planner_my_location),
        hasConnectorAbove = false,
        hasConnectorBelow = false,
        isRemovable = false,
        actions = listOf(
            firstAction,
            RoutePlannerStopCardAction(
                iconResId = R.drawable.ic_search,
                title = mokoString(SharedRes.strings.route_planner_stop_replace),
                testTag = TestTags.ROUTE_PLANNER_STOP_REPLACE,
                onClick = { onEvent(RoutePlannerUiEvents.StopReplaceClicked(start.id)) },
            ),
            RoutePlannerStopCardAction(
                iconResId = R.drawable.ic_edit_square,
                title = mokoString(SharedRes.strings.route_planner_stop_rename),
                testTag = TestTags.ROUTE_PLANNER_STOP_RENAME,
                onClick = { onEvent(RoutePlannerUiEvents.StopRenameClicked(start.id)) },
            ),
        ),
        onRemoveClick = {},
        isExpanded = isExpanded,
        onTitleClick = { onEvent(RoutePlannerUiEvents.StopClicked(start.id)) },
        titleTrailing = {
            IconButton(
                onClick = { onEvent(RoutePlannerUiEvents.StopClicked(start.id)) },
                modifier = Modifier.testTag(TestTags.ROUND_TRIP_WIZARD_START_MORE_BUTTON),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_more_horiz),
                    contentDescription = mokoString(SharedRes.strings.round_trip_wizard_a11y_start_actions),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        modifier = modifier
            .testTag(TestTags.ROUND_TRIP_WIZARD_START_ROW)
            // The top of the light block the length slider continues, which the open card's tint draws over
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = BLOCK_ALPHA),
                shape = RoundedCornerShape(topStart = Dimens.Large, topEnd = Dimens.Large),
            )
            // Lines the icon up with the slider's title below, the open card's tint reaches its own inset past it
            .padding(start = Dimens.Large, top = Dimens.Small, end = Dimens.Large),
    ) {
        RoutePlannerRowIcon(imageVector = mokoImage(WaypointType.ROUND_TRIP.icon), tint = Color.Unspecified)
    }
}

@Composable
fun RoundTripWizardFooter(
    uiState: RoutePlannerUiState,
    isJoinedToStart: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        RoundTripWizardDistanceSlider(
            distanceKm = uiState.roundTrip?.distanceKm ?: RoutePlanConstants.ROUND_TRIP_DEFAULT_DISTANCE_KM,
            onDistanceCommitted = { onEvent(RoutePlannerUiEvents.RoundTripDistanceChanged(it)) },
            isJoinedToStart = isJoinedToStart,
        )
        // The stats replace a shorter placeholder once the loop arrives, and the sheet follows this height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            RoutePlannerRouteStatus(
                error = uiState.routePlanError,
                routeStats = uiState.routeStats,
                isPlanExpected = uiState.isRoutePlanExpected,
                onRetryClick = { onEvent(RoutePlannerUiEvents.RetryClicked) },
                modifier = Modifier.alpha(if (uiState.isRoutePlanLoading) 0f else 1f),
            )
            if (uiState.isRoutePlanLoading) {
                // Drawn over the stats' slot without resizing it, so the sheet keeps its height
                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    RoutePlannerLoading()
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge)) {
            SecondaryButton(
                iconResId = R.drawable.ic_refresh,
                text = mokoString(SharedRes.strings.round_trip_wizard_regenerate),
                onClick = { onEvent(RoutePlannerUiEvents.RoundTripRegenerateClicked) },
                enabled = uiState.isRegenerateEnabled,
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (uiState.isRegenerateEnabled) 1f else DISABLED_ALPHA)
                    .testTag(TestTags.ROUND_TRIP_WIZARD_REGENERATE_BUTTON),
            )
            PrimaryButton(
                iconResId = R.drawable.ic_save,
                text = mokoString(SharedRes.strings.round_trip_wizard_save),
                onClick = { onEvent(RoutePlannerUiEvents.SaveRouteClicked) },
                enabled = uiState.isSaveEnabled,
                modifier = Modifier
                    .weight(1f)
                    .testTag(TestTags.ROUND_TRIP_WIZARD_SAVE_BUTTON),
            )
        }
    }
}

@Composable
fun RoundTripWizardDistanceSlider(
    distanceKm: Int,
    onDistanceCommitted: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isJoinedToStart: Boolean = false,
) {
    // Under the start row the block continues it, alone in the minimized sheet it is rounded all around
    val topCornerRadius by animateDpAsState(
        targetValue = if (isJoinedToStart) 0.dp else Dimens.Large,
        label = "distanceSliderTopCorner",
    )
    val minKm = RoutePlanConstants.ROUND_TRIP_MIN_DISTANCE_KM
    val maxKm = RoutePlanConstants.ROUND_TRIP_MAX_DISTANCE_KM
    // The thumb's value while dragging, the loop is only replanned once it is let go
    var draftKm by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(distanceKm) { draftKm = null }
    val shownKm = draftKm?.roundToInt() ?: distanceKm
    val title = mokoString(SharedRes.strings.round_trip_wizard_distance_title)
    val valueText = mokoString(SharedRes.strings.round_trip_wizard_distance_value, shownKm)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = BLOCK_ALPHA),
                shape = RoundedCornerShape(
                    topStart = topCornerRadius,
                    topEnd = topCornerRadius,
                    bottomStart = Dimens.Large,
                    bottomEnd = Dimens.Large,
                ),
            )
            .padding(horizontal = Dimens.Large, vertical = Dimens.MediumLarge),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(TestTags.ROUND_TRIP_WIZARD_DISTANCE_VALUE),
            )
        }
        Slider(
            value = draftKm ?: distanceKm.toFloat(),
            onValueChange = { draftKm = it },
            onValueChangeFinished = {
                draftKm?.let { onDistanceCommitted(it.roundToInt()) }
                // Settles the thumb on the whole kilometre, also when the distance stays the same
                draftKm = draftKm?.roundToInt()?.toFloat()
            },
            colors = SliderDefaults.colors(
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = INACTIVE_TRACK_ALPHA),
                inactiveTickColor = MaterialTheme.colorScheme.primary,
            ),
            // Rounded to whole kilometres on release
            valueRange = minKm.toFloat()..maxKm.toFloat(),
            modifier = Modifier
                .semantics { contentDescription = title }
                .testTag(TestTags.ROUND_TRIP_WIZARD_DISTANCE_SLIDER),
        )
        DistanceTickLabels(minKm = minKm, maxKm = maxKm)
    }
}

@Composable
private fun DistanceTickLabels(minKm: Int, maxKm: Int, modifier: Modifier = Modifier) {
    val labels = DistanceTicksKm.map { mokoString(SharedRes.strings.round_trip_wizard_distance_value, it) }
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val width = constraints.maxWidth
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                // Each label sits under its value on the track, the end ones are kept inside the row
                val fraction = (DistanceTicksKm[index] - minKm).toFloat() / (maxKm - minKm)
                val x = (fraction * width - placeable.width / 2f).roundToInt()
                placeable.placeRelative(x.coerceIn(0, width - placeable.width), 0)
            }
        }
    }
}

@Preview
@Composable
private fun RoundTripWizardContentPreview() {
    HuKiTheme {
        RoutePlannerContent(
            uiState = previewRoundTripWizardState(),
            detent = RoutePlannerDetent.EXPANDED,
            onEvent = {},
        )
    }
}

@Preview(locale = "hu")
@Composable
private fun RoundTripWizardContentStartActionsPreview() {
    HuKiTheme {
        val uiState = previewRoundTripWizardState()
        RoutePlannerContent(
            uiState = uiState.copy(selectedStopId = uiState.stops.first().id),
            detent = RoutePlannerDetent.EXPANDED,
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun RoundTripWizardContentMinimizedPreview() {
    HuKiTheme {
        RoutePlannerContent(
            uiState = previewRoundTripWizardState(),
            detent = RoutePlannerDetent.MINIMIZED,
            onEvent = {},
        )
    }
}

private fun previewRoundTripWizardState(): RoutePlannerUiState {
    val planner = PreviewRoutePlanner.uiState()
    return planner.copy(
        waypoints = listOf(planner.stops.first(), RoutePlannerWaypoint()),
        mode = RoutePlannerMode.ROUND_TRIP_WIZARD,
        roundTrip = RoundTripOptions(distanceKm = 9, seed = 7, heading = 90),
    )
}
