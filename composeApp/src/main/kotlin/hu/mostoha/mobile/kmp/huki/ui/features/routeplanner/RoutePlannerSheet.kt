package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.roundToInt

/**
 * The tallest the expanded sheet grows before its stop list scrolls: a strip of the map stays visible above it,
 * except in landscape where the height is scarce.
 */
fun routePlannerMaxExpandedHeight(containerHeight: Dp, statusBarInset: Dp, isLandscape: Boolean): Dp =
    containerHeight - statusBarInset - if (isLandscape) 0.dp else Dimens.RoutePlannerMapPeekHeight

private val SheetShape = RoundedCornerShape(topStart = Dimens.ExtraLarge, topEnd = Dimens.ExtraLarge)

@Composable
fun RoutePlannerSheet(
    detent: RoutePlannerDetent,
    onDetentChange: (RoutePlannerDetent) -> Unit,
    minimizedHeight: Dp,
    expandedHeight: Dp,
    maxExpandedHeight: Dp,
    modifier: Modifier = Modifier,
    aboveSheet: @Composable () -> Unit = {},
    content: @Composable (sheetModifier: Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val state = remember { AnchoredDraggableState(initialValue = detent) }
        val animatedMinimizedHeight by animateDpAsState(minimizedHeight, label = "minimizedHeight")
        val anchors = remember(maxHeight, animatedMinimizedHeight, expandedHeight) {
            with(density) {
                DraggableAnchors {
                    RoutePlannerDetent.MINIMIZED at (maxHeight - animatedMinimizedHeight).toPx()
                    RoutePlannerDetent.EXPANDED at (maxHeight - expandedHeight).toPx()
                }
            }
        }
        val interaction = remember { SheetInteraction() }
        // Anchors that move while a drag or its release settle holds the state leave it stuck on a stale target,
        // which then ignores every later detent change, so new anchors wait until the sheet is let go
        SideEffect {
            if (!interaction.isActive) state.updateAnchors(anchors, state.targetValue)
        }
        LaunchedEffect(detent) {
            if (state.targetValue != detent) {
                state.animateTo(detent)
            }
        }
        LaunchedEffect(state) {
            // The content keeps its layout under the finger and switches once the sheet is let go
            snapshotFlow { state.targetValue.takeUnless { interaction.isFingerDown } }
                .filterNotNull()
                .distinctUntilChanged()
                .collect { onDetentChange(it) }
        }
        val baseFlingBehavior = AnchoredDraggableDefaults.flingBehavior(state)
        val flingBehavior = remember(baseFlingBehavior, interaction) {
            object : FlingBehavior {
                override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                    val scrollScope = this
                    return interaction.settling {
                        with(baseFlingBehavior) { scrollScope.performFling(initialVelocity) }
                    }
                }
            }
        }
        val sheetTop = remember { SheetTop() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        Box(
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(placeable.width, placeable.height) {
                    placeable.place(x = 0, y = sheetTop.px.roundToInt() - placeable.height)
                }
            },
        ) {
            aboveSheet()
        }
        content(
            Modifier
                .sheetContentLayout(
                    detent = detent,
                    state = state,
                    interaction = interaction,
                    sheetTop = sheetTop,
                    containerHeightPx = containerHeightPx,
                )
                .heightIn(max = maxExpandedHeight)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            interaction.isFingerDown = event.changes.any { it.pressed }
                        }
                    }
                }
                .nestedScroll(rememberSheetNestedScrollConnection(state, interaction))
                .anchoredDraggable(
                    state = state,
                    reverseDirection = false,
                    orientation = Orientation.Vertical,
                    flingBehavior = flingBehavior,
                ),
        )
        Spacer(
            modifier = Modifier
                // Drawn under the content, but measured after it so it reads the anchor the content just updated
                .zIndex(-1f)
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = sheetTop.px.roundToInt()) }
                .layout { measurable, constraints ->
                    val height = (containerHeightPx - sheetTop.px)
                        .roundToInt()
                        .coerceIn(0, constraints.maxHeight)
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    layout(placeable.width, height) { placeable.place(0, 0) }
                }
                .shadow(elevation = Dimens.Small, shape = SheetShape)
                .background(color = MaterialTheme.colorScheme.surface, shape = SheetShape),
        )
    }
}

