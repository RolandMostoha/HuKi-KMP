package hu.mostoha.mobile.kmp.huki.ui.features.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(bottom = Dimens.Small),
    ) {
        if (mainUiState.isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = Dimens.Large, end = Dimens.ExtraLarge)
                    .size(32.dp)
                    .drawBehind {
                        val strokePx = 4.dp.toPx()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = size.minDimension / 2 - strokePx / 2,
                            style = Stroke(width = strokePx),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    strokeWidth = 5.dp,
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    horizontal = Dimens.Medium,
                    vertical = Dimens.Small,
                ),
            horizontalAlignment = Alignment.End,
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        end = Dimens.Medium,
                        bottom = Dimens.Large,
                    )
                    .wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
            ) {
                SmallFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = Dimens.FloatingActionElevation,
                    ),
                    shape = CircleShape,
                    onClick = { onLayersClicked() },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_fab_layers),
                        contentDescription = mokoString(SharedRes.strings.layers_a11y_fab),
                    )
                }
                FloatingActionButton(
                    modifier = Modifier.testTag(TestTags.MAIN_FAB_MY_LOCATION_BUTTON),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = Dimens.FloatingActionElevation,
                    ),
                    shape = CircleShape,
                    onClick = { onMyLocationClicked() },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            when (mainUiState.myLocationState.myLocationStatus) {
                                MyLocationStatus.Default -> R.drawable.ic_fab_my_location_default
                                MyLocationStatus.Following -> R.drawable.ic_fab_my_location_following
                                MyLocationStatus.FollowingLiveCompass -> R.drawable.ic_fab_my_location_live_compass
                                MyLocationStatus.NotAvailable -> R.drawable.ic_fab_my_location_default
                            },
                        ),
                        contentDescription = mokoString(mainUiState.myLocationState.myLocationStatus.accessibilityId),
                    )
                }
            }
            AnimatedVisibility(
                visible = mainUiState.sheet == Sheet.SearchBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                SearchBar(
                    modifier = Modifier
                        .padding(horizontal = Dimens.Medium),
                    onSearchClick = {
                        onSearchClicked()
                    },
                    onMenuClick = {
                        // TODO Feature:Menu
                    },
                )
            }
        }
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
        )
    }
}
