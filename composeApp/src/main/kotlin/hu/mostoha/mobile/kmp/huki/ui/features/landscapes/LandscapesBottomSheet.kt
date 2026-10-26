package hu.mostoha.mobile.kmp.huki.ui.features.landscapes

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.Strings
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesUiState
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId
import java.text.Collator
import kotlin.math.abs

private const val SHEET_EXPANDED_FRACTION = 0.9f
private val DragThreshold = 32.dp
private val LandscapeIconSize = 26.dp
private val CategoryIconSize = 24.dp
private val HeaderButtonSize = 48.dp

@Composable
fun LandscapesBottomSheet(
    landscapes: LandscapesUiState,
    sheetHeights: LandscapesSheetHeights,
    onLandscapeClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onShowAllClick: () -> Unit,
    onDestinationClick: (Destination) -> Unit,
    onCloseClick: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCategories = landscapes.grouping == LandscapesGrouping.CATEGORY
    val selectedRowId = if (isCategories) landscapes.selectedCategoryId else landscapes.selectedLandscapeId
    // Without a selected category the map has nothing to show yet
    var isExpanded by rememberSaveable { mutableStateOf(isCategories && selectedRowId == null) }
    val sheetHeight by animateDpAsState(
        if (isExpanded || !sheetHeights.isExpandable) sheetHeights.expanded else sheetHeights.collapsed,
    )
    val rows = rememberDestinationGroupItems(landscapes)
    val listState = rememberLazyListState()
    LaunchedEffect(selectedRowId) {
        val index = rows.indexOfFirst { it.id == selectedRowId }
        if (index >= 0) listState.animateScrollToItem(index)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .testTagAsResourceId(TestTags.LANDSCAPES_SHEET),
        shape = RoundedCornerShape(topStart = Dimens.ExtraLarge, topEnd = Dimens.ExtraLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Small),
    ) {
        Column(
            modifier = Modifier.sheetHeaderDrag(
                isExpandable = sheetHeights.isExpandable,
                isExpanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                onSwipeDown = onSwipeDown,
            ),
        ) {
            DragHandle(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalPadding = Dimens.SmallMedium,
            )
            LandscapesSheetHeader(
                isCategories = isCategories,
                isExpandable = sheetHeights.isExpandable,
                isExpanded = isExpanded,
                onShowAllClick = onShowAllClick,
                onExpandToggleClick = { isExpanded = !isExpanded },
                onCloseClick = onCloseClick,
            )
        }
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = Dimens.MediumLarge,
                top = Dimens.Small,
                end = Dimens.MediumLarge,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + Dimens.ExtraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
        ) {
            items(rows, key = { it.id }) { row ->
                val isSelected = row.id == selectedRowId
                DestinationGroupRow(
                    icon = mokoImage(row.iconRes),
                    iconSize = row.iconSize,
                    color = mokoColor(row.colorRes),
                    title = row.title,
                    destinationCount = row.destinationCount,
                    destinations = if (isSelected) landscapes.destinations else emptyList(),
                    isSelected = isSelected,
                    selectedDestinationId = landscapes.selectedDestinationId,
                    // The selection is shown on the map, which the expanded sheet would cover
                    onClick = {
                        isExpanded = false
                        if (isCategories) onCategoryClick(row.id) else onLandscapeClick(row.id)
                    },
                    onDestinationClick = { destination ->
                        isExpanded = false
                        onDestinationClick(destination)
                    },
                )
            }
        }
    }
}

data class LandscapesSheetHeights(
    val collapsed: Dp,
    val expanded: Dp,
    val isExpandable: Boolean,
)

@Composable
fun rememberLandscapesSheetHeights(): LandscapesSheetHeights {
    val windowHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    return if (isLandscape) {
        val height = windowHeight * SharedDimens.OKT_SHEET_LANDSCAPE_FRACTION.toFloat()
        LandscapesSheetHeights(collapsed = height, expanded = height, isExpandable = false)
    } else {
        LandscapesSheetHeights(
            // The minimum keeps a row with its carousel visible on small phones
            collapsed = maxOf(
                windowHeight * SharedDimens.LANDSCAPES_SHEET_FRACTION.toFloat(),
                SharedDimens.LANDSCAPES_SHEET_MIN_HEIGHT.dp,
            ),
            expanded = windowHeight * SHEET_EXPANDED_FRACTION,
            isExpandable = true,
        )
    }
}

@Composable
private fun Modifier.sheetHeaderDrag(
    isExpandable: Boolean,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSwipeDown: () -> Unit,
): Modifier {
    val thresholdPx = with(LocalDensity.current) { DragThreshold.toPx() }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val expandLabel = mokoString(SharedRes.strings.a11y_expand_sheet)
    val collapseLabel = mokoString(SharedRes.strings.a11y_collapse_sheet)
    return this
        .draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { dragDistance += it },
            onDragStarted = { dragDistance = 0f },
            onDragStopped = {
                when {
                    abs(dragDistance) < thresholdPx -> Unit
                    dragDistance < 0 -> if (isExpandable) onExpandedChange(true)
                    isExpanded -> onExpandedChange(false)
                    else -> onSwipeDown()
                }
            },
        )
        .semantics {
            if (isExpandable) {
                customActions = listOf(
                    CustomAccessibilityAction(if (isExpanded) collapseLabel else expandLabel) {
                        onExpandedChange(!isExpanded)
                        true
                    },
                )
            }
        }
}

