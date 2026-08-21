package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
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
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiState
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlanInfoViewData
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.ui.components.PrimaryButton
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun RoutePlannerContent(
    uiState: RoutePlannerUiState,
    detent: RoutePlannerDetent,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onMoreStopsClick: () -> Unit,
    modifier: Modifier = Modifier,
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
    var cardHeight by remember { mutableStateOf(Dp.Unspecified) }
    var plannerHeight by remember { mutableStateOf(Dp.Unspecified) }

    // The minimized sheet is the card without the planner block, and the planner block only has a
    // height while it is visible — so the peek is derived instead of measured on a hidden copy.
    LaunchedEffect(cardHeight, plannerHeight, detent) {
        val isMeasured = cardHeight != Dp.Unspecified && plannerHeight != Dp.Unspecified
        if (detent != RoutePlannerDetent.EXPANDED || !isMeasured) {
            return@LaunchedEffect
        }
        onMinimizedHeightMeasured(cardHeight - plannerHeight)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.ROUTE_PLANNER_SHEET)
            .onGloballyPositioned {
                if (detent != RoutePlannerDetent.EXPANDED) return@onGloballyPositioned
                val height = with(density) { it.size.height.toDp() }
                cardHeight = height
                onExpandedHeightMeasured(height)
            },
        shape = RoundedCornerShape(
            topStart = Dimens.ExtraLarge,
            topEnd = Dimens.ExtraLarge,
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Small),
    ) {
        Column(
            modifier = Modifier
                .then(if (isFullScreen) Modifier.fillMaxHeight() else Modifier.wrapContentHeight())
                .padding(top = Dimens.Small),
        ) {
            DragHandle(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalPadding = Dimens.SmallMedium,
            )
            RoutePlannerHeader(onCloseClick = { onEvent(RoutePlannerUiEvents.CloseClicked) })
            if (!isMinimized) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(weightWhen(isFullScreen))
                        .onGloballyPositioned { plannerHeight = with(density) { it.size.height.toDp() } }
                        .padding(
                            start = Dimens.ExtraLarge,
                            top = Dimens.Medium,
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
                            RoutePlannerWaypointList(
                                uiState = uiState,
                                detent = detent,
                                onEvent = onEvent,
                                onMoreStopsClick = onMoreStopsClick,
                                rowHeight = rowHeight,
                                modifier = Modifier.hiddenWhileLoading(uiState.isRoutePlanLoading, stopListAlpha),
                            )
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
                        top = Dimens.Large,
                        end = Dimens.ExtraLarge,
                        bottom = navBarInset + Dimens.Large,
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    RoutePlannerRouteStatus(
                        error = uiState.routePlanError,
                        routeStats = uiState.routeStats,
                        isPlanExpected = uiState.isRoutePlanExpected,
                        onRetryClick = { onEvent(RoutePlannerUiEvents.RetryClicked) },
                        modifier = Modifier.hiddenWhileLoading(uiState.isRoutePlanLoading, stopListAlpha),
                    )
                    if (isMinimized && uiState.isRoutePlanLoading) {
                        RoutePlannerLoading(modifier = Modifier.align(Alignment.Center))
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
private fun ColumnScope.weightWhen(isFullScreen: Boolean, fill: Boolean = true): Modifier =
    if (isFullScreen) Modifier.weight(weight = 1f, fill = fill) else Modifier

@Composable
private fun RoutePlannerHeader(onCloseClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.ExtraLarge, end = Dimens.Large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mokoString(SharedRes.strings.route_planner_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.ROUTE_PLANNER_TITLE),
        )
        IconButton(
            onClick = onCloseClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.testTag(TestTags.ROUTE_PLANNER_CLOSE_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = mokoString(SharedRes.strings.a11y_close),
            )
        }
    }
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
