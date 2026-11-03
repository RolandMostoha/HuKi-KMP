package hu.mostoha.mobile.kmp.huki.ui.features.gpxcollection

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.Strings
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionUiEffects
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionUiEvents
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionUiState
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.GpxCollectionItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxCollectionSortMode
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDistanceRange
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileDay
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileHeader
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection
import hu.mostoha.mobile.kmp.huki.model.domain.GpxLandscape
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.ui.components.ConnectedToggleGroup
import hu.mostoha.mobile.kmp.huki.ui.components.GpxFileCard
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.ui.components.ScreenViewEffect
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.shareGpxFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.spatialk.units.extensions.kilometers
import java.text.Collator
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun GpxCollectionScreen(
    onBack: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenGpx: (String) -> Unit,
    isEnterTransitionSettled: Boolean = true,
    viewModel: GpxCollectionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenViewEffect { viewModel.onEvent(GpxCollectionUiEvents.ScreenViewed) }
    GpxCollectionContent(
        uiState = uiState,
        isListVisible = isEnterTransitionSettled,
        uiEffects = viewModel.uiEffects,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenTutorial = onOpenTutorial,
        onOpenGpx = onOpenGpx,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GpxCollectionContent(
    uiState: GpxCollectionUiState,
    uiEffects: Flow<GpxCollectionUiEffects>,
    onEvent: (GpxCollectionUiEvents) -> Unit,
    onBack: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenGpx: (String) -> Unit,
    isListVisible: Boolean = true,
) {
    val context = LocalContext.current
    LaunchedEffect(uiEffects) {
        uiEffects.collect { effect ->
            when (effect) {
                GpxCollectionUiEffects.NavigateBack -> onBack()
                GpxCollectionUiEffects.NavigateToTutorial -> onOpenTutorial()
                is GpxCollectionUiEffects.OpenGpx -> onOpenGpx(effect.fileUri)
                is GpxCollectionUiEffects.ShareGpx -> context.shareGpxFile(effect.fileUri, effect.fileName)
            }
        }
    }
    val sections = remember(uiState.sections, uiState.sortMode, context) {
        if (uiState.sortMode == GpxCollectionSortMode.LANDSCAPE) {
            uiState.sections.sortedByLandscapeName(Strings(context))
        } else {
            uiState.sections
        }
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val barColor = MaterialTheme.colorScheme.surfaceVariant
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag(TestTags.GPX_COLLECTION_SCREEN_ROOT),
        containerColor = barColor,
        topBar = {
            val subtitle = when (uiState.fileCount) {
                0 -> mokoString(SharedRes.strings.gpx_collection_subtitle_empty)
                1 -> mokoString(SharedRes.strings.gpx_collection_subtitle_single, uiState.fileCount)
                else -> mokoString(SharedRes.strings.gpx_collection_subtitle_pattern, uiState.fileCount)
            }
            MediumFlexibleTopAppBar(
                title = { Text(text = mokoString(SharedRes.strings.gpx_collection_title)) },
                subtitle = { Text(text = if (uiState.isLoading) "" else subtitle) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.GPX_COLLECTION_BACK_BUTTON),
                        onClick = { onEvent(GpxCollectionUiEvents.BackClicked) },
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.gpx_collection_a11y_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.GPX_COLLECTION_HELP_BUTTON),
                        onClick = { onEvent(GpxCollectionUiEvents.HelpClicked) },
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(SharedRes.images.ic_help.drawableResId),
                            contentDescription = mokoString(SharedRes.strings.gpx_collection_a11y_help),
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
        val layoutDirection = LocalLayoutDirection.current
        val bottomInset = innerPadding.calculateBottomPadding()
        Crossfade(
            targetState = isListVisible && !uiState.isLoading && sections.isEmpty(),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                ),
            label = "gpx_collection_content",
        ) { isEmpty ->
            if (isEmpty) {
                GpxCollectionEmptyView(
                    onActionClick = { onEvent(GpxCollectionUiEvents.HelpClicked) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomInset),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTags.GPX_COLLECTION_LIST),
                    contentPadding = PaddingValues(
                        start = Dimens.Large,
                        end = Dimens.Large,
                        top = Dimens.Small,
                        bottom = bottomInset + Dimens.ExtraLarge,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Small),
                ) {
                    item(key = "sort_mode") {
                        GpxSortModeSelector(
                            selected = uiState.sortMode,
                            onSelect = { onEvent(GpxCollectionUiEvents.SortModeSelected(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.Small),
                        )
                    }
                    if (!isListVisible || uiState.isLoading) {
                        item(key = "loading") {
                            GpxCollectionLoadingView(
                                modifier = Modifier
                                    .fillParentMaxHeight(LOADING_HEIGHT_FRACTION)
                                    .fillMaxWidth()
                                    .animateItem(),
                            )
                        }
                    } else {
                        sections.forEach { section ->
                            item(key = section.header.key) {
                                GpxSectionHeader(
                                    header = section.header,
                                    count = section.items.size,
                                    modifier = Modifier.animateItem(),
                                )
                            }
                            item(key = "card_${section.header.key}") {
                                GpxFileSectionCard(
                                    section = section,
                                    onFileClick = { file ->
                                        onEvent(GpxCollectionUiEvents.FileClicked(file))
                                    },
                                    onShareClick = { file ->
                                        onEvent(GpxCollectionUiEvents.ShareClicked(file))
                                    },
                                    onDeleteClick = { file ->
                                        onEvent(GpxCollectionUiEvents.DeleteClicked(file))
                                    },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    uiState.pendingDelete?.let { file ->
        DeleteConfirmDialog(
            fileName = file.fileName,
            onConfirm = { onEvent(GpxCollectionUiEvents.DeleteConfirmed) },
            onDismiss = { onEvent(GpxCollectionUiEvents.DeleteDismissed) },
        )
    }
}

/**
 * Sorts the landscape sections by their localized name with the device's collation, keeping the other ones last.
 */
private fun List<GpxFileSection>.sortedByLandscapeName(strings: Strings): List<GpxFileSection> {
    val collator = Collator.getInstance()
    val (landscapes, others) = partition { it.header is GpxFileHeader.Landscape }
    return landscapes.sortedWith { first, second ->
        collator.compare(first.landscapeName(strings), second.landscapeName(strings))
    } + others
}

private fun GpxFileSection.landscapeName(strings: Strings): String =
    (header as? GpxFileHeader.Landscape)?.let { strings.get(it.landscape.nameRes) }.orEmpty()

private const val LOADING_HEIGHT_FRACTION = 0.6f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GpxCollectionLoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.testTag(TestTags.GPX_COLLECTION_LOADING),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun GpxCollectionEmptyView(onActionClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = Dimens.Large),
        contentAlignment = Alignment.Center,
    ) {
        InfoView(
            infoViewData = InfoViewData(
                infoViewType = InfoViewType.SUCCESS,
                icon = SharedRes.images.ic_gpx,
                title = SharedRes.strings.gpx_collection_empty_title,
                message = SharedRes.strings.gpx_collection_empty_message,
            ),
            primaryActionText = mokoString(SharedRes.strings.gpx_collection_empty_action),
            onPrimaryActionClick = onActionClick,
            modifier = Modifier.testTag(TestTags.GPX_COLLECTION_EMPTY_VIEW),
        )
    }
}

@Composable
private fun GpxSortModeSelector(
    selected: GpxCollectionSortMode,
    onSelect: (GpxCollectionSortMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val a11yLabel = mokoString(SharedRes.strings.gpx_collection_sort_a11y)
    ConnectedToggleGroup(
        options = GpxCollectionSortMode.entries,
        selected = selected,
        onSelect = onSelect,
        label = { mokoString(it.label) },
        modifier = modifier.semantics { contentDescription = a11yLabel },
        optionModifier = { Modifier.testTag(it.testTag) },
    )
}

private val GpxCollectionSortMode.label: StringResource
    get() = when (this) {
        GpxCollectionSortMode.DATE -> SharedRes.strings.gpx_collection_sort_date
        GpxCollectionSortMode.DISTANCE -> SharedRes.strings.gpx_collection_sort_distance
        GpxCollectionSortMode.LANDSCAPE -> SharedRes.strings.gpx_collection_sort_landscape
    }

private val GpxCollectionSortMode.testTag: String
    get() = when (this) {
        GpxCollectionSortMode.DATE -> TestTags.GPX_COLLECTION_SORT_DATE
        GpxCollectionSortMode.DISTANCE -> TestTags.GPX_COLLECTION_SORT_DISTANCE
        GpxCollectionSortMode.LANDSCAPE -> TestTags.GPX_COLLECTION_SORT_LANDSCAPE
    }

@Composable
private fun GpxSectionHeader(header: GpxFileHeader, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.Medium, bottom = Dimens.Small)
            .semantics(mergeDescendants = true) {}
            .testTag(TestTags.GPX_COLLECTION_SECTION_HEADER),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = header.text(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (header !is GpxFileHeader.Day) {
            Text(
                text = if (count == 1) {
                    mokoString(SharedRes.strings.gpx_collection_subtitle_single, count)
                } else {
                    mokoString(SharedRes.strings.gpx_collection_subtitle_pattern, count)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GpxFileHeader.text(): String =
    when (this) {
        is GpxFileHeader.Day -> day.text()
        is GpxFileHeader.Distance -> mokoString(range.label)
        is GpxFileHeader.Landscape -> mokoString(landscape.nameRes)
        GpxFileHeader.OutsideLandscapes -> mokoString(SharedRes.strings.gpx_collection_landscape_other)
    }

@Composable
private fun GpxFileDay.text(): String =
    when (this) {
        GpxFileDay.Today -> mokoString(SharedRes.strings.gpx_collection_date_today)
        GpxFileDay.Yesterday -> mokoString(SharedRes.strings.gpx_collection_date_yesterday)
        is GpxFileDay.Date -> label
    }

private val GpxDistanceRange.label: StringResource
    get() = when (this) {
        GpxDistanceRange.OVER_20 -> SharedRes.strings.gpx_collection_distance_over_20
        GpxDistanceRange.FROM_10_TO_20 -> SharedRes.strings.gpx_collection_distance_10_20
        GpxDistanceRange.FROM_5_TO_10 -> SharedRes.strings.gpx_collection_distance_5_10
        GpxDistanceRange.UNDER_5 -> SharedRes.strings.gpx_collection_distance_under_5
    }

@Composable
private fun GpxFileSectionCard(
    section: GpxFileSection,
    onFileClick: (GpxFileItem) -> Unit,
    onShareClick: (GpxFileItem) -> Unit,
    onDeleteClick: (GpxFileItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            section.items.forEachIndexed { index, item ->
                val file = item.file
                GpxFileCard(
                    file = file,
                    landscape = item.landscape,
                    onClick = { onFileClick(file) },
                    onRenameClick = {}, // Rename not wired yet
                    onShareClick = { onShareClick(file) },
                    onDeleteClick = { onDeleteClick(file) },
                    modifier = Modifier.testTag(TestTags.GPX_COLLECTION_ITEM),
                )
                if (index != section.items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Dimens.Large),
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
private fun GpxCollectionContentPreview() {
    val now = Clock.System.now()
    val sampleToday = GpxFileItem(
        fileName = "okt15.gpx",
        fileUri = "uri/okt15.gpx",
        trackId = "okt15",
        title = "OKT-15 Rozália téglagyár – Dobogókő",
        totalDistance = 22.6.kilometers,
        travelTime = 7.hours.plus(28.minutes),
        incline = 1986,
        decline = 1642,
        lastModified = now,
        lastOpened = now,
        origin = GpxOrigin.EXTERNAL,
    )
    val sampleOld = sampleToday.copy(
        fileName = "ram-szakadek.gpx",
        title = "Rám-szakadék – Dera-szurdok",
        totalDistance = 9.8.kilometers,
        travelTime = 4.hours.plus(18.minutes),
        incline = 680,
        decline = 612,
    )
    val pilis = GpxLandscape(
        id = "pilis_hegyseg",
        nameRes = SharedRes.strings.landscape_pilis_hegyseg,
        colorRes = SharedRes.colors.landscapePilisHegyseg,
        iconRes = SharedRes.images.ic_landscapes_mountain_medium,
    )
    HuKiTheme {
        GpxCollectionContent(
            uiState = GpxCollectionUiState(
                isLoading = false,
                fileCount = 2,
                sortMode = GpxCollectionSortMode.DISTANCE,
                sections = listOf(
                    GpxFileSection(
                        header = GpxFileHeader.Distance(GpxDistanceRange.OVER_20),
                        items = listOf(GpxCollectionItem(sampleToday, pilis)),
                    ),
                    GpxFileSection(
                        header = GpxFileHeader.Distance(GpxDistanceRange.FROM_5_TO_10),
                        items = listOf(GpxCollectionItem(sampleOld, null)),
                    ),
                ),
            ),
            uiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
            onOpenTutorial = {},
            onOpenGpx = {},
        )
    }
}

@Preview(name = "Empty")
@Composable
private fun GpxCollectionEmptyPreview() {
    HuKiTheme {
        GpxCollectionContent(
            uiState = GpxCollectionUiState(isLoading = false),
            uiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
            onOpenTutorial = {},
            onOpenGpx = {},
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun GpxCollectionLoadingPreview() {
    HuKiTheme {
        GpxCollectionContent(
            uiState = GpxCollectionUiState(isLoading = true),
            uiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
            onOpenTutorial = {},
            onOpenGpx = {},
        )
    }
}
