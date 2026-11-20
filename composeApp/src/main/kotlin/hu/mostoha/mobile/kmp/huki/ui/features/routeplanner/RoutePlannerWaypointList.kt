package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiState
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlinx.coroutines.launch

private const val MAX_DEFAULT_STOP_COUNT = 3
private const val SURFACE_ALPHA = 0.5f
private val LiftedStopElevation = 4.dp
private val LiftedStopShape = RoundedCornerShape(Dimens.Medium)

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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SURFACE_ALPHA)),
    ) {
        if (isCollapsed) {
            Column(modifier = Modifier.padding(horizontal = Dimens.MediumLarge)) {
                CollapsedStops(
                    uiState = uiState,
                    rowHeight = rowHeight,
                    onEvent = onEvent,
                    onMoreStopsClick = onMoreStopsClick,
                )
            }
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
                maxStopCount = RoutePlanConstants.MAX_WAYPOINT_COUNT,
                rowHeight = rowHeight,
                modifier = Modifier.padding(horizontal = Dimens.MediumLarge),
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
    StopItem(uiState = uiState, index = 0, rowHeight = rowHeight, isReorderable = false, onEvent = onEvent)
    RoutePlannerMoreStopsRow(
        hiddenStopCount = stops.size - 2,
        onClick = onMoreStopsClick,
        rowHeight = rowHeight,
    )
    StopItem(
        uiState = uiState,
        index = stops.lastIndex,
        rowHeight = rowHeight,
        isReorderable = false,
        onEvent = onEvent,
    )
}

@Composable
private fun ColumnScope.ReorderableStops(
    uiState: RoutePlannerUiState,
    rowHeight: Dp,
    isFullScreen: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
) {
    val stops = uiState.stops
    // A round trip's last stop mirrors its start, so neither end takes part in reordering
    val movableIndices = remember(stops.size, uiState.isRoundTrip) {
        if (uiState.isRoundTrip) 1 until stops.lastIndex else stops.indices
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var drag by remember { mutableStateOf<StopDrag?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val stopIds = remember(stops) { stops.map { it.id } }
    // The drag gesture outlives reorders that keep its row's index, so it reads the order when a drag starts
    val currentStopIds by rememberUpdatedState(stopIds)
    // A drag belongs to the order it started on, so the reordered list never shows its offsets for a frame
    val activeDrag = drag?.takeIf { it.stopIds == stopIds }
    LaunchedEffect(stopIds) { drag = null }
    val dropIndex by remember(activeDrag, movableIndices) {
        derivedStateOf { activeDrag?.let { listState.dropIndex(it.index, dragOffset, movableIndices) } }
    }
    val moveUpLabel = mokoString(SharedRes.strings.route_planner_a11y_move_up)
    val moveDownLabel = mokoString(SharedRes.strings.route_planner_a11y_move_down)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFullScreen) {
                    // Fills only what a long list needs, so the max stops row stays under the last stop.
                    Modifier.weight(weight = 1f, fill = false)
                } else {
                    Modifier.heightIn(max = Dimens.RoutePlannerListMaxHeight)
                },
            )
            .testTag(TestTags.ROUTE_PLANNER_WAYPOINT_LIST),
    ) {
        itemsIndexed(stops, key = { _, stop -> stop.id }) { index, _ ->
            val isDragged = activeDrag?.index == index
            val makeRoomOffset by animateFloatAsState(
                targetValue = makeRoomOffset(index, activeDrag, dropIndex),
                label = "stopMakeRoomOffset",
            )
            val lift by animateFloatAsState(targetValue = if (isDragged) 1f else 0f, label = "stopLift")
            Box(
                modifier = Modifier
                    .zIndex(if (isDragged || lift > 0f) 1f else 0f)
                    .graphicsLayer {
                        translationY = when {
                            activeDrag == null -> 0f
                            isDragged -> dragOffset
                            else -> makeRoomOffset
                        }
                    }
                    .semantics {
                        customActions = buildList {
                            if (index in movableIndices && index - 1 in movableIndices) {
                                add(
                                    CustomAccessibilityAction(moveUpLabel) {
                                        onEvent(RoutePlannerUiEvents.WaypointMoved(index, index - 1))
                                        true
                                    },
                                )
                            }
                            if (index in movableIndices && index + 1 in movableIndices) {
                                add(
                                    CustomAccessibilityAction(moveDownLabel) {
                                        onEvent(RoutePlannerUiEvents.WaypointMoved(index, index + 1))
                                        true
                                    },
                                )
                            }
                        }
                    },
            ) {
                LiftedStopBackground(lift = { lift }, modifier = Modifier.matchParentSize())
                StopItem(
                    uiState = uiState,
                    index = index,
                    rowHeight = rowHeight,
                    isReorderable = index in movableIndices,
                    keepsDragHandleSpace = true,
                    onEvent = onEvent,
                    dragHandleModifier = Modifier.pointerInput(index, stops.size) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                dragOffset = 0f
                                drag = StopDrag(
                                    index = index,
                                    size = listState.itemSize(index),
                                    stopIds = currentStopIds,
                                )
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y
                            },
                            onDragEnd = {
                                val target = listState.dropIndex(index, dragOffset, movableIndices)
                                coroutineScope.launch {
                                    // The row settles into its new slot before the list reorders under it
                                    animate(dragOffset, listState.slotOffset(index, target)) { value, _ ->
                                        dragOffset = value
                                    }
                                    if (target != index) {
                                        onEvent(RoutePlannerUiEvents.WaypointMoved(index, target))
                                    } else {
                                        drag = null
                                    }
                                }
                            },
                            onDragCancel = { drag = null },
                        )
                    },
                    // The stop card reaches past the row into this padding
                    modifier = Modifier.padding(horizontal = Dimens.MediumLarge),
                )
            }
        }
    }
}

