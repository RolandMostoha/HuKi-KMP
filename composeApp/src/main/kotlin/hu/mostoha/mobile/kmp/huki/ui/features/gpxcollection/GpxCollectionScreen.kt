package hu.mostoha.mobile.kmp.huki.ui.features.gpxcollection

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionUiEffects
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionUiEvents
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionUiState
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileHeader
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.ui.components.GpxFileCard
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.shareGpxFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun GpxCollectionScreen(
    onBack: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenGpx: (String) -> Unit,
    viewModel: GpxCollectionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GpxCollectionContent(
        uiState = uiState,
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
                subtitle = { Text(text = subtitle) },
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
        if (!uiState.isLoading && uiState.sections.isEmpty()) {
            GpxCollectionEmptyView(
                onActionClick = { onEvent(GpxCollectionUiEvents.HelpClicked) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag(TestTags.GPX_COLLECTION_LIST),
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
                        GpxSectionHeader(header = section.header)
                    }
                    item(key = "card_${section.header}") {
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
                        )
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
private fun GpxSectionHeader(header: GpxFileHeader) {
    val text = when (header) {
        GpxFileHeader.Today -> mokoString(SharedRes.strings.gpx_collection_date_today)
        GpxFileHeader.Yesterday -> mokoString(SharedRes.strings.gpx_collection_date_yesterday)
        is GpxFileHeader.Date -> header.label
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
private fun GpxFileSectionCard(
    section: GpxFileSection,
    onFileClick: (GpxFileItem) -> Unit,
    onShareClick: (GpxFileItem) -> Unit,
    onDeleteClick: (GpxFileItem) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            section.files.forEachIndexed { index, file ->
                GpxFileCard(
                    file = file,
                    onClick = { onFileClick(file) },
                    onRenameClick = {}, // Rename not wired yet
                    onShareClick = { onShareClick(file) },
                    onDeleteClick = { onDeleteClick(file) },
                    modifier = Modifier.testTag(TestTags.GPX_COLLECTION_ITEM),
                )
                if (index != section.files.lastIndex) {
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
    val sampleToday2 = sampleToday.copy(
        fileName = "pilis-korte.gpx",
        title = "Pilis-tető körtúra",
        totalDistance = 11.2.kilometers,
        travelTime = 3.hours.plus(14.minutes),
        incline = 540,
        decline = 540,
    )
    val sampleOld = sampleToday.copy(
        fileName = "ram-szakadek.gpx",
        title = "Rám-szakadék – Dera-szurdok",
        totalDistance = 9.8.kilometers,
        travelTime = 4.hours.plus(18.minutes),
        incline = 680,
        decline = 612,
    )
    HuKiTheme {
        GpxCollectionContent(
            uiState = GpxCollectionUiState(
                isLoading = false,
                fileCount = 3,
                sections = listOf(
                    GpxFileSection(GpxFileHeader.Today, listOf(sampleToday, sampleToday2)),
                    GpxFileSection(GpxFileHeader.Date("2026.04.10"), listOf(sampleOld)),
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
