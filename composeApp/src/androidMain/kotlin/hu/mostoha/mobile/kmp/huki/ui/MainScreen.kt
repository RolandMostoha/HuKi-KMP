package hu.mostoha.mobile.kmp.huki.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.permissions.compose.BindEffect
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.main.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.features.layers.LayersBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.map.MapComponent
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.utils.mokoString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BindEffect(viewModel.permissionsController)

    MainContent(
        uiState = uiState,
        mainUiEffects = viewModel.mainUiEffects,
        mapUiEffects = viewModel.mapUiEffects,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    mainUiEffects: Flow<MainUiEffects>,
    mapUiEffects: Flow<MapUiEffects>,
    onEvent: (MainUiEvents) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showLayersBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(mainUiEffects) {
        mainUiEffects.collect { effect ->
            when (effect) {
                is MainUiEffects.ShowLayersBottomSheet -> {
                    showLayersBottomSheet = true
                }
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
    ) {
        MapComponent(
            mapUiState = uiState.mapUiState,
            mapUiEffects = mapUiEffects,
            onEvent = onEvent,
        )

        FloatingActionContainer(
            uiState = uiState,
            onLayersClicked = {
                onEvent(MainUiEvents.LayersClicked)
            },
            onMyLocationClicked = {
                onEvent(MainUiEvents.MyLocationClicked)
            },
        )

        if (showLayersBottomSheet) {
            LayersBottomSheet(
                sheetState = sheetState,
                selectedBaseLayer = uiState.mapUiState.baseLayer,
                isHikingLayerSelected = uiState.mapUiState.hikingLayerVisible,
                isGpxLayerSelected = uiState.mapUiState.gpxLayerVisible,
                onBaseLayerSelected = {
                    onEvent(MainUiEvents.BaseLayerSelected(it))
                },
                onHikingLayerSelected = {
                    onEvent(MainUiEvents.HikingLayerSelected)
                },
                onGpxLayerSelected = {
                    onEvent(MainUiEvents.GpxLayerSelected)
                },
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                        showLayersBottomSheet = false
                    }
                },
            )
        }
    }
}

@Composable
private fun FloatingActionContainer(
    uiState: MainUiState,
    onLayersClicked: () -> Unit,
    onMyLocationClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimens.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            SmallFloatingActionButton(
                modifier = Modifier.testTag(TestTags.MAIN_FAB_LAYERS),
                containerColor = MaterialTheme.colorScheme.surface,
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
                shape = CircleShape,
                onClick = { onMyLocationClicked() },
            ) {
                Icon(
                    modifier = Modifier.testTag(TestTags.MAIN_FAB_MY_LOCATION_BUTTON),
                    imageVector = ImageVector.vectorResource(
                        when (uiState.myLocationState.myLocationStatus) {
                            MyLocationStatus.Default -> R.drawable.ic_fab_my_location_default
                            MyLocationStatus.Following -> R.drawable.ic_fab_my_location_following
                            MyLocationStatus.FollowingLiveCompass -> R.drawable.ic_fab_my_location_live_compass
                            MyLocationStatus.NotAvailable -> R.drawable.ic_fab_my_location_default
                        },
                    ),
                    contentDescription = mokoString(
                        when (uiState.myLocationState.myLocationStatus) {
                            MyLocationStatus.NotAvailable -> SharedRes.strings.my_location_a11y_not_available
                            MyLocationStatus.Default -> SharedRes.strings.my_location_a11y_default
                            MyLocationStatus.Following -> SharedRes.strings.my_location_a11y_following
                            MyLocationStatus.FollowingLiveCompass -> SharedRes.strings.my_location_a11y_live_compass
                        },
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainContentPreview() {
    HuKiTheme {
        MainContent(
            uiState = MainUiState(),
            mainUiEffects = emptyFlow(),
            mapUiEffects = emptyFlow(),
            onEvent = {},
        )
    }
}