@Composable
private fun LiftedStopBackground(lift: () -> Float, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surface
    Box(
        modifier = modifier
            // Inset from the clipping list background, so the lifted row's shadow shows on every side
            .padding(horizontal = Dimens.Small, vertical = Dimens.ExtraSmall)
            .graphicsLayer {
                val fraction = lift()
                alpha = fraction
                shadowElevation = LiftedStopElevation.toPx() * fraction
                shape = LiftedStopShape
                clip = true
            }
            .background(color),
    )
}

/**
 * A stop being dragged to a new place in the order of [stopIds].
 */
private data class StopDrag(
    val index: Int,
    val size: Int,
    val stopIds: List<String>,
)

/**
 * The rows between the dragged stop and its drop place step aside by the dragged row's height.
 */
private fun makeRoomOffset(index: Int, drag: StopDrag?, dropIndex: Int?): Float =
    when {
        drag == null || dropIndex == null || index == drag.index -> 0f
        index in (drag.index + 1)..dropIndex -> -drag.size.toFloat()
        index in dropIndex until drag.index -> drag.size.toFloat()
        else -> 0f
    }

private fun LazyListState.itemSize(index: Int): Int =
    layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.size ?: 0

/**
 * The stop whose row the dragged row's center is over.
 */
private fun LazyListState.dropIndex(index: Int, dragOffset: Float, movableIndices: IntRange): Int {
    val items = layoutInfo.visibleItemsInfo
    val dragged = items.firstOrNull { it.index == index } ?: return index
    val center = dragged.offset + dragged.size / 2f + dragOffset
    val target = items.firstOrNull { center < it.offset + it.size }?.index ?: items.last().index
    return target.coerceIn(movableIndices)
}

/**
 * How far the row at [index] moves to land in [target]'s place once the rows in between stepped aside.
 */
private fun LazyListState.slotOffset(index: Int, target: Int): Float {
    val items = layoutInfo.visibleItemsInfo
    val dragged = items.firstOrNull { it.index == index } ?: return 0f
    val targetItem = items.firstOrNull { it.index == target } ?: return 0f
    return when {
        target > index -> (targetItem.offset + targetItem.size - dragged.offset - dragged.size).toFloat()
        target < index -> (targetItem.offset - dragged.offset).toFloat()
        else -> 0f
    }
}

