package hu.mostoha.mobile.kmp.huki.ui.features.destinations

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.Strings
import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsTab
import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsUiEffects
import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsUiEvents
import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsUiState
import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsViewModel
import hu.mostoha.mobile.kmp.huki.features.destinations.NearbyState
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationListItem
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.navigateToAppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DestinationsScreen(
    onBack: () -> Unit,
    onShowOnMap: (Destination) -> Unit,
    viewModel: DestinationsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BindEffect(viewModel.permissionsController)
    DestinationsContent(
        uiState = uiState,
        uiEffects = viewModel.uiEffects,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onShowOnMap = onShowOnMap,
        distanceText = viewModel::distanceText,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DestinationsContent(
    uiState: DestinationsUiState,
    uiEffects: Flow<DestinationsUiEffects>,
    onEvent: (DestinationsUiEvents) -> Unit,
    onBack: () -> Unit,
    onShowOnMap: (Destination) -> Unit = {},
    distanceText: (Destination) -> String? = { null },
) {
    val context = LocalContext.current
    var preview by remember { mutableStateOf<DestinationPreview?>(null) }
    val sheetState = rememberModalBottomSheetState()
    LaunchedEffect(uiEffects) {
        uiEffects.collect { effect ->
            when (effect) {
                DestinationsUiEffects.NavigateBack -> onBack()
                DestinationsUiEffects.NavigateToAppSettings -> context.navigateToAppSettings()
            }
        }
    }
    val onDestinationClick: (Destination, String?, String?) -> Unit = { destination, landscapeText, distance ->
        preview = DestinationPreview(destination, landscapeText, distance)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val barColor = MaterialTheme.colorScheme.surfaceVariant
    val sortedLandscapes = remember(uiState.landscapes, context) {
        val strings = Strings(context)
        uiState.landscapes.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { strings.get(it.nameRes) })
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag(TestTags.DESTINATIONS_SCREEN_ROOT),
        containerColor = barColor,
        topBar = {
            val subtitle = when (uiState.destinationCount) {
                1 -> mokoString(SharedRes.strings.destinations_subtitle_single, uiState.destinationCount)
                else -> mokoString(SharedRes.strings.destinations_subtitle_pattern, uiState.destinationCount)
            }
            MediumFlexibleTopAppBar(
                title = { Text(text = mokoString(SharedRes.strings.destinations_title)) },
                subtitle = { Text(text = subtitle) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.DESTINATIONS_BACK_BUTTON),
                        onClick = { onEvent(DestinationsUiEvents.BackClicked) },
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.destinations_a11y_back),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.DESTINATIONS_LIST),
            contentPadding = PaddingValues(
                start = Dimens.Large,
                end = Dimens.Large,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + Dimens.ExtraLarge,
            ),
        ) {
            item(key = "tab_bar") {
                DestinationsTabBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { onEvent(DestinationsUiEvents.TabSelected(it)) },
                    modifier = Modifier.padding(vertical = Dimens.Small),
                )
            }
            when (uiState.selectedTab) {
                DestinationsTab.POPULAR -> {
                    topSpacing()
                    popularContent(uiState.popularItems, context, distanceText, onDestinationClick)
                }
                DestinationsTab.LANDSCAPES -> landscapesContent(
                    sortedLandscapes,
                    context,
                    distanceText,
                    onDestinationClick,
                )
                DestinationsTab.NEARBY -> {
                    topSpacing()
                    nearbyContent(uiState.nearbyState, onEvent, onDestinationClick)
                }
            }
        }
    }
    preview?.let { current ->
        DestinationPreviewBottomSheet(
            destination = current.destination,
            sheetState = sheetState,
            landscapeText = current.landscapeText,
            distanceText = current.distanceText,
            onShowOnMapClick = {
                onShowOnMap(current.destination)
                preview = null
            },
            onDismissRequest = { preview = null },
        )
    }
}

