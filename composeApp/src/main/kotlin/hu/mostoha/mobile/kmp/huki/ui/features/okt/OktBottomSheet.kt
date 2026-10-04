package hu.mostoha.mobile.kmp.huki.ui.features.okt

import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.map.OktUiState
import hu.mostoha.mobile.kmp.huki.model.domain.OktLine
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

private const val OKT_SHEET_PORTRAIT_FRACTION = 0.45f
private const val OKT_SHEET_LANDSCAPE_FRACTION = 0.6f

@Composable
fun OktBottomSheet(
    okt: OktUiState,
    sheetHeight: Dp,
    onSectionClick: (String) -> Unit,
    onSectionStartClick: (String) -> Unit,
    onSectionWebsiteClick: (String) -> Unit,
    onSectionReverseClick: (String) -> Unit,
    onStampClick: (OktMarker) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var previousSelectedSectionId by remember { mutableStateOf(okt.selectedSectionId) }
    LaunchedEffect(okt.selectedSectionId) {
        val index = okt.sections.indexOfFirst { it.id == okt.selectedSectionId }
        val previousIndex = okt.sections.indexOfFirst { it.id == previousSelectedSectionId }
        previousSelectedSectionId = okt.selectedSectionId
        if (index < 0) return@LaunchedEffect
        listState.scrollToSelectedSection(index, previousIndex)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .testTagAsResourceId(TestTags.OKT_SHEET),
        shape = RoundedCornerShape(topStart = Dimens.ExtraLarge, topEnd = Dimens.ExtraLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Small),
    ) {
        Column {
            DragHandle(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalPadding = Dimens.SmallMedium,
            )
            OktSheetHeader(
                type = okt.type,
                onStartClick = { onSectionStartClick(okt.selectedSectionId) },
                onCloseClick = onCloseClick,
            )
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
                items(okt.sections, key = { it.id }) { section ->
                    OktSectionRow(
                        section = section,
                        isSelected = section.id == okt.selectedSectionId,
                        isReversed = okt.isReversed(section.id),
                        selectedMarkerId = okt.selectedMarkerId,
                        onClick = { onSectionClick(section.id) },
                        onStartClick = { onSectionStartClick(section.id) },
                        onWebsiteClick = { onSectionWebsiteClick(section.id) },
                        onReverseClick = { onSectionReverseClick(section.id) },
                        onStampClick = onStampClick,
                    )
                }
            }
        }
    }
}

/**
 * Sheet height as a share of the window, so the map keeps room for the trail on short and landscape screens.
 */
@Composable
fun rememberOktSheetHeight(): Dp {
    val windowHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    return if (isLandscape) {
        windowHeight * OKT_SHEET_LANDSCAPE_FRACTION
    } else {
        maxOf(windowHeight * OKT_SHEET_PORTRAIT_FRACTION, Dimens.OktSheetMinHeight)
    }
}

/**
 * Scrolls in step with the carousel resize, minus the height the previous selection above gives up while collapsing.
 */
private suspend fun LazyListState.scrollToSelectedSection(index: Int, previousIndex: Int) {
    val visibleItems = layoutInfo.visibleItemsInfo
    val item = visibleItems.firstOrNull { it.index == index }
    if (item == null) {
        animateScrollToItem(index)
        return
    }
    val collapsingHeight = visibleItems
        .firstOrNull { it.index == previousIndex && previousIndex < index }
        ?.let { it.size - item.size }
        ?: 0
    animateScrollBy(
        value = (item.offset - collapsingHeight).toFloat(),
        animationSpec = tween(STAMP_CAROUSEL_ANIM_MILLIS),
    )
}

@Composable
private fun OktSheetHeader(type: OktType, onStartClick: () -> Unit, onCloseClick: () -> Unit) {
    val oktBlue = mokoColor(SharedRes.colors.oktBlue)
    val onOktBlue = mokoColor(SharedRes.colors.onOktBlue)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
        FilledIconButton(
            onClick = onStartClick,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = oktBlue, contentColor = onOktBlue),
            modifier = Modifier.testTag(TestTags.OKT_START_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_play_arrow),
                contentDescription = mokoString(SharedRes.strings.okt_a11y_start),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) { heading() },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = mokoString(type.title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = mokoString(type.longSubtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = MaterialTheme.typography.bodySmall.fontSize,
                    stepSize = 0.5.sp,
                ),
            )
        }
        IconButton(
            onClick = onCloseClick,
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.testTag(TestTags.OKT_CLOSE_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = mokoString(SharedRes.strings.a11y_close),
            )
        }
    }
}

@Preview
@Composable
private fun OktBottomSheetPreview() {
    val line = OktLine(id = "okt_base", geoJson = "", bounds = emptyList())
    HuKiTheme {
        OktBottomSheet(
            okt = OktUiState(
                type = OktType.OKT,
                sections = listOf(
                    PreviewOktSection.copy(id = "OKT", badgeNumber = null, name = "Írott-kő - Hollóháza"),
                    PreviewOktSection,
                    PreviewOktSection.copy(id = "OKT-02", badgeNumber = "02", name = "Sárvár - Sümeg"),
                ),
                selectedSectionId = PreviewOktSection.id,
                baseLine = line,
                selectedLine = line,
                markers = emptyList(),
            ),
            sheetHeight = 420.dp,
            onSectionClick = {},
            onSectionStartClick = {},
            onSectionWebsiteClick = {},
            onSectionReverseClick = {},
            onStampClick = {},
            onCloseClick = {},
        )
    }
}
