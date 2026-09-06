package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

@Composable
fun RoutePlannerSheet(
    detent: RoutePlannerDetent,
    onDetentChange: (RoutePlannerDetent) -> Unit,
    minimizedHeight: Dp,
    expandedHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable (sheetModifier: Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val state = remember { AnchoredDraggableState(initialValue = detent) }
        val animatedMinimizedHeight by animateDpAsState(minimizedHeight, label = "minimizedHeight")
        val animatedExpandedHeight by animateDpAsState(expandedHeight, label = "expandedHeight")
        val anchors = remember(maxHeight, animatedMinimizedHeight, animatedExpandedHeight, statusBarInset) {
            with(density) {
                DraggableAnchors {
                    RoutePlannerDetent.MINIMIZED at (maxHeight - animatedMinimizedHeight).toPx()
                    RoutePlannerDetent.EXPANDED at (maxHeight - animatedExpandedHeight).toPx()
                    RoutePlannerDetent.FULL_SCREEN at statusBarInset.toPx()
                }
            }
        }
        SideEffect { state.updateAnchors(anchors, state.targetValue) }
        LaunchedEffect(detent) {
            if (state.targetValue != detent) {
                state.animateTo(detent)
            }
        }
        LaunchedEffect(state) {
            snapshotFlow { state.targetValue }
                .distinctUntilChanged()
                .collect { onDetentChange(it) }
        }
        val fullScreenHeight = maxHeight - statusBarInset
        val containerHeightPx = with(density) { maxHeight.toPx() }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = state.requireOffset().roundToInt()) }
                .layout { measurable, constraints ->
                    val height = (containerHeightPx - state.requireOffset())
                        .roundToInt()
                        .coerceIn(0, constraints.maxHeight)
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    layout(placeable.width, height) { placeable.place(0, 0) }
                }
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = Dimens.ExtraLarge, topEnd = Dimens.ExtraLarge),
                ),
        )
        content(
            Modifier
                .offset { IntOffset(x = 0, y = state.requireOffset().roundToInt()) }
                .then(if (detent == RoutePlannerDetent.FULL_SCREEN) Modifier.height(fullScreenHeight) else Modifier)
                .nestedScroll(rememberSheetNestedScrollConnection(state))
                .anchoredDraggable(
                    state = state,
                    reverseDirection = false,
                    orientation = Orientation.Vertical,
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(state),
                ),
        )
    }
}

@Composable
private fun rememberSheetNestedScrollConnection(
    state: AnchoredDraggableState<RoutePlannerDetent>,
): NestedScrollConnection {
    return remember(state) {
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
                state.settle(AnchoredDraggableDefaults.SnapAnimationSpec)
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                state.settle(AnchoredDraggableDefaults.SnapAnimationSpec)
                return available
            }
        }
    }
}
