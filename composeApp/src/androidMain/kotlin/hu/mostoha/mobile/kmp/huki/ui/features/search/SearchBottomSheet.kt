package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.network.toInfoViewData
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.resolveMoko
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchBottomSheet(
    onCloseClick: () -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onDestinationSelected: (Destination) -> Unit,
    onLocationIqClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceFinderViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchBottomSheetContent(
        modifier = modifier,
        uiState = uiState,
        onCloseClick = onCloseClick,
        onPlaceSelected = onPlaceSelected,
        onDestinationSelected = onDestinationSelected,
        onLocationIqClicked = onLocationIqClicked,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun SearchBottomSheetContent(
    uiState: PlaceFinderUiState,
    onEvent: (PlaceFinderUiEvents) -> Unit,
    onCloseClick: () -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onDestinationSelected: (Destination) -> Unit,
    onLocationIqClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val errorInfo = uiState.error
    val navigationBarBottomPadding = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                OutlinedTextField(
                    value = uiState.searchText,
                    onValueChange = { onEvent(PlaceFinderUiEvents.SearchTextChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(32.dp),
                            clip = false,
                        )
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(text = mokoString(SharedRes.strings.search_input_placeholder))
                    },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.padding(start = Dimens.Small),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchText.isNotEmpty()) {
                            IconButton(
                                modifier = Modifier.padding(end = Dimens.Small),
                                onClick = { onEvent(PlaceFinderUiEvents.SearchTextChanged("")) },
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_clear),
                                    contentDescription = mokoString(SharedRes.strings.a11y_clear_text),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = KeyboardType.Text,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Small))
            val isSearchActive = uiState.isLoading || uiState.places.isNotEmpty() || errorInfo != null
            if (isSearchActive) {
                Row(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .padding(
                            start = Dimens.Large,
                            end = Dimens.Large,
                            bottom = Dimens.Small,
                        ),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.isLoading) {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.MediumLarge))
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onLocationIqClicked)
                            .semantics(mergeDescendants = true) {
                                contentDescription = context.resolveMoko(
                                    SharedRes.strings.settings_a11y_open_location_iq,
                                )
                            }
                            .padding(Dimens.ExtraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mokoString(SharedRes.strings.search_powered_by),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(Dimens.Small))
                        Icon(
                            painter = painterResource(id = SharedRes.images.ic_location_iq_logo.drawableResId),
                            contentDescription = null,
                            modifier = Modifier.height(18.dp),
                            tint = Color.Unspecified,
                        )
                    }
                }
            }
            if (uiState.places.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = Dimens.Large,
                        top = Dimens.Small,
                        end = Dimens.Large,
                        bottom = Dimens.ExtraLarge + navigationBarBottomPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
                ) {
                    itemsIndexed(
                        items = uiState.places,
                        key = { index, place -> "${place.id}-$index" },
                    ) { _, place ->
                        SearchResultItem(
                            place = place,
                            onClick = { onPlaceSelected(place) },
                        )
                    }
                }
            } else if (errorInfo != null) {
                InfoView(
                    infoViewData = errorInfo,
                    primaryActionText = mokoString(SharedRes.strings.search_error_retry),
                    onPrimaryActionClick = {
                        onEvent(PlaceFinderUiEvents.RetryClicked)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = Dimens.Huge,
                            start = Dimens.Large,
                            end = Dimens.Large,
                            bottom = Dimens.ExtraLarge + navigationBarBottomPadding,
                        ),
                )
            } else if (uiState.searchText.isEmpty() && uiState.topDestinations.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    DestinationsSection(
                        destinations = uiState.topDestinations,
                        onDestinationSelected = onDestinationSelected,
                    )
                    Spacer(modifier = Modifier.height(Dimens.ExtraLarge + navigationBarBottomPadding))
                }
            } else {
                Spacer(modifier = Modifier.weight(1f, fill = true))
            }
        }
    }
}

@Preview(name = "Search Results")
@Composable
private fun SearchBottomSheetContentPreview() {
    HuKiTheme {
        SearchBottomSheetContent(
            uiState = PlaceFinderUiState(
                searchText = "Arden",
                places = listOf(
                    Place(
                        id = "1",
                        title = "Mt. Arden Summit",
                        subtitle = "Arden Valley, Bern, Switzerland",
                        location = Location(47.3769, 8.5417),
                    ),
                    Place(
                        id = "2",
                        title = "Arden Ridge Viewpoint",
                        subtitle = "2,104 m · Bernese Alps",
                        location = Location(47.4979, 19.0402),
                    ),
                    Place(
                        id = "3",
                        title = "Ardennes Nat. Park",
                        subtitle = null,
                        location = Location(48.2082, 16.3738),
                    ),
                ),
            ),
            onCloseClick = {},
            onPlaceSelected = {},
            onDestinationSelected = {},
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
            onCloseClick = {},
            onPlaceSelected = {},
            onDestinationSelected = {},
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
                topDestinations = listOf(
                    Destination(
                        osmId = "1",
                        name = "Dobogókő",
                        town = "Pilisszentkereszt",
                        type = DestinationType.PEAK,
                        location = Location(47.7181, 18.8948),
                        description = SharedRes.strings.destinations_section_title,
                        popularity = 10,
                    ),
                    Destination(
                        osmId = "2",
                        name = "Balaton",
                        town = "Siófok",
                        type = DestinationType.LAKE,
                        location = Location(46.83, 17.73),
                        description = SharedRes.strings.destinations_section_title,
                        popularity = 9,
                    ),
                ),
            ),
            onCloseClick = {},
            onPlaceSelected = {},
            onDestinationSelected = {},
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
            onCloseClick = {},
            onPlaceSelected = {},
            onDestinationSelected = {},
            onLocationIqClicked = {},
            onEvent = {},
        )
    }
}
