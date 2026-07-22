package hu.mostoha.mobile.kmp.huki.ui.features.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.icerock.moko.permissions.compose.BindEffect
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.analytics.GpxSource
import hu.mostoha.mobile.kmp.huki.model.domain.GpxMapsNavigationType
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.isModal
import hu.mostoha.mobile.kmp.huki.model.domain.isStandard
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.features.gpx.GpxDetailsBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.layers.LayersBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.map.MapContent
import hu.mostoha.mobile.kmp.huki.ui.features.search.SearchBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.whatsnew.WhatsNewBottomSheet
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.navigateToAppSettings
import hu.mostoha.mobile.kmp.huki.util.navigateToDirections
import hu.mostoha.mobile.kmp.huki.util.rememberScopedViewModelStoreOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    onMenuClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
    onGpxCollectionClicked: () -> Unit,
    onPlaceHistoryClicked: () -> Unit,
    onDestinationsClicked: () -> Unit,
    openGpxUri: String? = null,
    onOpenGpxConsumed: () -> Unit = {},
    openPlace: Pair<OsmType, String>? = null,
    onOpenPlaceConsumed: () -> Unit = {},
    openDestinationOsmId: String? = null,
    onOpenDestinationConsumed: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BindEffect(viewModel.permissionsController)

    LaunchedEffect(openGpxUri) {
        openGpxUri?.let {
            viewModel.onEvent(MainUiEvents.GpxFileReopened(it))
            onOpenGpxConsumed()
        }
    }

    LaunchedEffect(openPlace) {
        openPlace?.let { (osmType, osmId) ->
            viewModel.onEvent(MainUiEvents.HistoryPlaceSelected(osmType, osmId))
            onOpenPlaceConsumed()
        }
    }

    LaunchedEffect(openDestinationOsmId) {
        openDestinationOsmId?.let { osmId ->
            viewModel.onEvent(MainUiEvents.DestinationSelected(osmId))
            onOpenDestinationConsumed()
        }
    }

    MainContent(
        uiState = uiState,
        mainUiEffects = viewModel.mainUiEffects,
        mapUiEffects = viewModel.mapUiEffects,
        onEvent = viewModel::onEvent,
        onMenuClicked = onMenuClicked,
        onLocationIqClicked = onLocationIqClicked,
        onGpxCollectionClicked = onGpxCollectionClicked,
        onPlaceHistoryClicked = onPlaceHistoryClicked,
        onDestinationsClicked = onDestinationsClicked,
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    mainUiEffects: Flow<MainUiEffects>,
    mapUiEffects: Flow<MapUiEffects>,
    onEvent: (MainUiEvents) -> Unit,
    onMenuClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
    onGpxCollectionClicked: () -> Unit,
    onPlaceHistoryClicked: () -> Unit,
    onDestinationsClicked: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val standardSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )
    val standardSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = standardSheetState)
    var showModalBottomSheet by remember { mutableStateOf(false) }
    var gpxSheetPeekHeight by remember { mutableStateOf(300.dp) }
    val currentSheet by rememberUpdatedState(uiState.sheet)

    val gpxFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { onEvent(MainUiEvents.GpxFileSelected(it.toString(), GpxSource.LAYERS)) }
        },
    )

    LaunchedEffect(Unit) {
        snapshotFlow { standardSheetScaffoldState.bottomSheetState.currentValue }
            .distinctUntilChanged()
            .collect { value ->
                val sheet = currentSheet ?: return@collect
                if (!sheet.isStandard()) return@collect
                val dismissed = if (sheet is Sheet.Gpx) {
                    value == SheetValue.Hidden
                } else {
                    value == SheetValue.PartiallyExpanded || value == SheetValue.Hidden
                }
                if (dismissed) {
                    onEvent(MainUiEvents.SheetDismissed)
                }
            }
    }

    LaunchedEffect(uiState.sheet) {
        val sheet = uiState.sheet
        when {
            sheet == null -> {
                modalSheetState.hide()
                showModalBottomSheet = false
                standardSheetState.hide()
            }
            sheet.isStandard() -> {
                modalSheetState.hide()
                showModalBottomSheet = false
                if (sheet is Sheet.Gpx) {
                    standardSheetState.partialExpand()
                } else {
                    standardSheetState.expand()
                }
            }
            sheet.isModal() -> {
                standardSheetState.hide()
                showModalBottomSheet = true
            }
        }
    }

    MainUiEffectHandler(
        mainUiEffects = mainUiEffects,
        onShowGpxFilePicker = { gpxFilePickerLauncher.launch(arrayOf("*/*")) },
    )

    BottomSheetScaffold(
        scaffoldState = standardSheetScaffoldState,
        sheetPeekHeight = if (uiState.sheet is Sheet.Gpx) gpxSheetPeekHeight else 0.dp,
        sheetDragHandle = null,
        sheetSwipeEnabled = true,
        sheetContainerColor = Color.Transparent,
        sheetShadowElevation = 0.dp,
        sheetContent = {
            when (val sheet = uiState.sheet) {
                is Sheet.Gpx -> {
                    GpxDetailsBottomSheet(
                        gpxDetails = sheet.gpxDetails,
                        onStartClick = {
                            onEvent(MainUiEvents.GpxStartNavigationClicked)
                        },
                        onNavigateToStart = {
                            onEvent(MainUiEvents.GpxMapsNavigationClicked(GpxMapsNavigationType.START))
                        },
                        onNavigateToEnd = {
                            onEvent(MainUiEvents.GpxMapsNavigationClicked(GpxMapsNavigationType.END))
                        },
                        onCloseClick = {
                            coroutineScope.launch {
                                standardSheetState.hide()
                                onEvent(MainUiEvents.GpxCloseClicked)
                            }
                        },
                        onCollapsedHeightMeasured = { gpxSheetPeekHeight = it },
                    )
                }
                is Sheet.Search -> {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides rememberScopedViewModelStoreOwner(),
                    ) {
                        SearchBottomSheet(
                            onCloseClick = {
                                coroutineScope.launch { standardSheetState.hide() }
                            },
                            onPlaceSelected = { place ->
                                coroutineScope.launch {
                                    standardSheetState.hide()
                                    onEvent(MainUiEvents.SearchPlaceSelected(place))
                                }
                            },
                            onRecentPlaceSelected = { place ->
                                coroutineScope.launch {
                                    standardSheetState.hide()
                                    onEvent(MainUiEvents.SearchRecentPlaceSelected(place))
                                }
                            },
                            onSearchPlaceHistorySelected = { place ->
                                coroutineScope.launch {
                                    standardSheetState.hide()
                                    onEvent(MainUiEvents.SearchResultPlaceHistorySelected(place))
                                }
                            },
                            onDestinationSelected = { destination ->
                                coroutineScope.launch {
                                    standardSheetState.hide()
                                    onEvent(MainUiEvents.SearchDestinationSelected(destination))
                                }
                            },
                            onSearchDestinationSelected = { destination ->
                                coroutineScope.launch {
                                    standardSheetState.hide()
                                    onEvent(MainUiEvents.SearchResultDestinationSelected(destination))
                                }
                            },
                            onGpxFileSelected = { fileUri ->
                                coroutineScope.launch {
                                    standardSheetState.hide()
                                    onEvent(MainUiEvents.GpxFileReopened(fileUri))
                                }
                            },
                            onSeeAllGpxClicked = onGpxCollectionClicked,
                            onSeeAllPlacesClicked = onPlaceHistoryClicked,
                            onSeeAllDestinationsClicked = onDestinationsClicked,
                            onLocationIqClicked = onLocationIqClicked,
                        )
                    }
                }
                else -> Unit
            }
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            MapContent(
                mapUiState = uiState.mapUiState,
                mapUiEffects = mapUiEffects,
                onEvent = onEvent,
            )
            FloatingActionContainer(
                modifier = Modifier.padding(innerPadding),
                mainUiState = uiState,
                onSearchClicked = {
                    onEvent(MainUiEvents.SearchClicked)
                },
                onLayersClicked = {
                    onEvent(MainUiEvents.LayersClicked)
                },
                onMyLocationClicked = {
                    onEvent(MainUiEvents.MyLocationClicked)
                },
                onZoomInClicked = {
                    onEvent(MainUiEvents.ZoomInClicked)
                },
                onZoomOutClicked = {
                    onEvent(MainUiEvents.ZoomOutClicked)
                },
                onGpxToggleLineClicked = {
                    onEvent(MainUiEvents.GpxRouteVisibilityToggled)
                },
                onGpxToggleDistancesClicked = {
                    onEvent(MainUiEvents.GpxDistancesVisibilityToggled)
                },
                onGpxOverviewClicked = {
                    onEvent(MainUiEvents.GpxOverviewClicked)
                },
                onGpxClearClicked = {
                    onEvent(MainUiEvents.GpxCloseClicked)
                },
                onMenuClicked = onMenuClicked,
            )
            if (showModalBottomSheet) {
                MainModalBottomSheet(
                    uiState = uiState,
                    sheetState = modalSheetState,
                    onEvent = onEvent,
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
private fun MainUiEffectHandler(mainUiEffects: Flow<MainUiEffects>, onShowGpxFilePicker: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(mainUiEffects) {
        mainUiEffects.collect { effect ->
            when (effect) {
                MainUiEffects.NavigateToAppSettings -> context.navigateToAppSettings()
                MainUiEffects.ShowGpxFilePicker -> onShowGpxFilePicker()
                is MainUiEffects.OpenMapsNavigation -> context.navigateToDirections(effect.location)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainModalBottomSheet(uiState: MainUiState, sheetState: SheetState, onEvent: (MainUiEvents) -> Unit) {
    when (val sheet = uiState.sheet) {
        is Sheet.Layers -> {
            LayersBottomSheet(
                sheetState = sheetState,
                selectedBaseLayer = uiState.mapUiState.baseLayer,
                isHikingLayerSelected = uiState.mapUiState.hikingLayerVisible,
                isGpxLayerSelected = uiState.mapUiState.gpxLayerVisible,
                onBaseLayerSelected = { onEvent(MainUiEvents.BaseLayerSelected(it)) },
                onHikingLayerSelected = { onEvent(MainUiEvents.HikingLayerSelected) },
                onGpxLayerSelected = { onEvent(MainUiEvents.GpxLayerSelected) },
                onDismissRequest = { onEvent(MainUiEvents.SheetDismissed) },
            )
        }
        is Sheet.WhatsNew -> {
            WhatsNewBottomSheet(
                whatsNew = sheet.whatsNew,
                sheetState = sheetState,
                onDismissRequest = { onEvent(MainUiEvents.SheetDismissed) },
            )
        }
        else -> Unit
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
            onMenuClicked = {},
            onLocationIqClicked = {},
            onGpxCollectionClicked = {},
            onPlaceHistoryClicked = {},
            onDestinationsClicked = {},
        )
    }
}

@Preview
@Composable
private fun MainContentLoadingPreview() {
    HuKiTheme {
        MainContent(
            uiState = MainUiState(isGpxLoading = true),
            mainUiEffects = emptyFlow(),
            mapUiEffects = emptyFlow(),
            onEvent = {},
            onMenuClicked = {},
            onLocationIqClicked = {},
            onGpxCollectionClicked = {},
            onPlaceHistoryClicked = {},
            onDestinationsClicked = {},
        )
    }
}
