package hu.mostoha.mobile.kmp.huki.ui.features.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import hu.mostoha.mobile.kmp.huki.ui.features.gpx.GpxDetailsBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.layers.LayersBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.map.MapContent
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.navigateToAppSettings
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val layersSheetState = rememberModalBottomSheetState()
    val detailsBottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )
    val detailsBottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = detailsBottomSheetState)
    var showLayersBottomSheet by remember { mutableStateOf(false) }

    val gpxFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { onEvent(MainUiEvents.GpxFileSelected(it.toString())) }
        },
    )

    LaunchedEffect(mainUiEffects) {
        mainUiEffects.collect { effect ->
            when (effect) {
                is MainUiEffects.ShowLayersBottomSheet ->
                    scope.launch {
                        if (effect.show) {
                            showLayersBottomSheet = true
                        } else {
                            layersSheetState.hide()
                            showLayersBottomSheet = false
                        }
                    }
                MainUiEffects.NavigateToAppSettings -> context.navigateToAppSettings()
                MainUiEffects.ShowGpxFilePicker -> gpxFilePickerLauncher.launch(arrayOf("*/*"))
                is MainUiEffects.ShowDetailsBottomSheet ->
                    scope.launch {
                        if (effect.show) {
                            detailsBottomSheetState.expand()
                        } else {
                            detailsBottomSheetState.hide()
                        }
                    }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = detailsBottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        sheetSwipeEnabled = true,
        sheetContent = {
            val gpxDetails = uiState.mapUiState.gpxDetails
            when {
                gpxDetails != null -> {
                    GpxDetailsBottomSheet(
                        gpxDetails = gpxDetails,
                        onStartClick = {
                            onEvent(MainUiEvents.GpxStartNavigationClicked)
                        },
                        onDismissRequest = {
                            onEvent(MainUiEvents.GpxCloseClicked)
                        },
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            MapContent(
                mapUiState = uiState.mapUiState,
                mapUiEffects = mapUiEffects,
                onEvent = onEvent,
            )
            FloatingActionContainer(
                mainUiState = uiState,
                onLayersClicked = {
                    onEvent(MainUiEvents.LayersClicked)
                },
                onMyLocationClicked = {
                    onEvent(MainUiEvents.MyLocationClicked)
                },
            )
            if (showLayersBottomSheet) {
                LayersBottomSheet(
                    sheetState = layersSheetState,
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
                        onEvent(MainUiEvents.LayersDismissed)
                    },
                )
            }
            uiState.alert?.let { alert ->
                AlertDialog(
                    onDismissRequest = {
                        onEvent(MainUiEvents.AlertDismissed)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onEvent(MainUiEvents.AlertDismissed)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(text = mokoString(SharedRes.strings.alert_ok))
                        }
                    },
                    title = {
                        Text(
                            text = mokoString(alert.title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    },
                    text = {
                        Text(text = mokoString(alert.message))
                    },
                )
            }
        }
    }
}

@Composable
private fun FloatingActionContainer(
    mainUiState: MainUiState,
    onLayersClicked: () -> Unit,
    onMyLocationClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
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
                .padding(Dimens.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            SmallFloatingActionButton(
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

@Preview
@Composable
private fun MainContentLoadingPreview() {
    HuKiTheme {
        MainContent(
            uiState = MainUiState(isLoading = true),
            mainUiEffects = emptyFlow(),
            mapUiEffects = emptyFlow(),
            onEvent = {},
        )
    }
}
