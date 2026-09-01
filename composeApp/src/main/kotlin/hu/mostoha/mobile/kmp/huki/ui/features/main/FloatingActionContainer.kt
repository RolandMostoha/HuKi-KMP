package hu.mostoha.mobile.kmp.huki.ui.features.main

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun FloatingActionContainer(
    mainUiState: MainUiState,
    onSearchClicked: () -> Unit,
    onLayersClicked: () -> Unit,
    onMyLocationClicked: () -> Unit,
    onMyLocationLongClicked: () -> Unit,
    onZoomInClicked: () -> Unit,
    onZoomOutClicked: () -> Unit,
    onGpxToggleLineClicked: () -> Unit,
    onGpxToggleDistancesClicked: () -> Unit,
    onGpxOverviewClicked: () -> Unit,
    onGpxClearClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(bottom = Dimens.Small),
    ) {
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomStart),
            visible = mainUiState.sheet == null &&
                mainUiState.mapUiState.gpxDetails != null &&
                mainUiState.mapUiState.gpxLayerVisible,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
        ) {
            GpxControlMenu(
                isRouteVisible = mainUiState.mapUiState.gpxRouteVisible,
                isAllDistancesVisible = mainUiState.mapUiState.allDistancesVisible,
                onToggleLineClicked = onGpxToggleLineClicked,
                onToggleDistancesClicked = onGpxToggleDistancesClicked,
                onOverviewClicked = onGpxOverviewClicked,
                onClearClicked = onGpxClearClicked,
            )
        }
        if (isLandscape) {
            // Landscape: search bar bottom-start with a fixed width, FABs dropped to bottom-end.
            SearchBarAction(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        horizontal = Dimens.Medium,
                        vertical = Dimens.Small,
                    ),
                visible = mainUiState.isSearchBarVisible && mainUiState.sheet == null,
                searchBarModifier = Modifier.width(Dimens.SearchBarLandscapeWidth),
                onSearchClicked = onSearchClicked,
                onMenuClicked = onMenuClicked,
            )
            FabColumnAction(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        horizontal = Dimens.Medium,
                        vertical = Dimens.Small,
                    ),
                mainUiState = mainUiState,
                onLayersClicked = onLayersClicked,
                onMyLocationClicked = onMyLocationClicked,
                onMyLocationLongClicked = onMyLocationLongClicked,
                onZoomInClicked = onZoomInClicked,
                onZoomOutClicked = onZoomOutClicked,
            )
        } else {
            // Portrait: FABs stacked above a full-width search bar.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        horizontal = Dimens.Medium,
                        vertical = Dimens.Small,
                    ),
                horizontalAlignment = Alignment.End,
            ) {
                FabColumnAction(
                    modifier = Modifier.padding(bottom = Dimens.Large),
                    mainUiState = mainUiState,
                    onLayersClicked = onLayersClicked,
                    onMyLocationClicked = onMyLocationClicked,
                    onMyLocationLongClicked = onMyLocationLongClicked,
                    onZoomInClicked = onZoomInClicked,
                    onZoomOutClicked = onZoomOutClicked,
                )
                SearchBarAction(
                    visible = mainUiState.isSearchBarVisible && mainUiState.sheet == null,
                    searchBarModifier = Modifier.padding(horizontal = Dimens.Medium),
                    onSearchClicked = onSearchClicked,
                    onMenuClicked = onMenuClicked,
                )
            }
        }
    }
}

@Composable
private fun FabColumnAction(
    mainUiState: MainUiState,
    onLayersClicked: () -> Unit,
    onMyLocationClicked: () -> Unit,
    onMyLocationLongClicked: () -> Unit,
    onZoomInClicked: () -> Unit,
    onZoomOutClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = mainUiState.sheet == null,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
    ) {
        Column(
            modifier = Modifier
                .padding(end = Dimens.Medium)
                .wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val myLocationStatus = mainUiState.myLocationState.myLocationStatus
            val isZoomControlsVisible = mainUiState.mapZoomControlsAlwaysVisible ||
                myLocationStatus == MyLocationStatus.FollowingLiveCompass
            AnimatedVisibility(
                visible = isZoomControlsVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                MapZoomControls(
                    modifier = Modifier.padding(bottom = Dimens.ExtraSmall),
                    onZoomInClicked = onZoomInClicked,
                    onZoomOutClicked = onZoomOutClicked,
                )
            }
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = Dimens.FloatingActionElevation,
                ),
                shape = CircleShape,
                onClick = {
                    if (!mainUiState.isGpxLoading) {
                        onLayersClicked()
                    }
                },
            ) {
                if (mainUiState.isGpxLoading) {
                    LoadingIndicator(
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_fab_layers),
                        contentDescription = mokoString(SharedRes.strings.layers_a11y_fab),
                    )
                }
            }
            FloatingActionButton(
                modifier = Modifier.testTag(TestTags.MAIN_FAB_MY_LOCATION_BUTTON),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = Dimens.FloatingActionElevation,
                ),
                shape = CircleShape,
                onClick = {},
            ) {
                val myLocationIconRes = when (myLocationStatus) {
                    MyLocationStatus.Default -> R.drawable.ic_fab_my_location_default
                    MyLocationStatus.Following -> R.drawable.ic_fab_my_location_following
                    MyLocationStatus.FollowingLiveCompass -> R.drawable.ic_fab_my_location_live_compass
                    MyLocationStatus.NotAvailable -> R.drawable.ic_fab_my_location_default
                }
                Box(
                    modifier = Modifier
                        .size(Dimens.FloatingActionSize)
                        .combinedClickable(
                            enabled = !mainUiState.isMyLocationLoading,
                            onClick = onMyLocationClicked,
                            onLongClick = onMyLocationLongClicked,
                            onClickLabel = mokoString(myLocationStatus.a11yId),
                            onLongClickLabel = mokoString(SharedRes.strings.my_location_a11y_live_compass),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (mainUiState.isMyLocationLoading) {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = ImageVector.vectorResource(myLocationIconRes),
                            contentDescription = mokoString(myLocationStatus.a11yId),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBarAction(
    visible: Boolean,
    onSearchClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    modifier: Modifier = Modifier,
    searchBarModifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        SearchBar(
            modifier = searchBarModifier,
            onSearchClick = onSearchClicked,
            onMenuClick = onMenuClicked,
        )
    }
}

@Preview
@Composable
private fun MainContentPreview() {
    HuKiTheme {
        FloatingActionContainer(
            mainUiState = MainUiState(),
            onSearchClicked = {},
            onLayersClicked = {},
            onMyLocationClicked = {},
            onMyLocationLongClicked = {},
            onZoomInClicked = {},
            onZoomOutClicked = {},
            onGpxToggleLineClicked = {},
            onGpxToggleDistancesClicked = {},
            onGpxOverviewClicked = {},
            onGpxClearClicked = {},
            onMenuClicked = {},
        )
    }
}
