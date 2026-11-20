package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiState
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlanInfoViewData
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.ui.components.PoweredByAttribution
import hu.mostoha.mobile.kmp.huki.ui.components.PrimaryButton
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlin.math.roundToInt

@Composable
fun RoutePlannerContent(
    uiState: RoutePlannerUiState,
    detent: RoutePlannerDetent,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onMoreStopsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDetentChange: (RoutePlannerDetent) -> Unit = {},
    onMinimizedHeightMeasured: (Dp) -> Unit = {},
    onExpandedHeightMeasured: (Dp) -> Unit = {},
) {
    val density = LocalDensity.current
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isMinimized = detent == RoutePlannerDetent.MINIMIZED
    val stopListAlpha by animateFloatAsState(
        targetValue = if (uiState.isRoutePlanLoading) 0f else 1f,
        label = "stopListAlpha",
    )
    val isFullScreen = detent == RoutePlannerDetent.FULL_SCREEN
    val rowHeight = if (isFullScreen) Dimens.RoutePlannerRowHeightExpanded else Dimens.RoutePlannerRowHeight
    val plannerFraction = rememberPlannerReveal(isMinimized)
    val isPlannerCollapsed = isMinimized && !plannerFraction.isRunning
    var cardHeight by remember { mutableStateOf(Dp.Unspecified) }
    ReportSettledExpandedHeight(
        cardHeight = cardHeight,
        isExpanded = detent == RoutePlannerDetent.EXPANDED,
        isRevealing = plannerFraction.isRunning,
        onExpandedHeightMeasured = onExpandedHeightMeasured,
    )
    var headerHeight by remember { mutableStateOf(Dp.Unspecified) }
    var footerHeight by remember { mutableStateOf(Dp.Unspecified) }
    var selectedStopHeight by remember { mutableStateOf(0.dp) }
    // A moved stop is placed on the map, which its card would cover
    val isSelectedStopShown = isMinimized && uiState.selectedStop != null && !uiState.isMovingStop
    // The header and footer are shown in every detent, so the peek follows them live, e.g. when the
    // route stats and the save button appear while the sheet is minimized.
    LaunchedEffect(headerHeight, footerHeight, selectedStopHeight, isSelectedStopShown, navBarInset) {
        if (headerHeight == Dp.Unspecified || footerHeight == Dp.Unspecified) return@LaunchedEffect
        val selectedStopPeek = if (isSelectedStopShown) selectedStopHeight + Dimens.MediumLarge else 0.dp
        val padding = Dimens.ExtraSmall + Dimens.Medium + navBarInset + Dimens.Large
        onMinimizedHeightMeasured(headerHeight + footerHeight + selectedStopPeek + padding)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.ROUTE_PLANNER_SHEET)
            .onGloballyPositioned { cardHeight = with(density) { it.size.height.toDp() } },
        shape = RoundedCornerShape(
            topStart = Dimens.ExtraLarge,
            topEnd = Dimens.ExtraLarge,
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .then(if (isFullScreen) Modifier.fillMaxHeight() else Modifier.wrapContentHeight())
                .padding(top = Dimens.ExtraSmall),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { headerHeight = with(density) { it.size.height.toDp() } },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DragHandle(verticalPadding = Dimens.Small)
                RoutePlannerHeader(
                    detent = detent,
                    onDetentClick = { onDetentChange(detent.toggled()) },
                    onCloseClick = { onEvent(RoutePlannerUiEvents.CloseClicked) },
                    onGraphhopperClick = { onEvent(RoutePlannerUiEvents.GraphhopperAttributionClicked) },
                )
            }
            // The sheet follows its content's height, so the planner block's own size animation moves the sheet
            Box(
                modifier = Modifier
                    .then(weightWhen(isFullScreen))
                    .revealed(fraction = { plannerFraction.value })
                    .then(if (isPlannerCollapsed) Modifier.clearAndSetSemantics {} else Modifier),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isFullScreen) Modifier.fillMaxHeight() else Modifier)
                        .padding(
                            start = Dimens.ExtraLarge,
                            top = Dimens.ExtraSmall,
                            end = Dimens.ExtraLarge,
                        ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
                ) {
                    RoutePlannerProfilePicker(
                        selectedProfile = uiState.routeProfile,
                        onProfileSelected = { onEvent(RoutePlannerUiEvents.ProfileSelected(it)) },
                    )
                    AnimatedVisibility(
                        visible = uiState.isStopListVisible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.then(weightWhen(isFullScreen, fill = false)),
                    ) {
                        Box {
                            Column(
                                modifier = Modifier.hiddenWhileLoading(uiState.isRoutePlanLoading, stopListAlpha),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
                            ) {
                                RoutePlannerWaypointList(
                                    uiState = uiState,
                                    detent = detent,
                                    onEvent = onEvent,
                                    onMoreStopsClick = onMoreStopsClick,
                                    rowHeight = rowHeight,
                                    modifier = Modifier.then(weightWhen(isFullScreen, fill = false)),
                                )
                                RoutePlannerStopActions(
                                    isAddStopEnabled = uiState.isAddStopEnabled,
                                    isRoundTripEnabled = uiState.isRoundTripEnabled,
                                    onAddStopClick = { onEvent(RoutePlannerUiEvents.AddEmptyStopClicked) },
                                    onRoundTripClick = { onEvent(RoutePlannerUiEvents.RoundTripClicked) },
                                )
                            }
                            if (uiState.isRoutePlanLoading) {
                                RoutePlannerLoading(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.ExtraLarge,
                        top = Dimens.Medium,
                        end = Dimens.ExtraLarge,
                        bottom = navBarInset + Dimens.Large,
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
            ) {
                RoutePlannerMinimizedSelectedStop(
                    uiState = uiState,
                    isVisible = isSelectedStopShown,
                    onEvent = onEvent,
                    onHeightMeasured = { selectedStopHeight = it },
                )
                RoutePlannerRouteFooter(
                    uiState = uiState,
                    isMinimized = isMinimized,
                    stopListAlpha = stopListAlpha,
                    onEvent = onEvent,
                    modifier = Modifier.onGloballyPositioned {
                        footerHeight = with(density) { it.size.height.toDp() }
                    },
                )
            }
        }
    }
}

@Composable
private fun RoutePlannerRouteFooter(
    uiState: RoutePlannerUiState,
    isMinimized: Boolean,
    stopListAlpha: Float,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        // The stats replace a shorter placeholder once the route arrives, and the sheet follows this height
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
                modifier = Modifier.hiddenWhileLoading(uiState.isRoutePlanLoading, stopListAlpha),
            )
            if (isMinimized && uiState.isRoutePlanLoading) {
                // Drawn over the stats' slot without resizing it, so the minimized sheet keeps its height
                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    RoutePlannerLoading()
                }
            }
        }
        AnimatedVisibility(
            visible = uiState.isSaveButtonVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            PrimaryButton(
                iconResId = R.drawable.ic_save,
                text = mokoString(SharedRes.strings.route_planner_save),
                onClick = { onEvent(RoutePlannerUiEvents.SaveRouteClicked) },
                enabled = uiState.isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.ROUTE_PLANNER_SAVE_BUTTON),
            )
        }
    }
}