@Composable
private fun LandscapesSheetHeader(
    isCategories: Boolean,
    isExpandable: Boolean,
    isExpanded: Boolean,
    onShowAllClick: () -> Unit,
    onExpandToggleClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val iconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
        when {
            // Categories have no country-wide overview, so the action resizes the sheet instead
            isCategories && isExpandable -> IconButton(
                onClick = onExpandToggleClick,
                colors = iconButtonColors,
                modifier = Modifier.testTag(TestTags.LANDSCAPES_EXPAND_BUTTON),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isExpanded) R.drawable.ic_chevron_down else R.drawable.ic_chevron_up,
                    ),
                    contentDescription = mokoString(
                        if (isExpanded) SharedRes.strings.a11y_collapse_sheet else SharedRes.strings.a11y_expand_sheet,
                    ),
                )
            }
            isCategories -> Spacer(modifier = Modifier.size(HeaderButtonSize))
            else -> IconButton(
                onClick = onShowAllClick,
                colors = iconButtonColors,
                modifier = Modifier.testTag(TestTags.LANDSCAPES_SHOW_ALL_BUTTON),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_open_in_full),
                    contentDescription = mokoString(SharedRes.strings.landscapes_a11y_show_all),
                )
            }
        }
        Text(
            text = mokoString(
                if (isCategories) SharedRes.strings.categories_title else SharedRes.strings.landscapes_title,
            ),
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        IconButton(
            onClick = onCloseClick,
            colors = iconButtonColors,
            modifier = Modifier.testTag(TestTags.LANDSCAPES_CLOSE_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = mokoString(
                    if (isCategories) {
                        SharedRes.strings.categories_a11y_close
                    } else {
                        SharedRes.strings.landscapes_a11y_close
                    },
                ),
            )
        }
    }
}

private data class DestinationGroupItem(
    val id: String,
    val iconRes: ImageResource,
    val iconSize: Dp,
    val colorRes: ColorResource,
    val title: String,
    val destinationCount: Int,
)

@Composable
private fun rememberDestinationGroupItems(landscapes: LandscapesUiState): List<DestinationGroupItem> {
    val context = LocalContext.current
    // Sorted once per sheet, so rows keep their place while the selection changes
    return remember(landscapes.grouping, landscapes.landscapes, landscapes.categories) {
        val strings = Strings(context)
        val items = when (landscapes.grouping) {
            LandscapesGrouping.LANDSCAPE -> landscapes.landscapes.map { landscape ->
                DestinationGroupItem(
                    id = landscape.id,
                    iconRes = landscape.landscapeType.iconRes,
                    iconSize = LandscapeIconSize,
                    colorRes = landscape.colorRes,
                    title = strings.get(landscape.nameRes),
                    destinationCount = landscape.destinations.size,
                )
            }
            LandscapesGrouping.CATEGORY -> landscapes.categories.map { category ->
                DestinationGroupItem(
                    id = category.id,
                    iconRes = category.iconRes,
                    iconSize = CategoryIconSize,
                    colorRes = category.colorRes,
                    title = strings.get(category.title),
                    destinationCount = category.destinations.size,
                )
            }
        }
        val collator = Collator.getInstance()
        items.sortedWith { first, second -> collator.compare(first.title, second.title) }
    }
}

@Preview
@Composable
private fun LandscapesBottomSheetPreview() {
    val landscape = Landscape(
        id = "matra",
        nameRes = SharedRes.strings.landscape_matra,
        colorRes = SharedRes.colors.primary,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        center = Location(latitude = 47.87, longitude = 20.01),
        destinations = listOf(PreviewLandscapeDestination),
        areaTags = emptyMap(),
    )
    HuKiTheme {
        LandscapesBottomSheet(
            landscapes = LandscapesUiState(
                landscapes = listOf(landscape, landscape.copy(id = "bukk", nameRes = SharedRes.strings.landscape_bukk)),
                outlinesGeoJson = "",
                labelsGeoJson = "",
                hungaryMaskGeoJson = "",
                selectedLandscapeId = landscape.id,
                selectedDestinationId = PreviewLandscapeDestination.id,
            ),
            sheetHeights = LandscapesSheetHeights(collapsed = 360.dp, expanded = 360.dp, isExpandable = true),
            onLandscapeClick = {},
            onCategoryClick = {},
            onShowAllClick = {},
            onDestinationClick = {},
            onCloseClick = {},
            onSwipeDown = {},
        )
    }
}