@Composable
private fun StopItem(
    uiState: RoutePlannerUiState,
    index: Int,
    rowHeight: Dp,
    isReorderable: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    modifier: Modifier = Modifier,
    keepsDragHandleSpace: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
) {
    val stop = uiState.stops[index]
    val hasConnectorBelow = uiState.hasConnectorBelow(index)
    val isExpanded = uiState.isStopExpanded(stop)
    val dragHandle: (@Composable () -> Unit)? = when {
        isReorderable -> {
            { RoutePlannerDragHandle(rowHeight = rowHeight, modifier = dragHandleModifier) }
        }
        // A fixed stop among draggable ones keeps its remove button in line with theirs
        keepsDragHandleSpace -> {
            { Spacer(modifier = Modifier.width(Dimens.RoutePlannerStopActionSize)) }
        }
        else -> null
    }
    if (stop.isEmpty) {
        RoutePlannerStopCard(
            title = mokoString(SharedRes.strings.route_planner_add_stop_title),
            hasConnectorAbove = index > 0,
            hasConnectorBelow = hasConnectorBelow,
            isRemovable = uiState.isEmptyStopRemovable,
            actions = listOf(
                RoutePlannerStopCardAction(
                    iconResId = R.drawable.ic_recenter,
                    title = mokoString(SharedRes.strings.route_planner_empty_stop_pick_on_map),
                    testTag = TestTags.routePlannerEmptyStopPickOnMap(index),
                    onClick = { onEvent(RoutePlannerUiEvents.PickOnMapClicked(stop.id)) },
                ),
                RoutePlannerStopCardAction(
                    iconResId = R.drawable.ic_search,
                    title = mokoString(SharedRes.strings.route_planner_empty_stop_search),
                    testTag = TestTags.routePlannerEmptyStopRow(index),
                    onClick = { onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked(stop.id)) },
                ),
            ),
            onRemoveClick = { onEvent(RoutePlannerUiEvents.WaypointRemoved(stop.id)) },
            isExpanded = isExpanded,
            isTitleHighlighted = true,
            // The search action takes over the row's tag once the card opens
            titleTestTag = if (isExpanded) null else TestTags.routePlannerEmptyStopRow(index),
            onTitleClickLabel = mokoString(SharedRes.strings.route_planner_a11y_add_stop),
            onTitleClick = { onEvent(RoutePlannerUiEvents.StopClicked(stop.id)) },
            rowHeight = rowHeight,
            collapsedTrailing = dragHandle,
            modifier = modifier,
        ) {
            RoutePlannerEmptyStopIcon()
        }
    } else {
        RoutePlannerSelectedStopCard(
            stop = stop,
            waypointType = uiState.stopType(index),
            hasConnectorAbove = index > 0,
            hasConnectorBelow = hasConnectorBelow,
            onEvent = onEvent,
            isExpanded = isExpanded,
            collapsedTrailing = dragHandle,
            rowHeight = rowHeight,
            modifier = modifier.then(
                if (isExpanded) Modifier else Modifier.testTag(TestTags.routePlannerStopRow(index)),
            ),
        )
    }
}

@Composable
private fun RoutePlannerDragHandle(rowHeight: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(Dimens.RoutePlannerStopActionSize)
            .height(rowHeight),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_drag_handle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.IconMedium),
        )
    }
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

@Composable
fun RoutePlannerSelectedStopCard(
    stop: RoutePlannerWaypoint,
    waypointType: WaypointType,
    hasConnectorAbove: Boolean,
    hasConnectorBelow: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    collapsedTrailing: (@Composable () -> Unit)? = null,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
) {
    RoutePlannerStopCard(
        title = stop.displayName?.let { mokoString(it) } ?: mokoString(SharedRes.strings.route_planner_waypoint_empty),
        hasConnectorAbove = hasConnectorAbove,
        hasConnectorBelow = hasConnectorBelow,
        isRemovable = true,
        actions = listOf(
            RoutePlannerStopCardAction(
                iconResId = R.drawable.ic_open_with,
                title = mokoString(SharedRes.strings.route_planner_stop_move),
                testTag = TestTags.ROUTE_PLANNER_STOP_MOVE,
                onClick = { onEvent(RoutePlannerUiEvents.StopMoveClicked(stop.id)) },
            ),
            RoutePlannerStopCardAction(
                iconResId = R.drawable.ic_search,
                title = mokoString(SharedRes.strings.route_planner_stop_replace),
                testTag = TestTags.ROUTE_PLANNER_STOP_REPLACE,
                onClick = { onEvent(RoutePlannerUiEvents.StopReplaceClicked(stop.id)) },
            ),
            RoutePlannerStopCardAction(
                iconResId = R.drawable.ic_edit_square,
                title = mokoString(SharedRes.strings.route_planner_stop_rename),
                testTag = TestTags.ROUTE_PLANNER_STOP_RENAME,
                onClick = { onEvent(RoutePlannerUiEvents.StopRenameClicked(stop.id)) },
            ),
        ),
        onTitleClick = { onEvent(RoutePlannerUiEvents.StopClicked(stop.id)) },
        onRemoveClick = { onEvent(RoutePlannerUiEvents.WaypointRemoved(stop.id)) },
        isExpanded = isExpanded,
        collapsedTrailing = collapsedTrailing,
        rowHeight = rowHeight,
        modifier = modifier,
    ) {
        RoutePlannerRowIcon(imageVector = mokoImage(waypointType.icon), tint = Color.Unspecified)
    }
}