/**
 * Places the content at the sheet's top edge and keeps the expanded anchor on its height.
 */
private fun Modifier.sheetContentLayout(
    detent: RoutePlannerDetent,
    state: AnchoredDraggableState<RoutePlannerDetent>,
    interaction: SheetInteraction,
    sheetTop: SheetTop,
    containerHeightPx: Float,
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        // The content animates its own height, so the expanded anchor follows it within the same frame
        if (detent == RoutePlannerDetent.EXPANDED && !state.isAnimationRunning && !interaction.isActive) {
            state.updateExpandedAnchor(containerHeightPx - placeable.height)
        }
        // Between the minimized and expanded sheet the content animates its own height, so the sheet
        // keeps its bottom edge and follows it instead of sliding a content that already changed
        sheetTop.px = if (!interaction.isActive) {
            sheetTop.followContent(contentTop = containerHeightPx - placeable.height, contentHeight = placeable.height)
        } else {
            state.requireOffset().also { sheetTop.holdAt(it) }
        }
        layout(placeable.width, placeable.height) {
            placeable.place(x = 0, y = sheetTop.px.roundToInt())
        }
    }

/**
 * Where the content placed the sheet's top edge in this layout pass, for the background and the views above it.
 */
private class SheetTop {
    var px by mutableFloatStateOf(0f)
    private var heldPx: Float? = null
    private var lastContentHeight = 0

    /**
     * Keeps the top where a drag or its settle left it, until the content following it catches up.
     */
    fun holdAt(top: Float) {
        heldPx = top
    }

    /**
     * A settle can end while the content still resizes towards the sheet, e.g. its stops revealing or folding,
     * and following it right away would move the sheet back, so the top holds while the content catches up.
     */
    fun followContent(contentTop: Float, contentHeight: Int): Float {
        val held = heldPx
        val isCatchingUp = held != null &&
            (
                contentTop > held &&
                    contentHeight > lastContentHeight ||
                    contentTop < held &&
                    contentHeight < lastContentHeight
                )
        lastContentHeight = contentHeight
        if (isCatchingUp) return held
        heldPx = null
        return contentTop
    }
}

/**
 * Whether a finger or the settle after it lets go is moving the sheet.
 */
private class SheetInteraction {
    var isFingerDown by mutableStateOf(false)
    private var settleCount by mutableIntStateOf(0)

    val isActive: Boolean
        get() = isFingerDown || settleCount > 0

    suspend fun <T> settling(block: suspend () -> T): T {
        settleCount++
        try {
            return block()
        } finally {
            settleCount--
        }
    }
}

private fun AnchoredDraggableState<RoutePlannerDetent>.updateExpandedAnchor(position: Float) {
    val current = anchors.positionOf(RoutePlannerDetent.EXPANDED)
    if (current.isNaN() || current == position) return
    val newAnchors = DraggableAnchors {
        RoutePlannerDetent.MINIMIZED at anchors.positionOf(RoutePlannerDetent.MINIMIZED)
        RoutePlannerDetent.EXPANDED at position
    }
    updateAnchors(newAnchors, targetValue)
}

@Composable
private fun rememberSheetNestedScrollConnection(
    state: AnchoredDraggableState<RoutePlannerDetent>,
    interaction: SheetInteraction,
): NestedScrollConnection {
    return remember(state, interaction) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                return if (delta < 0 && source == NestedScrollSource.UserInput) {
                    Offset(x = 0f, y = state.dispatchRawDelta(delta))
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (source == NestedScrollSource.UserInput) {
                    Offset(x = 0f, y = state.dispatchRawDelta(available.y))
                } else {
                    Offset.Zero
                }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val isSheetDragged = !state.offset.isNaN() && state.offset > state.anchors.minPosition()
                if (available.y >= 0 || !isSheetDragged) {
                    return Velocity.Zero
                }
                interaction.settling { state.settle(AnchoredDraggableDefaults.SnapAnimationSpec) }
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                interaction.settling { state.settle(AnchoredDraggableDefaults.SnapAnimationSpec) }
                return available
            }
        }
    }
}
