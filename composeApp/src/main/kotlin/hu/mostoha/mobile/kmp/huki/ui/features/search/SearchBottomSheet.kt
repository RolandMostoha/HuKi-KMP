package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderUiEvents
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderUiState
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoViewData
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.SearchField
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants.SEARCH_CONTENT_FADE_IN_DURATION
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants.SEARCH_CONTENT_FADE_OUT_DURATION
import hu.mostoha.mobile.kmp.huki.util.millis
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchBottomSheet(
    isSheetExpanded: Boolean,
    onCloseClick: () -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onRecentPlaceSelected: (Place) -> Unit,
    onSearchPlaceHistorySelected: (Place) -> Unit,
    onDestinationSelected: (Destination) -> Unit,
    onSearchDestinationSelected: (Destination) -> Unit,
    onGpxFileSelected: (String) -> Unit,
    onSeeAllGpxClicked: () -> Unit,
    onSeeAllPlacesClicked: () -> Unit,
    onSeeAllDestinationsClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceFinderViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showContent by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isSheetExpanded) {
        if (isSheetExpanded) showContent = true
    }

    SearchBottomSheetContent(
        modifier = modifier,
        uiState = uiState,
        showContent = showContent,
        onCloseClick = onCloseClick,
        onPlaceSelected = onPlaceSelected,
        onRecentPlaceSelected = onRecentPlaceSelected,
        onSearchPlaceHistorySelected = onSearchPlaceHistorySelected,
        onDestinationSelected = onDestinationSelected,
        onSearchDestinationSelected = onSearchDestinationSelected,
        onGpxFileSelected = onGpxFileSelected,
        onSeeAllGpxClicked = onSeeAllGpxClicked,
        onSeeAllPlacesClicked = onSeeAllPlacesClicked,
        onSeeAllDestinationsClicked = onSeeAllDestinationsClicked,
        onLocationIqClicked = onLocationIqClicked,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun SearchBottomSheetContent(
    uiState: PlaceFinderUiState,
    showContent: Boolean,
    onEvent: (PlaceFinderUiEvents) -> Unit,
    onCloseClick: () -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onRecentPlaceSelected: (Place) -> Unit,
    onSearchPlaceHistorySelected: (Place) -> Unit,
    onDestinationSelected: (Destination) -> Unit,
    onSearchDestinationSelected: (Destination) -> Unit,
    onGpxFileSelected: (String) -> Unit,
    onSeeAllGpxClicked: () -> Unit,
    onSeeAllPlacesClicked: () -> Unit,
    onSeeAllDestinationsClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val navigationBarBottomPadding = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }

    LaunchedEffect(showContent) {
        if (showContent) focusRequester.requestFocus()
    }

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(
            topStart = Dimens.ExtraLarge,
            topEnd = Dimens.ExtraLarge,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.Small,
                        top = Dimens.Medium,
                        end = Dimens.Large,
                        bottom = Dimens.Medium,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
            ) {
                IconButton(
                    onClick = onCloseClick,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                        contentDescription = mokoString(SharedRes.strings.a11y_close),
                    )
                }
                SearchField(
                    value = uiState.searchText,
                    onValueChange = { onEvent(PlaceFinderUiEvents.SearchTextChanged(it)) },
                    focusRequester = focusRequester,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Small))
            val bottomPadding = Dimens.ExtraLarge + navigationBarBottomPadding
            val contentType = if (showContent) uiState.toContentType() else SearchContentType.EMPTY
            AnimatedContent(
                targetState = contentType to uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transitionSpec = {
                    fadeIn(
                        tween(
                            durationMillis = SEARCH_CONTENT_FADE_IN_DURATION.millis(),
                            delayMillis = SEARCH_CONTENT_FADE_OUT_DURATION.millis(),
                        ),
                    ) togetherWith fadeOut(tween(SEARCH_CONTENT_FADE_OUT_DURATION.millis()))
                },
                contentKey = { (type, _) -> type },
                label = "searchContent",
            ) { (targetContentType, contentUiState) ->
                val scrollModifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                when (targetContentType) {
                    SearchContentType.RESULTS -> SearchResultsContent(
                        uiState = contentUiState,
                        onPlaceSelected = onPlaceSelected,
                        onSearchPlaceHistorySelected = onSearchPlaceHistorySelected,
                        onSearchDestinationSelected = onSearchDestinationSelected,
                        onRetryClicked = { onEvent(PlaceFinderUiEvents.RetryClicked) },
                        onLocationIqClicked = onLocationIqClicked,
                        bottomPadding = bottomPadding,
                        modifier = scrollModifier,
                    )
                    SearchContentType.DISCOVERY -> SearchDiscoveryContent(
                        uiState = contentUiState,
                        onRecentPlaceSelected = onRecentPlaceSelected,
                        onDestinationSelected = onDestinationSelected,
                        onGpxFileSelected = onGpxFileSelected,
                        onSeeAllGpxClicked = onSeeAllGpxClicked,
                        onSeeAllPlacesClicked = onSeeAllPlacesClicked,
                        onSeeAllDestinationsClicked = onSeeAllDestinationsClicked,
                        bottomPadding = bottomPadding,
                        modifier = scrollModifier,
                    )
                    SearchContentType.EMPTY -> Spacer(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

private enum class SearchContentType { EMPTY, DISCOVERY, RESULTS }

private fun PlaceFinderUiState.toContentType(): SearchContentType {
    val isSearching = searchText.isNotEmpty()
    val hasResults = searchRecentPlaces.isNotEmpty() ||
        searchDestinations.isNotEmpty() ||
        places.isNotEmpty() ||
        isLoading ||
        error != null
    val hasDiscoveryContent = destinations.isNotEmpty() ||
        recentPlaces.isNotEmpty() ||
        recentGpxFiles.isNotEmpty()
    return when {
        isSearching && hasResults -> SearchContentType.RESULTS
        !isSearching && hasDiscoveryContent -> SearchContentType.DISCOVERY
        else -> SearchContentType.EMPTY
    }
}

@Composable
private fun SearchResultsContent(
    uiState: PlaceFinderUiState,
    onPlaceSelected: (Place) -> Unit,
    onSearchPlaceHistorySelected: (Place) -> Unit,
    onSearchDestinationSelected: (Destination) -> Unit,
    onRetryClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (uiState.searchRecentPlaces.isNotEmpty()) {
            RecentPlacesSection(
                places = uiState.searchRecentPlaces,
                onPlaceSelected = onSearchPlaceHistorySelected,
            )
        }
        if (uiState.searchDestinations.isNotEmpty()) {
            DestinationsSection(
                destinations = uiState.searchDestinations,
                onDestinationSelected = onSearchDestinationSelected,
            )
        }
        OnlineResultsSection(
            places = uiState.places,
            error = uiState.error,
            isLoading = uiState.isLoading,
            onPlaceSelected = onPlaceSelected,
            onRetryClicked = onRetryClicked,
            onLocationIqClicked = onLocationIqClicked,
        )
        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

@Composable
private fun SearchDiscoveryContent(
    uiState: PlaceFinderUiState,
    onRecentPlaceSelected: (Place) -> Unit,
    onDestinationSelected: (Destination) -> Unit,
    onGpxFileSelected: (String) -> Unit,
    onSeeAllGpxClicked: () -> Unit,
    onSeeAllPlacesClicked: () -> Unit,
    onSeeAllDestinationsClicked: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (uiState.recentPlaces.isNotEmpty()) {
            RecentPlacesSection(
                places = uiState.recentPlaces,
                onPlaceSelected = onRecentPlaceSelected,
                onSeeAllClicked = onSeeAllPlacesClicked,
            )
        }
        if (uiState.recentGpxFiles.isNotEmpty()) {
            RecentGpxSection(
                files = uiState.recentGpxFiles,
                onFileSelected = { onGpxFileSelected(it.fileUri) },
                onSeeAllClicked = onSeeAllGpxClicked,
            )
        }
        if (uiState.destinations.isNotEmpty()) {
            DestinationsSection(
                destinations = uiState.destinations,
                onDestinationSelected = onDestinationSelected,
                title = mokoString(uiState.destinationsTitle),
                onSeeAllClick = onSeeAllDestinationsClicked,
            )
        }
        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

@Preview(name = "Search Results")
@Composable
private fun SearchBottomSheetContentPreview() {
    HuKiTheme {
        SearchBottomSheetContent(
            uiState = PlaceFinderUiState(
                searchText = "Dob",
                searchRecentPlaces = listOf(
                    Place(
                        osmId = "r1",
                        name = "Dobogókő",
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        address = "Pilis Mountains, Hungary",
                        location = Location(47.7181, 18.8948),
                    ),
                ),
                searchDestinations = listOf(
                    Destination(
                        id = "d1",
                        name = "Dobogókő",
                        town = "Pilisszentkereszt",
                        type = DestinationType.PEAK,
                        location = Location(47.7181, 18.8948),
                        description = SharedRes.strings.destinations_section_title,
                        popularity = 10,
                    ),
                ),
                places = listOf(
                    Place(
                        osmId = "1",
                        name = "Mt. Arden Summit",
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        address = "Arden Valley, Bern, Switzerland",
                        location = Location(47.3769, 8.5417),
                    ),
                    Place(
                        osmId = "2",
                        name = "Arden Ridge Viewpoint",
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        address = "2,104 m · Bernese Alps",
                        location = Location(47.4979, 19.0402),
                    ),
                    Place(
                        osmId = "3",
                        name = "Ardennes Nat. Park",
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        address = null,
                        location = Location(48.2082, 16.3738),
                    ),
                ),
            ),
            showContent = true,
            onCloseClick = {},
            onPlaceSelected = {},
            onRecentPlaceSelected = {},
            onSearchPlaceHistorySelected = {},
            onDestinationSelected = {},
            onSearchDestinationSelected = {},
            onGpxFileSelected = {},
            onSeeAllGpxClicked = {},
            onSeeAllPlacesClicked = {},
            onSeeAllDestinationsClicked = {},
            onLocationIqClicked = {},
            onEvent = {},
        )
    }
}

@Preview(name = "Error")
@Composable
private fun SearchBottomSheetErrorStatePreview() {
    HuKiTheme {
        SearchBottomSheetContent(
            uiState = PlaceFinderUiState(
                searchText = "Arden",
                error = NetworkError.NO_INTERNET.toInfoViewData(),
            ),
            showContent = true,
            onCloseClick = {},
            onPlaceSelected = {},
            onRecentPlaceSelected = {},
            onSearchPlaceHistorySelected = {},
            onDestinationSelected = {},
            onSearchDestinationSelected = {},
            onGpxFileSelected = {},
            onSeeAllGpxClicked = {},
            onSeeAllPlacesClicked = {},
            onSeeAllDestinationsClicked = {},
            onLocationIqClicked = {},
            onEvent = {},
        )
    }
}

@Preview(name = "Destinations")
@Composable
private fun SearchBottomSheetDestinationsStatePreview() {
    HuKiTheme {
        SearchBottomSheetContent(
            uiState = PlaceFinderUiState(
                destinations = listOf(
                    Destination(
                        id = "1",
                        name = "Dobogókő",
                        town = "Pilisszentkereszt",
                        type = DestinationType.PEAK,
                        location = Location(47.7181, 18.8948),
                        description = SharedRes.strings.destinations_section_title,
                        popularity = 10,
                    ),
                    Destination(
                        id = "2",
                        name = "Balaton",
                        town = "Siófok",
                        type = DestinationType.LAKE,
                        location = Location(46.83, 17.73),
                        description = SharedRes.strings.destinations_section_title,
                        popularity = 9,
                    ),
                ),
                recentPlaces = listOf(
                    Place(
                        osmId = "10",
                        name = "Dobogókő",
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        address = "Pilis Mountains, Hungary",
                        location = Location(47.7181, 18.8948),
                    ),
                ),
            ),
            showContent = true,
            onCloseClick = {},
            onPlaceSelected = {},
            onRecentPlaceSelected = {},
            onSearchPlaceHistorySelected = {},
            onDestinationSelected = {},
            onSearchDestinationSelected = {},
            onGpxFileSelected = {},
            onSeeAllGpxClicked = {},
            onSeeAllPlacesClicked = {},
            onSeeAllDestinationsClicked = {},
            onLocationIqClicked = {},
            onEvent = {},
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun SearchBottomSheetLoadingStatePreview() {
    HuKiTheme {
        SearchBottomSheetContent(
            uiState = PlaceFinderUiState(
                searchText = "Arden",
                isLoading = true,
            ),
            showContent = true,
            onCloseClick = {},
            onPlaceSelected = {},
            onRecentPlaceSelected = {},
            onSearchPlaceHistorySelected = {},
            onDestinationSelected = {},
            onSearchDestinationSelected = {},
            onGpxFileSelected = {},
            onSeeAllGpxClicked = {},
            onSeeAllPlacesClicked = {},
            onSeeAllDestinationsClicked = {},
            onLocationIqClicked = {},
            onEvent = {},
        )
    }
}
