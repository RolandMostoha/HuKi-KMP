package hu.mostoha.mobile.kmp.huki.ui.features.okt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionItem
import hu.mostoha.mobile.kmp.huki.model.domain.OktStampItem
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal const val STAMP_CAROUSEL_ANIM_MILLIS = 250

private val SectionRowShape = RoundedCornerShape(22.dp)
private val SectionRowPadding = 12.dp

@Composable
fun OktSectionRow(
    section: OktSectionItem,
    isSelected: Boolean,
    isReversed: Boolean,
    selectedMarkerId: String?,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onReverseClick: () -> Unit,
    onStampClick: (OktMarker) -> Unit,
    modifier: Modifier = Modifier,
) {
    val oktBlue = mokoColor(SharedRes.colors.oktBlue)
    val containerColor by animateColorAsState(
        if (isSelected) oktBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
    )
    val stamps = if (isReversed) section.reversedStamps else section.stamps
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SectionRowShape)
            .background(containerColor)
            .padding(vertical = SectionRowPadding),
        verticalArrangement = Arrangement.spacedBy(SectionRowPadding),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SectionRowPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SectionRowPadding),
        ) {
            OktSectionHeader(
                section = section,
                isSelected = isSelected,
                isReversed = isReversed,
                onClick = onClick,
                onStartClick = onStartClick,
                modifier = Modifier.weight(1f),
            )
            OktSectionMenu(
                section = section,
                isSelected = isSelected,
                onWebsiteClick = onWebsiteClick,
                onStartClick = onStartClick,
                onReverseClick = onReverseClick,
            )
        }
        AnimatedVisibility(
            visible = isSelected && stamps.isNotEmpty(),
            enter = fadeIn(tween(STAMP_CAROUSEL_ANIM_MILLIS)) + expandVertically(tween(STAMP_CAROUSEL_ANIM_MILLIS)),
            exit = fadeOut(tween(STAMP_CAROUSEL_ANIM_MILLIS)) + shrinkVertically(tween(STAMP_CAROUSEL_ANIM_MILLIS)),
        ) {
            OktStampCarousel(
                stamps = stamps,
                selectedMarkerId = selectedMarkerId,
                onStampClick = onStampClick,
            )
        }
    }
}

@Composable
private fun OktSectionHeader(
    section: OktSectionItem,
    isSelected: Boolean,
    isReversed: Boolean,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val oktBlue = mokoColor(SharedRes.colors.oktBlue)
    val onOktBlue = mokoColor(SharedRes.colors.onOktBlue)
    val startLabel = mokoString(SharedRes.strings.okt_menu_start)
    val reversedState = mokoString(SharedRes.strings.okt_a11y_reversed_state)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.Large))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                selected = isSelected
                if (isReversed) {
                    stateDescription = reversedState
                }
                customActions = listOf(
                    CustomAccessibilityAction(startLabel) {
                        onStartClick()
                        true
                    },
                )
            }
            .testTag(TestTags.OKT_SECTION_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SectionRowPadding),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.OktBadgeSize)
                .clip(CircleShape)
                .background(if (isSelected) oktBlue else oktBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            OktBadgeText(
                prefix = section.badgePrefix,
                number = section.badgeNumber,
                fontSize = 13.sp,
                color = if (isSelected) onOktBlue else oktBlue,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
            ) {
                AnimatedVisibility(visible = isReversed, enter = fadeIn(), exit = fadeOut()) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_swap_horiz),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(Dimens.IconSmall),
                    )
                }
                Text(
                    text = section.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 10.sp,
                        maxFontSize = MaterialTheme.typography.titleMedium.fontSize,
                        stepSize = 0.5.sp,
                    ),
                )
            }
            OktSectionStats(routeStats = section.routeStats)
        }
    }
}

@Composable
private fun OktSectionStats(routeStats: RouteStats) {
    val stats = listOf(
        R.drawable.ic_clock to TravelTimeFormatter.formatTravelTimeCompact(routeStats.travelTime),
        R.drawable.ic_place_circle to DistanceFormatter.formatDistance(routeStats.distance),
        R.drawable.ic_up_double to DistanceFormatter.formatElevation(routeStats.incline),
        R.drawable.ic_down_double to DistanceFormatter.formatElevation(routeStats.decline),
    )
    val text = buildAnnotatedString {
        stats.forEachIndexed { index, (iconResId, value) ->
            if (index > 0) append("   ")
            appendInlineContent(iconResId.toString())
            append(" $value")
        }
    }
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val inlineContent = stats.associate { (iconResId, _) ->
        iconResId.toString() to InlineTextContent(
            Placeholder(width = 1.1.em, height = 1.1.em, placeholderVerticalAlign = PlaceholderVerticalAlign.Center),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(iconResId),
                contentDescription = null,
                tint = iconColor,
            )
        }
    }
    Text(
        text = text,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 9.sp,
            maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
            stepSize = 0.5.sp,
        ),
    )
}

