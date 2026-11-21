package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.withTimeoutOrNull

private const val SURFACE_ALPHA = 0.5f
private const val REVEAL_FOLLOW_MILLIS = 600L
private val AutoScrollStep = 8.dp
private val LiftedStopElevation = 4.dp
private val LiftedStopShape = RoundedCornerShape(Dimens.Medium)

@Composable
fun RoutePlannerWaypointList(
    uiState: RoutePlannerUiState,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.Large))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SURFACE_ALPHA)),
    ) {
        ReorderableStops(uiState = uiState, rowHeight = rowHeight, onEvent = onEvent)
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
private fun ColumnScope.ReorderableStops(
    uiState: RoutePlannerUiState,
    rowHeight: Dp,
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
    // How far the list scrolled under the dragged row, which moves its slot but not the row under the finger
    var dragScroll by remember { mutableFloatStateOf(0f) }
    val stopIds = remember(stops) { stops.map { it.id } }
    // The drag gesture outlives reorders that keep its row's index, so it reads the order when a drag starts
    val currentStopIds by rememberUpdatedState(stopIds)
    // A drag belongs to the order it started on, so the reordered list never shows its offsets for a frame
    val activeDrag = drag?.takeIf { it.stopIds == stopIds }
    var movedStopId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(stopIds) { drag = null }
    StopsIntoView(
        listState = listState,
        stopIds = stopIds,
        selectedStopId = uiState.selectedStop?.id,
        movedStopId = movedStopId,
        onMovedStopShown = { movedStopId = null },
    )
    DragAutoScroll(
        listState = listState,
        drag = activeDrag,
        fingerOffset = { dragOffset - dragScroll },
        onScrolled = { consumed ->
            dragScroll += consumed
            dragOffset += consumed
        },
    )
    val dropIndex by remember(activeDrag, movableIndices) {
        derivedStateOf {
            activeDrag?.let { listState.dropIndex(it, dragOffset - dragScroll, movableIndices) }
        }
    }
    val moveUpLabel = mokoString(SharedRes.strings.route_planner_a11y_move_up)
    val moveDownLabel = mokoString(SharedRes.strings.route_planner_a11y_move_down)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            // Fills only what the stops need, so the max stops row stays under the last stop
            .weight(weight = 1f, fill = false)
            .testTag(TestTags.ROUTE_PLANNER_WAYPOINT_LIST),
    ) {
        itemsIndexed(stops, key = { _, stop -> stop.id }) { index, _ ->
            val isDragged = activeDrag?.index == index
            // The dragged row stays composed while autoscroll moves its slot out of the list's viewport
            val pinnableContainer = LocalPinnableContainer.current
            DisposableEffect(isDragged) {
                val handle = if (isDragged) pinnableContainer?.pin() else null
                onDispose { handle?.release() }
            }
            val makeRoomOffset by animateFloatAsState(
                targetValue = makeRoomOffset(index, activeDrag, dropIndex),
                label = "stopMakeRoomOffset",
            )
            val lift by animateFloatAsState(targetValue = if (isDragged) 1f else 0f, label = "stopLift")
            // A pinned row whose slot scrolled away is placed next to the visible rows instead of in its slot,
            // so the dragged row is drawn from where it is placed
            var placedTop by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .zIndex(if (isDragged || lift > 0f) 1f else 0f)
                    .onPlaced { placedTop = it.positionInParent().y }
                    .graphicsLayer {
                        translationY = when {
                            activeDrag == null -> 0f
                            isDragged -> activeDrag.startOffset + dragOffset - dragScroll - placedTop
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
                                dragScroll = 0f
                                drag = listState.startDrag(index, currentStopIds)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y
                            },
                            onDragEnd = {
                                val endedDrag = drag ?: return@detectDragGesturesAfterLongPress
                                val fingerOffset = dragOffset - dragScroll
                                val target = listState.dropIndex(endedDrag, fingerOffset, movableIndices)
                                coroutineScope.launch {
                                    // The row settles into its new slot before the list reorders under it
                                    val dropOffset = listState.dropOffset(endedDrag, dragScroll, target)
                                    animate(dragOffset, dropOffset) { value, _ ->
                                        dragOffset = value
                                    }
                                    if (target != index) {
                                        movedStopId = endedDrag.stopIds[index]
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

@Composable
private fun StopsIntoView(
    listState: LazyListState,
    stopIds: List<String>,
    selectedStopId: String?,
    movedStopId: String?,
    onMovedStopShown: () -> Unit,
) {
    var previousStopIds by remember { mutableStateOf(stopIds) }
    val currentStopIds by rememberUpdatedState(stopIds)
    LaunchedEffect(stopIds) {
        // A stop added below the visible rows, or dropped at the list's edge, would land out of sight
        val shownId = stopIds.firstOrNull { it !in previousStopIds } ?: movedStopId?.takeIf { it in stopIds }
        previousStopIds = stopIds
        onMovedStopShown()
        shownId?.let { listState.followIntoView(it, stopIds.indexOf(it)) }
    }
    // A stop selected on the map can sit outside the scrolled list
    LaunchedEffect(selectedStopId) {
        selectedStopId?.let { listState.followIntoView(it, currentStopIds.indexOf(it)) }
    }
}

@Composable
private fun DragAutoScroll(
    listState: LazyListState,
    drag: StopDrag?,
    fingerOffset: () -> Float,
    onScrolled: (Float) -> Unit,
) {
    val step = with(LocalDensity.current) { AutoScrollStep.toPx() }
    LaunchedEffect(drag) {
        val currentDrag = drag ?: return@LaunchedEffect
        while (true) {
            withFrameNanos { }
            val delta = listState.autoScrollDelta(currentDrag, fingerOffset(), step)
            if (delta != 0f) onScrolled(listState.scrollBy(delta))
        }
    }
}

/**
 * A stop being dragged to a new place in the order of [stopIds], from its slot at [startOffset] in the list.
 */
private data class StopDrag(
    val index: Int,
    val size: Int,
    val startOffset: Int,
    val stopIds: List<String>,
)

private fun LazyListState.startDrag(index: Int, stopIds: List<String>): StopDrag {
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    return StopDrag(index = index, size = item?.size ?: 0, startOffset = item?.offset ?: 0, stopIds = stopIds)
}

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

/**
 * Scrolls the list while the dragged row, [fingerOffset] away from where it started, presses on its edge.
 */
private fun LazyListState.autoScrollDelta(drag: StopDrag, fingerOffset: Float, step: Float): Float {
    val top = drag.startOffset + fingerOffset
    val bottom = top + drag.size
    val edge = drag.size / 2f
    return when {
        top < layoutInfo.viewportStartOffset + edge && canScrollBackward -> -step
        bottom > layoutInfo.viewportEndOffset - edge && canScrollForward -> step
        else -> 0f
    }
}

/**
 * The stop whose row the dragged row's center is over.
 */
private fun LazyListState.dropIndex(drag: StopDrag, fingerOffset: Float, movableIndices: IntRange): Int {
    val items = layoutInfo.visibleItemsInfo
    if (items.isEmpty()) return drag.index
    val center = drag.startOffset + drag.size / 2f + fingerOffset
    val target = items.firstOrNull { center < it.offset + it.size }?.index ?: items.last().index
    return target.coerceIn(movableIndices)
}

/**
 * The drag offset that lands the dragged row, [scrolled] away from its slot, in [target]'s place once the rows in
 * between stepped aside.
 */
private fun LazyListState.dropOffset(drag: StopDrag, scrolled: Float, target: Int): Float {
    val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == target }
    val slotTop = drag.startOffset - scrolled
    val targetTop = when {
        targetItem == null -> slotTop
        target > drag.index -> (targetItem.offset + targetItem.size - drag.size).toFloat()
        target < drag.index -> targetItem.offset.toFloat()
        else -> slotTop
    }
    return targetTop - drag.startOffset + scrolled
}

/**
 * Scrolls the stop with [key] at [index] into view, and keeps it there while its card opens.
 */
private suspend fun LazyListState.followIntoView(key: String, index: Int) {
    if (index < 0) return
    withTimeoutOrNull(REVEAL_FOLLOW_MILLIS) {
        snapshotFlow { layoutInfo }.collect { info ->
            val item = info.visibleItemsInfo.firstOrNull { it.key == key }
            if (item == null) {
                animateScrollToItem(index)
                return@collect
            }
            val overTop = info.viewportStartOffset - item.offset
            val overBottom = item.offset + item.size - info.viewportEndOffset
            when {
                overTop > 0 -> scrollBy(-overTop.toFloat())
                overBottom > 0 -> scrollBy(minOf(overBottom, -overTop).toFloat())
            }
        }
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
    dragHandleModifier: Modifier = Modifier,
    keepsDragHandleSpace: Boolean = false,
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
            trailing = dragHandle,
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
            trailing = dragHandle,
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
            onEvent = {},
        )
    }
}

@Preview(heightDp = 300)
@Composable
private fun RoutePlannerWaypointListManyStopsPreview() {
    HuKiTheme {
        RoutePlannerWaypointList(
            uiState = PreviewRoutePlanner.uiState(stops = PreviewRoutePlanner.manyStops(8)),
            onEvent = {},
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
    trailing: (@Composable () -> Unit)? = null,
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
        trailing = trailing,
        rowHeight = rowHeight,
        modifier = modifier,
    ) {
        RoutePlannerRowIcon(imageVector = mokoImage(waypointType.icon), tint = Color.Unspecified)
    }
}
