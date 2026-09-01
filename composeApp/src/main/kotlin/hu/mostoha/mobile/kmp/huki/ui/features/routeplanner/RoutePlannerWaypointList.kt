package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiState
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlin.math.roundToInt

private const val MAX_DEFAULT_STOP_COUNT = 3
private const val SURFACE_ALPHA = 0.5f

@Composable
fun RoutePlannerWaypointList(
    uiState: RoutePlannerUiState,
    detent: RoutePlannerDetent,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onMoreStopsClick: () -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
) {
    val stops = uiState.stops
    val isFullScreen = detent == RoutePlannerDetent.FULL_SCREEN
    val isCollapsed = !isFullScreen && stops.size > MAX_DEFAULT_STOP_COUNT
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.Large))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SURFACE_ALPHA))
            .padding(horizontal = Dimens.MediumLarge),
    ) {
        if (isCollapsed) {
            CollapsedStops(
                uiState = uiState,
                rowHeight = rowHeight,
                onEvent = onEvent,
                onMoreStopsClick = onMoreStopsClick,
            )
        } else {
            ReorderableStops(
                uiState = uiState,
                rowHeight = rowHeight,
                isFullScreen = isFullScreen,
                onEvent = onEvent,
            )
        }
        if (uiState.isMaxStopsReached) {
            RoutePlannerMaxStopsRow(
                maxStopCount = RoutePlannerUiState.MAX_WAYPOINT_COUNT,
                rowHeight = rowHeight,
            )
        } else {
            RoutePlannerAddStopRow(
                isRoundTripEnabled = uiState.isRoundTripEnabled,
                onAddStopClick = { onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked()) },
                onRoundTripClick = { onEvent(RoutePlannerUiEvents.RoundTripClicked) },
                rowHeight = rowHeight,
            )
        }
    }
}

@Composable
private fun CollapsedStops(
    uiState: RoutePlannerUiState,
    rowHeight: Dp,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onMoreStopsClick: () -> Unit,
) {
    val stops = uiState.stops
    StopRow(uiState = uiState, index = 0, rowHeight = rowHeight, isReorderable = false, onEvent = onEvent)
    RoutePlannerMoreStopsRow(
        hiddenStopCount = stops.size - 2,
        onClick = onMoreStopsClick,
        rowHeight = rowHeight,
    )
    StopRow(uiState = uiState, index = stops.lastIndex, rowHeight = rowHeight, isReorderable = false, onEvent = onEvent)
}

@Composable
private fun ColumnScope.ReorderableStops(
    uiState: RoutePlannerUiState,
    rowHeight: Dp,
    isFullScreen: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
) {
    val stops = uiState.stops
    val density = LocalDensity.current
    val rowHeightPx = with(density) { rowHeight.toPx() }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val draggedRowShape = RoundedCornerShape(Dimens.Medium)
    val draggedRowElevation = 6.dp
    val moveUpLabel = mokoString(SharedRes.strings.route_planner_a11y_move_up)
    val moveDownLabel = mokoString(SharedRes.strings.route_planner_a11y_move_down)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFullScreen) {
                    // Fills only what a long list needs, so the add stop row stays under the last stop.
                    Modifier.weight(weight = 1f, fill = false)
                } else {
                    Modifier.heightIn(max = Dimens.RoutePlannerListMaxHeight)
                },
            )
            .testTag(TestTags.ROUTE_PLANNER_WAYPOINT_LIST),
    ) {
        itemsIndexed(stops, key = { _, stop -> stop.id }) { index, _ ->
            val isDragged = draggingIndex == index
            RoutePlannerWaypointRow(
                waypoint = stops[index],
                waypointType = uiState.stopType(index),
                hasConnectorAbove = index > 0,
                rowHeight = rowHeight,
                emptyRowTestTag = TestTags.routePlannerEmptyStopRow(index),
                onRemoveClick = { onEvent(RoutePlannerUiEvents.WaypointRemoved(stops[index].id)) },
                onEmptyRowClick = {
                    onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked(stops[index].id))
                },
                dragHandleModifier = Modifier.pointerInput(index, stops.size) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            draggingIndex = index
                            dragOffset = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount.y
                        },
                        onDragEnd = {
                            val target = (index + (dragOffset / rowHeightPx).roundToInt())
                                .coerceIn(0, stops.lastIndex)
                            if (target != index) {
                                onEvent(RoutePlannerUiEvents.WaypointMoved(index, target))
                            }
                            draggingIndex = null
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            draggingIndex = null
                            dragOffset = 0f
                        },
                    )
                },
                modifier = Modifier
                    .zIndex(if (isDragged) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragged) dragOffset else 0f
                        shadowElevation = if (isDragged) draggedRowElevation.toPx() else 0f
                        shape = draggedRowShape
                        clip = isDragged
                    }
                    .background(
                        color = if (isDragged) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shape = draggedRowShape,
                    )
                    .semantics {
                        customActions = buildList {
                            if (index > 0) {
                                add(
                                    CustomAccessibilityAction(moveUpLabel) {
                                        onEvent(RoutePlannerUiEvents.WaypointMoved(index, index - 1))
                                        true
                                    },
                                )
                            }
                            if (index < stops.lastIndex) {
                                add(
                                    CustomAccessibilityAction(moveDownLabel) {
                                        onEvent(RoutePlannerUiEvents.WaypointMoved(index, index + 1))
                                        true
                                    },
                                )
                            }
                        }
                    },
            )
        }
    }
}

@Composable
private fun StopRow(
    uiState: RoutePlannerUiState,
    index: Int,
    rowHeight: Dp,
    isReorderable: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
) {
    val stop = uiState.stops[index]
    RoutePlannerWaypointRow(
        waypoint = stop,
        waypointType = uiState.stopType(index),
        hasConnectorAbove = index > 0,
        rowHeight = rowHeight,
        isReorderable = isReorderable,
        emptyRowTestTag = TestTags.routePlannerEmptyStopRow(index),
        onRemoveClick = { onEvent(RoutePlannerUiEvents.WaypointRemoved(stop.id)) },
        onEmptyRowClick = { onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked(stop.id)) },
    )
}

@Preview
@Composable
private fun RoutePlannerWaypointListPreview() {
    HuKiTheme {
        RoutePlannerWaypointList(
            uiState = PreviewRoutePlanner.uiState(),
            detent = RoutePlannerDetent.EXPANDED,
            onEvent = {},
            onMoreStopsClick = {},
        )
    }
}

@Preview
@Composable
private fun RoutePlannerWaypointListCollapsedPreview() {
    HuKiTheme {
        RoutePlannerWaypointList(
            uiState = PreviewRoutePlanner.uiState(stops = PreviewRoutePlanner.manyStops(4)),
            detent = RoutePlannerDetent.EXPANDED,
            onEvent = {},
            onMoreStopsClick = {},
        )
    }
}

@Preview
@Composable
private fun RoutePlannerWaypointListExpandedPreview() {
    HuKiTheme {
        RoutePlannerWaypointList(
            uiState = PreviewRoutePlanner.uiState(stops = PreviewRoutePlanner.manyStops(4)),
            detent = RoutePlannerDetent.FULL_SCREEN,
            onEvent = {},
            rowHeight = Dimens.RoutePlannerRowHeightExpanded,
            onMoreStopsClick = {},
        )
    }
}