@Composable
private fun RoutePlannerMinimizedSelectedStop(
    uiState: RoutePlannerUiState,
    isVisible: Boolean,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onHeightMeasured: (Dp) -> Unit,
) {
    val density = LocalDensity.current
    val selectedStop = uiState.selectedStop
    // The stop stays drawn while the card shrinks away after the selection ends
    var shownStop by remember { mutableStateOf(selectedStop) }
    if (selectedStop != null) shownStop = selectedStop
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        shownStop?.let { stop ->
            RoutePlannerSelectedStopCard(
                stop = stop,
                waypointType = uiState.stopType(uiState.stops.indexOfFirst { it.id == stop.id }),
                hasConnectorAbove = false,
                hasConnectorBelow = false,
                onEvent = onEvent,
                modifier = Modifier
                    .onGloballyPositioned { onHeightMeasured(with(density) { it.size.height.toDp() }) }
                    // Without plain rows to line up with, the card keeps to the sheet's content edges
                    .padding(horizontal = Dimens.RoutePlannerStopCardInset),
            )
        }
    }
}

@Composable
private fun rememberPlannerReveal(isMinimized: Boolean): Animatable<Float, AnimationVector1D> {
    // The block stays composed while minimized: building its stops on the release of a swipe takes several
    // frames, which would make the settling sheet skip ahead
    val fraction = remember { Animatable(if (isMinimized) 0f else 1f) }
    LaunchedEffect(isMinimized) {
        fraction.animateTo(if (isMinimized) 0f else 1f, spring(stiffness = Spring.StiffnessMediumLow))
    }
    return fraction
}

@Composable
private fun ReportSettledExpandedHeight(
    cardHeight: Dp,
    isExpanded: Boolean,
    isRevealing: Boolean,
    onExpandedHeightMeasured: (Dp) -> Unit,
) {
    // Only the settled height counts, the map's camera would otherwise follow the stops sliding in
    LaunchedEffect(cardHeight, isExpanded, isRevealing) {
        if (isExpanded && !isRevealing && cardHeight != Dp.Unspecified) onExpandedHeightMeasured(cardHeight)
    }
}

/**
 * Shows the top [fraction] of the content's height, faded by the same fraction.
 */
private fun Modifier.revealed(fraction: () -> Float): Modifier =
    clipToBounds().layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val visibleFraction = fraction().coerceIn(0f, 1f)
        layout(placeable.width, (placeable.height * visibleFraction).roundToInt()) {
            placeable.placeWithLayer(x = 0, y = 0) { alpha = visibleFraction }
        }
    }

