package hu.mostoha.mobile.kmp.huki.ui.features.discover

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.HikeRecommendationCard
import hu.mostoha.mobile.kmp.huki.ui.components.NavigationRowCard
import hu.mostoha.mobile.kmp.huki.ui.components.SectionHeader
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId
import kotlinx.coroutines.launch

private val HikeRecommendationTitleSizes = listOf(14.sp, 13.sp, 12.sp, 11.sp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverBottomSheet(
    sheetState: SheetState,
    onHikeRecommendationClicked: (HikeRecommendation) -> Unit,
    onBrowseDestinationsClicked: () -> Unit,
    onInfoClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = null,
        sheetState = sheetState,
    ) {
        DiscoverContent(
            onHikeRecommendationClicked = onHikeRecommendationClicked,
            onBrowseDestinationsClicked = onBrowseDestinationsClicked,
            onInfoClicked = onInfoClicked,
            onCloseClicked = onDismissRequest,
        )
    }
}

@Composable
fun DiscoverContent(
    onHikeRecommendationClicked: (HikeRecommendation) -> Unit,
    onBrowseDestinationsClicked: () -> Unit,
    onInfoClicked: () -> Unit,
    onCloseClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTagAsResourceId(TestTags.DISCOVER_SHEET)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(top = Dimens.Large, bottom = Dimens.ExtraLarge),
    ) {
        DiscoverHeader(onCloseClick = onCloseClicked)
        Column(
            modifier = Modifier
                .padding(top = Dimens.SectionSpacing)
                .testTag(TestTags.DISCOVER_HIKE_RECOMMENDATIONS_SECTION),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionHeaderSpacing),
        ) {
            SectionHeader(
                title = mokoString(SharedRes.strings.discover_hike_recommendations_title),
                trailingContent = { HikeRecommendationsInfoButton(onClick = onInfoClicked) },
            )
            BoxWithConstraints(modifier = Modifier.padding(horizontal = Dimens.Large)) {
                val cardCount = HikeRecommendation.entries.size
                val titleWidth = (maxWidth - Dimens.MediumLarge * (cardCount - 1)) / cardCount - Dimens.ExtraSmall * 2
                val titleStyle = rememberFittingTitleStyle(maxTitleWidth = titleWidth)
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge)) {
                    HikeRecommendation.entries.forEach { recommendation ->
                        HikeRecommendationCard(
                            recommendation = recommendation,
                            onClick = { onHikeRecommendationClicked(recommendation) },
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleStyle = titleStyle,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(TestTags.HIKE_RECOMMENDATION_CARD),
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.padding(top = Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionHeaderSpacing),
        ) {
            SectionHeader(title = mokoString(SharedRes.strings.destinations_section_title))
            NavigationRowCard(
                iconResId = R.drawable.ic_place_circle,
                title = mokoString(SharedRes.strings.discover_browse_destinations),
                subtitle = mokoString(SharedRes.strings.destinations_description),
                onClick = onBrowseDestinationsClicked,
                modifier = Modifier.testTag(TestTags.DISCOVER_BROWSE_DESTINATIONS_BUTTON),
            )
        }
    }
}

@Composable
private fun rememberFittingTitleStyle(maxTitleWidth: Dp): TextStyle {
    val baseStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
    val titles = HikeRecommendation.entries.map { mokoString(it.title) }
    val textMeasurer = rememberTextMeasurer()
    val maxTitleWidthPx = with(LocalDensity.current) { maxTitleWidth.roundToPx() }
    return remember(baseStyle, titles, maxTitleWidthPx) {
        HikeRecommendationTitleSizes
            .map { baseStyle.copy(fontSize = it) }
            .firstOrNull { style ->
                titles.all { textMeasurer.measure(it, style, maxLines = 1).size.width <= maxTitleWidthPx }
            }
            ?: baseStyle.copy(fontSize = HikeRecommendationTitleSizes.last())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HikeRecommendationsInfoButton(onClick: () -> Unit) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val coroutineScope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            RichTooltip(
                modifier = Modifier.testTag(TestTags.DISCOVER_HIKE_RECOMMENDATIONS_INFO_TOOLTIP),
                title = { Text(mokoString(SharedRes.strings.discover_hike_recommendations_title)) },
            ) {
                Text(mokoString(SharedRes.strings.discover_hike_recommendations_info))
            }
        },
        state = tooltipState,
    ) {
        IconButton(
            onClick = {
                onClick()
                coroutineScope.launch { tooltipState.show() }
            },
            modifier = Modifier.testTag(TestTags.DISCOVER_HIKE_RECOMMENDATIONS_INFO_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_help),
                contentDescription = mokoString(SharedRes.strings.discover_hike_recommendations_a11y_info),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiscoverHeader(onCloseClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.Large, end = Dimens.Large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mokoString(SharedRes.strings.discover_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onCloseClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.testTag(TestTags.DISCOVER_CLOSE_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = mokoString(SharedRes.strings.a11y_close),
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(device = "spec:width=891dp,height=411dp", name = "Landscape")
@Composable
private fun DiscoverContentPreview() {
    HuKiTheme {
        DiscoverContent(
            onHikeRecommendationClicked = {},
            onBrowseDestinationsClicked = {},
            onInfoClicked = {},
            onCloseClicked = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}