@Composable
private fun OktSectionMenu(
    section: OktSectionItem,
    isSelected: Boolean,
    onWebsiteClick: () -> Unit,
    onStartClick: () -> Unit,
    onReverseClick: () -> Unit,
) {
    val oktBlue = mokoColor(SharedRes.colors.oktBlue)
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { isExpanded = true },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = oktBlue.copy(alpha = if (isSelected) 0.2f else 0.1f),
                contentColor = oktBlue,
            ),
            modifier = Modifier.testTag(TestTags.OKT_SECTION_MENU),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_more_horiz),
                contentDescription = mokoString(SharedRes.strings.okt_a11y_section_menu, section.name),
            )
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = RoundedCornerShape(Dimens.Large),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = Dimens.FloatingActionElevation,
        ) {
            val itemContentPadding = PaddingValues(
                start = Dimens.Large,
                end = Dimens.Huge,
                top = Dimens.Medium,
                bottom = Dimens.Medium,
            )
            val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
            DropdownMenuItem(
                text = {
                    OktMenuItemText(
                        title = mokoString(SharedRes.strings.okt_menu_details),
                        subtitle = mokoString(SharedRes.strings.okt_menu_details_subtitle),
                    )
                },
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_language),
                        contentDescription = null,
                        tint = iconColor,
                    )
                },
                onClick = {
                    isExpanded = false
                    onWebsiteClick()
                },
                contentPadding = itemContentPadding,
            )
            DropdownMenuItem(
                text = {
                    OktMenuItemText(
                        title = mokoString(SharedRes.strings.okt_menu_start),
                        subtitle = mokoString(SharedRes.strings.okt_menu_start_subtitle, section.name),
                    )
                },
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_play_arrow),
                        contentDescription = null,
                        tint = iconColor,
                    )
                },
                onClick = {
                    isExpanded = false
                    onStartClick()
                },
                contentPadding = itemContentPadding,
            )
            if (section.stamps.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        OktMenuItemText(
                            title = mokoString(SharedRes.strings.okt_menu_reverse),
                            subtitle = mokoString(SharedRes.strings.okt_menu_reverse_subtitle),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_swap_horiz),
                            contentDescription = null,
                            tint = iconColor,
                        )
                    },
                    onClick = {
                        isExpanded = false
                        onReverseClick()
                    },
                    contentPadding = itemContentPadding,
                    modifier = Modifier.testTagAsResourceId(TestTags.OKT_SECTION_REVERSE),
                )
            }
        }
    }
}

@Composable
private fun OktMenuItemText(title: String, subtitle: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OktStampCarousel(
    stamps: List<OktStampItem>,
    selectedMarkerId: String?,
    onStampClick: (OktMarker) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedMarkerId) {
        val index = stamps.indexOfFirst { it.marker.id == selectedMarkerId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = SectionRowPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
    ) {
        items(stamps, key = { it.marker.id }) { stamp ->
            OktStampCard(
                stamp = stamp,
                isSelected = stamp.marker.id == selectedMarkerId,
                onClick = { onStampClick(stamp.marker) },
            )
        }
    }
}

@Preview
@Composable
private fun OktSectionRowPreview() {
    HuKiTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            OktSectionRow(
                section = PreviewOktSection,
                isSelected = true,
                isReversed = true,
                selectedMarkerId = PreviewOktStamps.first().marker.id,
                onClick = {},
                onStartClick = {},
                onWebsiteClick = {},
                onReverseClick = {},
                onStampClick = {},
            )
            OktSectionRow(
                section = PreviewOktSection.copy(id = "OKT-02", badgeNumber = "02"),
                isSelected = false,
                isReversed = false,
                selectedMarkerId = null,
                onClick = {},
                onStartClick = {},
                onWebsiteClick = {},
                onReverseClick = {},
                onStampClick = {},
            )
        }
    }
}

internal val PreviewOktSection = OktSectionItem(
    id = "OKT-01",
    badgePrefix = "OKT",
    badgeNumber = "01",
    name = "Írott-kő - Sárvár",
    routeStats = RouteStats(
        travelTime = 18.hours + 50.minutes,
        distance = 72.5.kilometers,
        incline = 570,
        decline = 1290,
    ),
    stamps = PreviewOktStamps,
    reversedStamps = PreviewOktStamps.reversed(),
    websiteUrl = "https://www.kektura.hu/okt-szakasz/okt-01",
)
