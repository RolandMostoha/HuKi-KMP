package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEffects
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerBottomSheet(
    stops: List<RoutePlannerWaypoint>,
    restored: RoutePlannerSession?,
    mapEvent: RoutePlannerMapEvent?,
    detent: RoutePlannerDetent,
    onMoreStopsClick: () -> Unit,
    onDetentChange: (RoutePlannerDetent) -> Unit,
    onRoutePlanUpdated: (
        routePlan: RoutePlan?,
        markers: List<GpxWaypoint>,
        isAwaitingRoutePlan: Boolean,
        isRoutePlanLoading: Boolean,
    ) -> Unit,
    onSessionChanged: (RoutePlannerSession) -> Unit,
    onEffect: (RoutePlannerUiEffects) -> Unit,
    onLocationIqClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPickOnMapChanged: (Boolean) -> Unit = {},
    onMoveStopChanged: (Boolean) -> Unit = {},
    onSelectedStopChanged: (Location?) -> Unit = {},
    onMinimizedHeightMeasured: (Dp) -> Unit = {},
    onExpandedHeightMeasured: (Dp) -> Unit = {},
    viewModel: RoutePlannerViewModel = koinViewModel { parametersOf(stops, restored) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSaveError by remember { mutableStateOf(false) }
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSearchSheetShown by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(
        uiState.routePlan,
        uiState.routePlanMarkers,
        uiState.isAwaitingRoutePlan,
        uiState.isRoutePlanLoading,
    ) {
        onRoutePlanUpdated(
            uiState.routePlan,
            uiState.routePlanMarkers,
            uiState.isAwaitingRoutePlan,
            uiState.isRoutePlanLoading,
        )
    }
    LaunchedEffect(uiState.session) {
        onSessionChanged(uiState.session)
    }
    LaunchedEffect(uiState.isPickingOnMap) {
        onPickOnMapChanged(uiState.isPickingOnMap)
    }
    LaunchedEffect(uiState.isMovingStop) {
        onMoveStopChanged(uiState.isMovingStop)
    }
    LaunchedEffect(uiState.selectedStop?.location) {
        onSelectedStopChanged(uiState.selectedStop?.location)
    }
    LaunchedEffect(mapEvent) {
        mapEvent?.let { viewModel.onEvent(it.event) }
    }
    // Back steps out of the planner one layer at a time instead of leaving the app with the unsaved plan
    BackHandler {
        when {
            uiState.selectedStop != null || uiState.isMovingStop || uiState.isPickingOnMap -> {
                viewModel.onEvent(RoutePlannerUiEvents.StopDeselected)
            }
            detent != RoutePlannerDetent.MINIMIZED -> onDetentChange(RoutePlannerDetent.MINIMIZED)
            else -> viewModel.onEvent(RoutePlannerUiEvents.CloseClicked)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            if (effect is RoutePlannerUiEffects.RoutePlanSaveFailed) {
                showSaveError = true
            }
            onEffect(effect)
        }
    }

    RoutePlannerContent(
        uiState = uiState,
        detent = detent,
        onEvent = viewModel::onEvent,
        onDetentChange = onDetentChange,
        onMinimizedHeightMeasured = onMinimizedHeightMeasured,
        onExpandedHeightMeasured = onExpandedHeightMeasured,
        onMoreStopsClick = onMoreStopsClick,
        modifier = modifier,
    )
    // Removing a modal sheet from composition skips its exit animation, so it is hidden first
    LaunchedEffect(uiState.isWaypointSearchVisible) {
        when {
            !uiState.isWaypointSearchVisible -> if (isSearchSheetShown) {
                keyboardController?.hide()
                searchSheetState.hide()
                isSearchSheetShown = false
            }
            isSearchSheetShown -> searchSheetState.show()
            else -> isSearchSheetShown = true
        }
    }
    if (isSearchSheetShown) {
        RoutePlannerSearchBottomSheet(
            sheetState = searchSheetState,
            myLocation = uiState.myLocation,
            replacedStop = uiState.replacedStop,
            onEvent = viewModel::onEvent,
            onLocationIqClick = onLocationIqClick,
        )
    }
    uiState.renamingStop?.let { stop ->
        RoutePlannerRenameStopSheet(
            stop = stop,
            stopNumber = uiState.stops.indexOfFirst { it.id == stop.id } + 1,
            onEvent = viewModel::onEvent,
        )
    }
    if (showSaveError) {
        AlertDialog(
            onDismissRequest = { showSaveError = false },
            confirmButton = {
                TextButton(
                    onClick = { showSaveError = false },
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
                    text = mokoString(SharedRes.strings.route_planner_save_error_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            },
            text = {
                Text(text = mokoString(SharedRes.strings.route_planner_save_error_message))
            },
        )
    }
}

@Composable
private fun RoutePlannerRenameStopSheet(
    stop: RoutePlannerWaypoint,
    stopNumber: Int,
    onEvent: (RoutePlannerUiEvents) -> Unit,
) {
    val resolvedName = stop.name?.let { mokoString(it) }.orEmpty()
    RoutePlannerRenameStopBottomSheet(
        stopNumber = stopNumber,
        resolvedName = resolvedName,
        currentName = stop.customName ?: resolvedName,
        currentComment = stop.comment.orEmpty(),
        address = stop.placeName,
        onDone = { name, comment ->
            // Keeping the resolved name as typed is no rename, so the stop follows later geocoding
            val customName = if (name == resolvedName) "" else name
            onEvent(RoutePlannerUiEvents.StopRenamed(waypointId = stop.id, name = customName, comment = comment))
        },
        onDismiss = { onEvent(RoutePlannerUiEvents.StopRenameDismissed) },
    )
}