private data class DestinationPreview(
    val destination: Destination,
    val landscapeText: String?,
    val distanceText: String?,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DestinationsTabBar(
    selectedTab: DestinationsTab,
    onTabSelected: (DestinationsTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = DestinationsTab.entries
    val colors = ToggleButtonDefaults.toggleButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.DESTINATIONS_TAB_BAR),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        tabs.forEachIndexed { index, tab ->
            val shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }
            ToggleButton(
                checked = selectedTab == tab,
                onCheckedChange = { onTabSelected(tab) },
                shapes = shapes,
                colors = colors,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = mokoString(tab.title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun LazyListScope.topSpacing() {
    item(key = "top_spacing") {
        Spacer(modifier = Modifier.height(Dimens.Large))
    }
}

private fun LazyListScope.popularContent(
    items: List<DestinationListItem>,
    context: Context,
    distanceText: (Destination) -> String?,
    onDestinationClick: (Destination, String?, String?) -> Unit,
) {
    val strings = Strings(context)
    destinationCardItems(
        count = items.size,
        key = { "popular_$it" },
    ) { index ->
        val item = items[index]
        val landscapeText = item.landscape?.let { strings.get(it.nameRes) }
        DestinationRow(
            destination = item.destination,
            onClick = { onDestinationClick(item.destination, landscapeText, distanceText(item.destination)) },
            rank = index + 1,
            landscapeText = landscapeText,
        )
    }
}

private fun LazyListScope.landscapesContent(
    landscapes: List<Landscape>,
    context: Context,
    distanceText: (Destination) -> String?,
    onDestinationClick: (Destination, String?, String?) -> Unit,
) {
    val strings = Strings(context)
    landscapes.forEach { landscape ->
        item(key = "landscape_header_${landscape.osmId}") {
            DestinationsSectionHeader(text = strings.get(landscape.nameRes))
        }
        val landscapeText = strings.get(landscape.nameRes)
        destinationCardItems(
            count = landscape.destinations.size,
            key = { "${landscape.osmId}_$it" },
        ) { index ->
            val destination = landscape.destinations[index]
            DestinationRow(
                destination = destination,
                onClick = { onDestinationClick(destination, landscapeText, distanceText(destination)) },
            )
        }
    }
}

private fun LazyListScope.nearbyContent(
    nearbyState: NearbyState,
    onEvent: (DestinationsUiEvents) -> Unit,
    onDestinationClick: (Destination, String?, String?) -> Unit,
) {
    when (nearbyState) {
        NearbyState.Loading -> item(key = "nearby_loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.Huge),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        NearbyState.PermissionRequired -> item(key = "nearby_permission") {
            NearbyInfoView(
                infoViewType = InfoViewType.WARNING,
                title = SharedRes.strings.destinations_nearby_permission_title,
                message = SharedRes.strings.destinations_nearby_permission_message,
                actionText = mokoString(SharedRes.strings.destinations_nearby_permission_action),
                onActionClick = { onEvent(DestinationsUiEvents.GrantLocationClicked) },
                testTag = TestTags.DESTINATIONS_NEARBY_PERMISSION_VIEW,
            )
        }
        NearbyState.LocationUnavailable -> item(key = "nearby_error") {
            NearbyInfoView(
                infoViewType = InfoViewType.ERROR,
                title = SharedRes.strings.destinations_nearby_error_title,
                message = SharedRes.strings.destinations_nearby_error_message,
                actionText = mokoString(SharedRes.strings.destinations_nearby_error_action),
                onActionClick = { onEvent(DestinationsUiEvents.RetryNearbyClicked) },
                testTag = TestTags.DESTINATIONS_NEARBY_ERROR_VIEW,
            )
        }
        is NearbyState.Loaded -> destinationCardItems(
            count = nearbyState.items.size,
            key = { "nearby_$it" },
        ) { index ->
            val item = nearbyState.items[index]
            DestinationRow(
                destination = item.destination,
                onClick = { onDestinationClick(item.destination, null, item.distance) },
                distanceText = item.distance,
            )
        }
    }
}

@Composable
private fun NearbyInfoView(
    infoViewType: InfoViewType,
    title: StringResource,
    message: StringResource,
    actionText: String,
    onActionClick: () -> Unit,
    testTag: String,
) {
    InfoView(
        infoViewData = InfoViewData(
            infoViewType = infoViewType,
            icon = ImageResource(R.drawable.ic_location_off),
            title = title,
            message = message,
        ),
        primaryActionText = actionText,
        onPrimaryActionClick = onActionClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.ExtraLarge)
            .testTag(testTag),
    )
}

@Composable
private fun DestinationsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = Dimens.Large, bottom = Dimens.Small),
    )
}

/**
 * Emits each row as its own lazy item so only visible rows compose, while preserving the
 * contiguous rounded-card look by rounding the first/last item's outer corners.
 */
private fun LazyListScope.destinationCardItems(
    count: Int,
    key: (index: Int) -> Any,
    row: @Composable (index: Int) -> Unit,
) {
    items(count = count, key = { key(it) }) { index ->
        val shape = when {
            count == 1 -> RoundedCornerShape(Dimens.Large)
            index == 0 -> RoundedCornerShape(topStart = Dimens.Large, topEnd = Dimens.Large)
            index == count - 1 -> RoundedCornerShape(bottomStart = Dimens.Large, bottomEnd = Dimens.Large)
            else -> RectangleShape
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                row(index)
                RowDivider(isLast = index == count - 1)
            }
        }
    }
}

@Composable
private fun RowDivider(isLast: Boolean) {
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(start = Dimens.Large + Dimens.IconContainer + Dimens.MediumLarge),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.dividerColor,
        )
    }
}

@Preview
@Composable
private fun DestinationsContentPreview() {
    HuKiTheme {
        DestinationsContent(
            uiState = DestinationsUiState(
                isLoading = false,
                destinationCount = 2,
                popularItems = listOf(
                    DestinationListItem(previewDestination),
                    DestinationListItem(previewDestination.copy(osmId = "2", name = "Pilis-tető")),
                ),
            ),
            uiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
        )
    }
}

private val previewDestination = Destination(
    osmId = "1",
    name = "Dobogókő",
    town = "Pilisszentkereszt",
    type = DestinationType.PEAK,
    location = Location(47.7181, 18.8948),
    description = SharedRes.strings.destinations_section_title,
    popularity = 10,
)
