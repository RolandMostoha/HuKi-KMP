package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEffects
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RoutePlannerBottomSheet(
    place: Place?,
    pick: RoutePlannerPick?,
    detent: RoutePlannerDetent,
    onMoreStopsClick: () -> Unit,
    onEffect: (RoutePlannerUiEffects) -> Unit,
    onLocationIqClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMinimizedHeightMeasured: (Dp) -> Unit = {},
    onExpandedHeightMeasured: (Dp) -> Unit = {},
    viewModel: RoutePlannerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSaveError by remember { mutableStateOf(false) }

    LaunchedEffect(place) {
        place?.let { viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(it)) }
    }
    LaunchedEffect(pick) {
        pick?.let { viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(it.location)) }
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
        onMoreStopsClick = onMoreStopsClick,
        onMinimizedHeightMeasured = onMinimizedHeightMeasured,
        onExpandedHeightMeasured = onExpandedHeightMeasured,
        modifier = modifier,
    )
    if (uiState.isWaypointSearchVisible) {
        RoutePlannerSearchBottomSheet(
            myLocation = uiState.myLocation,
            onEvent = viewModel::onEvent,
            onLocationIqClick = onLocationIqClick,
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