// Planning keeps the stop list in the layout so the sheet does not resize under the loading
// indicator, which also means the rows it hides must stop taking taps.
private fun Modifier.hiddenWhileLoading(isLoading: Boolean, alpha: Float): Modifier =
    this
        .alpha(alpha)
        .then(
            if (isLoading) {
                Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    }
                }
            } else {
                Modifier
            },
        )

// The full screen stage pins the stats and the save button to the bottom, so the stop list takes
// whatever space is left instead of the sheet growing past the screen.

@Composable
private fun RoutePlannerHeader(
    detent: RoutePlannerDetent,
    onDetentClick: () -> Unit,
    onCloseClick: () -> Unit,
    onGraphhopperClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoutePlannerHeaderButton(
            iconResId = if (detent == RoutePlannerDetent.MINIMIZED) {
                R.drawable.ic_open_in_full
            } else {
                R.drawable.ic_close_fullscreen
            },
            contentDescription = mokoString(
                if (detent == RoutePlannerDetent.MINIMIZED) {
                    SharedRes.strings.route_planner_a11y_expand
                } else {
                    SharedRes.strings.route_planner_a11y_minimize
                },
            ),
            onClick = onDetentClick,
            modifier = Modifier.testTag(TestTags.ROUTE_PLANNER_DETENT_BUTTON),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.Small),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = mokoString(SharedRes.strings.route_planner_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(TestTags.ROUTE_PLANNER_TITLE),
            )
            GraphhopperAttribution(onClick = onGraphhopperClick)
        }
        RoutePlannerHeaderButton(
            iconResId = R.drawable.ic_close,
            contentDescription = mokoString(SharedRes.strings.a11y_close),
            onClick = onCloseClick,
            modifier = Modifier.testTag(TestTags.ROUTE_PLANNER_CLOSE_BUTTON),
        )
    }
}

@Composable
private fun RoutePlannerHeaderButton(
    @DrawableRes iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = contentDescription,
        )
    }
}

private const val SECONDARY_TEXT_ALPHA = 0.6f

@Composable
private fun GraphhopperAttribution(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PoweredByAttribution(
        logo = painterResource(id = SharedRes.images.img_graphhopper_logo.drawableResId),
        logoHeight = 9.dp,
        logoTint = MaterialTheme.colorScheme.onSurface,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = SECONDARY_TEXT_ALPHA),
            fontSize = 9.sp,
        ),
        contentDescription = mokoString(SharedRes.strings.route_planner_a11y_open_graphhopper),
        onClick = onClick,
        modifier = modifier.testTag(TestTags.ROUTE_PLANNER_GRAPHHOPPER_ATTRIBUTION),
    )
}

@Preview
@Composable
private fun RoutePlannerContentPreview() {
    HuKiTheme {
        RoutePlannerContent(
            uiState = PreviewRoutePlanner.uiState(routePlan = PreviewRoutePlanner.routePlan),
            detent = RoutePlannerDetent.EXPANDED,
            onEvent = {},
            onMoreStopsClick = {},
        )
    }
}

@Preview
@Composable
private fun RoutePlannerContentErrorPreview() {
    HuKiTheme {
        RoutePlannerContent(
            uiState = PreviewRoutePlanner.uiState(
                error = NetworkError.RATE_LIMITED.toRoutePlanInfoViewData(),
            ),
            detent = RoutePlannerDetent.EXPANDED,
            onEvent = {},
            onMoreStopsClick = {},
        )
    }
}

@Preview
@Composable
private fun RoutePlannerContentMinimizedPreview() {
    HuKiTheme {
        RoutePlannerContent(
            uiState = PreviewRoutePlanner.uiState(routePlan = PreviewRoutePlanner.routePlan),
            detent = RoutePlannerDetent.MINIMIZED,
            onEvent = {},
            onMoreStopsClick = {},
        )
    }
}

@Preview
@Composable
private fun RoutePlannerContentMinimizedSelectedStopPreview() {
    val uiState = PreviewRoutePlanner.uiState(routePlan = PreviewRoutePlanner.routePlan)
    HuKiTheme {
        RoutePlannerContent(
            uiState = uiState.copy(selectedStopId = uiState.stops.last().id),
            detent = RoutePlannerDetent.MINIMIZED,
            onEvent = {},
            onMoreStopsClick = {},
        )
    }
}

@Preview
@Composable
private fun RoutePlannerContentFullScreenPreview() {
    HuKiTheme {
        RoutePlannerContent(
            uiState = PreviewRoutePlanner.uiState(stops = PreviewRoutePlanner.manyStops(6)),
            detent = RoutePlannerDetent.FULL_SCREEN,
            onEvent = {},
            onMoreStopsClick = {},
        )
    }
}

private fun ColumnScope.weightWhen(isFullScreen: Boolean, fill: Boolean = true): Modifier =
    if (isFullScreen) Modifier.weight(weight = 1f, fill = fill) else Modifier
