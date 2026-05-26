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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.permissions.compose.BindEffect
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.isModal
import hu.mostoha.mobile.kmp.huki.model.domain.isStandard
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.features.gpx.GpxDetailsBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.layers.LayersBottomSheet
import hu.mostoha.mobile.kmp.huki.ui.features.map.MapContent
import hu.mostoha.mobile.kmp.huki.ui.features.search.SearchBottomSheet
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.navigateToAppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel(), onSettingsClicked: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BindEffect(viewModel.permissionsController)

    MainContent(
        uiState = uiState,
        mainUiEffects = viewModel.mainUiEffects,
        mapUiEffects = viewModel.mapUiEffects,
        onEvent = viewModel::onEvent,
        onSettingsClicked = onSettingsClicked,
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    mainUiEffects: Flow<MainUiEffects>,
    mapUiEffects: Flow<MapUiEffects>,
    onEvent: (MainUiEvents) -> Unit,
    onSettingsClicked: () -> Unit,
) {
    val context = LocalContext.current
    val modalSheetState = rememberModalBottomSheetState()
    val standardSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )
    val standardSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = standardSheetState)
    var showModalBottomSheet by remember { mutableStateOf(false) }

    val gpxFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { onEvent(MainUiEvents.GpxFileSelected(it.toString())) }
        },
    )

    LaunchedEffect(Unit) {
        snapshotFlow { standardSheetScaffoldState.bottomSheetState.currentValue }
            .distinctUntilChanged()
            .collect { value ->
                if (value == SheetValue.PartiallyExpanded) {
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
                standardSheetState.expand()
            }
            sheet.isModal() -> {
                standardSheetState.hide()
                showModalBottomSheet = true
            }
        }
    }

    LaunchedEffect(mainUiEffects) {
        mainUiEffects.collect { effect ->
            when (effect) {
                MainUiEffects.NavigateToAppSettings -> context.navigateToAppSettings()
                MainUiEffects.ShowGpxFilePicker -> gpxFilePickerLauncher.launch(arrayOf("*/*"))
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = standardSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        sheetSwipeEnabled = true,
        sheetContent = {
            when (val sheet = uiState.sheet) {
                is Sheet.Gpx -> {
                    GpxDetailsBottomSheet(
                        gpxDetails = sheet.gpxDetails,
                        onStartClick = {
                            onEvent(MainUiEvents.GpxStartNavigationClicked)
                        },
                        onCloseClick = {
                            onEvent(MainUiEvents.GpxCloseClicked)
                        },
                    )
                }
                is Sheet.Search -> {
                    SearchBottomSheet(
                        onCloseClick = {
                            onEvent(MainUiEvents.SheetDismissed)
                        },
                        onPlaceSelected = { place ->
                            onEvent(MainUiEvents.SearchPlaceSelected(place))
                        },
                    )
                }
                else -> Unit
            }
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) { innerPadding ->
        Box(
            modifier = Modifier
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
                onSearchClicked = {
                    onEvent(MainUiEvents.SearchClicked)
                },
                onLayersClicked = {
                    onEvent(MainUiEvents.LayersClicked)
                },
                onMyLocationClicked = {
                    onEvent(MainUiEvents.MyLocationClicked)
                },
                onSettingsClicked = onSettingsClicked,
            )
            if (showModalBottomSheet) {
                LayersBottomSheet(
                    sheetState = modalSheetState,
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
                        onEvent(MainUiEvents.SheetDismissed)
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

@Preview
@Composable
private fun MainContentPreview() {
    HuKiTheme {
        MainContent(
            uiState = MainUiState(),
            mainUiEffects = emptyFlow(),
            mapUiEffects = emptyFlow(),
            onEvent = {},
            onSettingsClicked = {},
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
            onSettingsClicked = {},
        )
    }
}
