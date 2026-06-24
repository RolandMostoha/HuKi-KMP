package hu.mostoha.mobile.kmp.huki.ui.features.placehistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.placehistory.PlaceHistoryUiEffects
import hu.mostoha.mobile.kmp.huki.features.placehistory.PlaceHistoryUiEvents
import hu.mostoha.mobile.kmp.huki.features.placehistory.PlaceHistoryUiState
import hu.mostoha.mobile.kmp.huki.features.placehistory.PlaceHistoryViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryHeader
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistorySection
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.ui.components.PlaceRow
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun PlaceHistoryScreen(
    onBack: () -> Unit,
    onOpenPlace: (OsmType, String) -> Unit,
    viewModel: PlaceHistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlaceHistoryContent(
        uiState = uiState,
        uiEffects = viewModel.uiEffects,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenPlace = onOpenPlace,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlaceHistoryContent(
    uiState: PlaceHistoryUiState,
    uiEffects: Flow<PlaceHistoryUiEffects>,
    onEvent: (PlaceHistoryUiEvents) -> Unit,
    onBack: () -> Unit,
    onOpenPlace: (OsmType, String) -> Unit,
) {
    LaunchedEffect(uiEffects) {
        uiEffects.collect { effect ->
            when (effect) {
                PlaceHistoryUiEffects.NavigateBack -> onBack()
                is PlaceHistoryUiEffects.OpenPlace -> onOpenPlace(effect.osmType, effect.osmId)
            }
        }
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val barColor = MaterialTheme.colorScheme.surfaceVariant
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag(TestTags.PLACE_HISTORY_SCREEN_ROOT),
        containerColor = barColor,
        topBar = {
            val subtitle = when (uiState.placeCount) {
                0 -> mokoString(SharedRes.strings.place_history_subtitle_empty)
                1 -> mokoString(SharedRes.strings.place_history_subtitle_single, uiState.placeCount)
                else -> mokoString(SharedRes.strings.place_history_subtitle_pattern, uiState.placeCount)
            }
            MediumFlexibleTopAppBar(
                title = { Text(text = mokoString(SharedRes.strings.place_history_title)) },
                subtitle = { Text(text = subtitle) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.PLACE_HISTORY_BACK_BUTTON),
                        onClick = { onEvent(PlaceHistoryUiEvents.BackClicked) },
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.place_history_a11y_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barColor,
                    scrolledContainerColor = barColor,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        if (!uiState.isLoading && uiState.sections.isEmpty()) {
            PlaceHistoryEmptyView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag(TestTags.PLACE_HISTORY_LIST),
                contentPadding = PaddingValues(
                    start = Dimens.Large,
                    end = Dimens.Large,
                    top = Dimens.Small,
                    bottom = Dimens.ExtraLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Small),
            ) {
                uiState.sections.forEach { section ->
                    item(key = section.header.toString()) {
                        PlaceHistorySectionHeader(header = section.header)
                    }
                    item(key = "card_${section.header}") {
                        PlaceHistorySectionCard(
                            section = section,
                            onPlaceClick = { place ->
                                onEvent(PlaceHistoryUiEvents.PlaceClicked(place))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceHistoryEmptyView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = Dimens.Large),
        contentAlignment = Alignment.Center,
    ) {
        InfoView(
            infoViewData = InfoViewData(
                infoViewType = InfoViewType.INFO,
                icon = ImageResource(R.drawable.ic_place_circle),
                title = SharedRes.strings.place_history_empty_title,
                message = SharedRes.strings.place_history_empty_message,
            ),
            modifier = Modifier.testTag(TestTags.PLACE_HISTORY_EMPTY_VIEW),
        )
    }
}

@Composable
private fun PlaceHistorySectionHeader(header: PlaceHistoryHeader) {
    val text = when (header) {
        PlaceHistoryHeader.Today -> mokoString(SharedRes.strings.place_history_date_today)
        PlaceHistoryHeader.Yesterday -> mokoString(SharedRes.strings.place_history_date_yesterday)
        is PlaceHistoryHeader.Date -> header.label
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            top = Dimens.Medium,
            bottom = Dimens.Small,
        ),
    )
}

@Composable
private fun PlaceHistorySectionCard(section: PlaceHistorySection, onPlaceClick: (Place) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            section.items.forEachIndexed { index, item ->
                PlaceRow(
                    place = item.place,
                    onClick = { onPlaceClick(item.place) },
                    modifier = Modifier.testTag(TestTags.PLACE_HISTORY_ITEM),
                )
                if (index != section.items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Dimens.Large + Dimens.IconContainer + Dimens.MediumLarge),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.dividerColor,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlaceHistoryContentPreview() {
    val now = Clock.System.now()
    val dobogoko = PlaceHistoryItem(
        place = Place(
            osmId = "1",
            name = "Dobogókő",
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
            address = "Pilis Mountains, Hungary",
            location = Location(47.7181, 18.8948),
            placeCategory = PlaceCategory.PEAK,
            osmType = OsmType.NODE,
        ),
        lastVisited = now,
    )
    val balaton = PlaceHistoryItem(
        place = Place(
            osmId = "2",
            name = "Balaton",
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
            address = "Lake, Transdanubia, Hungary",
            location = Location(46.83, 17.73),
            placeCategory = PlaceCategory.LAKE,
            osmType = OsmType.RELATION,
        ),
        lastVisited = now,
    )
    val castle = PlaceHistoryItem(
        place = Place(
            osmId = "3",
            name = "Visegrádi vár",
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
            address = "Castle, Visegrád",
            location = Location(47.79, 18.97),
            placeCategory = PlaceCategory.CASTLE,
            osmType = OsmType.WAY,
        ),
        lastVisited = now.minus(2.days),
    )
    HuKiTheme {
        PlaceHistoryContent(
            uiState = PlaceHistoryUiState(
                isLoading = false,
                placeCount = 3,
                sections = listOf(
                    PlaceHistorySection(PlaceHistoryHeader.Today, listOf(dobogoko, balaton)),
                    PlaceHistorySection(PlaceHistoryHeader.Date("2026.04.10"), listOf(castle)),
                ),
            ),
            uiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
            onOpenPlace = { _, _ -> },
        )
    }
}

@Preview(name = "Empty")
@Composable
private fun PlaceHistoryEmptyPreview() {
    HuKiTheme {
        PlaceHistoryContent(
            uiState = PlaceHistoryUiState(isLoading = false),
            uiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
            onOpenPlace = { _, _ -> },
        )
    }
}
