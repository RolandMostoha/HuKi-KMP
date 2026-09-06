package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderUiEvents
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderUiState
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.ui.components.SearchField
import hu.mostoha.mobile.kmp.huki.ui.components.SecondaryButton
import hu.mostoha.mobile.kmp.huki.ui.features.search.DestinationsSection
import hu.mostoha.mobile.kmp.huki.ui.features.search.OnlineResultsSection
import hu.mostoha.mobile.kmp.huki.ui.features.search.RecentPlacesSection
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerSearchBottomSheet(
    myLocation: Location?,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onLocationIqClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceFinderViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(RoutePlannerUiEvents.WaypointSearchDismissed) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = null,
        modifier = modifier,
    ) {
        RoutePlannerSearchContent(
            uiState = uiState,
            myLocation = myLocation,
            onEvent = onEvent,
            onPlaceFinderEvent = viewModel::onEvent,
            onLocationIqClick = onLocationIqClick,
        )
    }
}

@Composable
private fun RoutePlannerSearchContent(
    uiState: PlaceFinderUiState,
    myLocation: Location?,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onPlaceFinderEvent: (PlaceFinderUiEvents) -> Unit,
    onLocationIqClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val navBarInset = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val bottomPadding = Dimens.ExtraLarge + navBarInset

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTagAsResourceId(TestTags.ROUTE_PLANNER_SEARCH_SCREEN),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ExtraLarge, top = Dimens.Large, end = Dimens.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        ) {
            Text(
                text = mokoString(SharedRes.strings.route_planner_add_stop_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { onEvent(RoutePlannerUiEvents.WaypointSearchDismissed) },
                modifier = Modifier.testTagAsResourceId(TestTags.ROUTE_PLANNER_SEARCH_CLOSE_BUTTON),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = mokoString(SharedRes.strings.a11y_close),
                )
            }
        }
        SearchField(
            value = uiState.searchText,
            onValueChange = { onPlaceFinderEvent(PlaceFinderUiEvents.SearchTextChanged(it)) },
            focusRequester = focusRequester,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Large, vertical = Dimens.SmallMedium)
                .testTagAsResourceId(TestTags.ROUTE_PLANNER_SEARCH_FIELD),
        )
        RoutePlannerSearchResults(
            uiState = uiState,
            myLocation = myLocation,
            bottomPadding = bottomPadding,
            onEvent = onEvent,
            onPlaceFinderEvent = onPlaceFinderEvent,
            onLocationIqClick = onLocationIqClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun RoutePlannerSearchResults(
    uiState: PlaceFinderUiState,
    myLocation: Location?,
    bottomPadding: Dp,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    onPlaceFinderEvent: (PlaceFinderUiEvents) -> Unit,
    onLocationIqClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSearching = uiState.searchText.isNotEmpty()
    Column(modifier = modifier) {
        if (uiState.searchRecentPlaces.isNotEmpty()) {
            RecentPlacesSection(
                places = uiState.searchRecentPlaces,
                onPlaceSelected = { onEvent(RoutePlannerUiEvents.SearchPlaceAdded(it)) },
            )
        } else if (!isSearching && uiState.recentPlaces.isNotEmpty()) {
            RecentPlacesSection(
                places = uiState.recentPlaces,
                onPlaceSelected = { onEvent(RoutePlannerUiEvents.SearchPlaceAdded(it)) },
            )
        }
        if (uiState.searchDestinations.isNotEmpty()) {
            DestinationsSection(
                destinations = uiState.searchDestinations,
                onDestinationSelected = { onEvent(RoutePlannerUiEvents.SearchDestinationAdded(it)) },
            )
        } else if (!isSearching && uiState.destinations.isNotEmpty()) {
            DestinationsSection(
                destinations = uiState.destinations,
                onDestinationSelected = { onEvent(RoutePlannerUiEvents.SearchDestinationAdded(it)) },
                title = mokoString(uiState.destinationsTitle),
            )
        }
        if (isSearching) {
            OnlineResultsSection(
                places = uiState.places,
                error = uiState.error,
                isLoading = uiState.isLoading,
                onPlaceSelected = { onEvent(RoutePlannerUiEvents.SearchPlaceAdded(it)) },
                onRetryClicked = { onPlaceFinderEvent(PlaceFinderUiEvents.RetryClicked) },
                onLocationIqClicked = onLocationIqClick,
            )
        } else {
            RoutePlannerSearchActions(
                myLocation = myLocation,
                onEvent = onEvent,
                modifier = Modifier.padding(
                    start = Dimens.Large,
                    top = Dimens.Huge,
                    end = Dimens.Large,
                    bottom = Dimens.SectionSpacing,
                ),
            )
        }
        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

@Composable
private fun RoutePlannerSearchActions(
    myLocation: Location?,
    onEvent: (RoutePlannerUiEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        SecondaryButton(
            iconResId = R.drawable.ic_my_location,
            text = mokoString(SharedRes.strings.route_planner_pick_my_location),
            onClick = { onEvent(RoutePlannerUiEvents.MyLocationAdded) },
            enabled = myLocation != null,
            modifier = Modifier
                .fillMaxWidth()
                .testTagAsResourceId(TestTags.ROUTE_PLANNER_SEARCH_MY_LOCATION_BUTTON),
        )
        SecondaryButton(
            iconResId = R.drawable.ic_touch_long,
            text = mokoString(SharedRes.strings.route_planner_pick_on_map),
            onClick = { onEvent(RoutePlannerUiEvents.PickOnMapClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .testTagAsResourceId(TestTags.ROUTE_PLANNER_SEARCH_PICK_ON_MAP_BUTTON),
        )
    }
}
