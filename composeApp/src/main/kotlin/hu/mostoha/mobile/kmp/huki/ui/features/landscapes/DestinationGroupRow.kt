package hu.mostoha.mobile.kmp.huki.ui.features.landscapes

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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.rememberLastNonNull
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

private const val CAROUSEL_ANIM_MILLIS = 250
private const val SELECTED_CONTAINER_ALPHA = 0.3f
private val RowShape = RoundedCornerShape(22.dp)
private val RowPadding = 12.dp

@Composable
fun DestinationGroupRow(
    icon: ImageVector,
    iconSize: Dp,
    color: Color,
    title: String,
    destinationCount: Int,
    destinations: List<Destination>,
    isSelected: Boolean,
    selectedDestinationId: String?,
    onClick: () -> Unit,
    onDestinationClick: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        if (isSelected) color.copy(alpha = SELECTED_CONTAINER_ALPHA) else color.copy(alpha = 0f),
    )
    // Keeps the last destinations so the carousel can still collapse after the group is deselected
    val carouselDestinations = rememberLastNonNull(destinations.takeIf { it.isNotEmpty() }).orEmpty()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RowShape)
            .background(containerColor)
            .padding(vertical = RowPadding),
        verticalArrangement = Arrangement.spacedBy(RowPadding),
    ) {
        DestinationGroupHeader(
            icon = icon,
            iconSize = iconSize,
            color = color,
            title = title,
            destinationCount = destinationCount,
            isSelected = isSelected,
            onClick = onClick,
        )
        AnimatedVisibility(
            visible = isSelected && destinations.isNotEmpty(),
            enter = fadeIn(tween(CAROUSEL_ANIM_MILLIS)) + expandVertically(tween(CAROUSEL_ANIM_MILLIS)),
            exit = fadeOut(tween(CAROUSEL_ANIM_MILLIS)) + shrinkVertically(tween(CAROUSEL_ANIM_MILLIS)),
        ) {
            DestinationCarousel(
                destinations = carouselDestinations,
                selectedDestinationId = selectedDestinationId,
                onDestinationClick = onDestinationClick,
            )
        }
    }
}

@Composable
private fun DestinationGroupHeader(
    icon: ImageVector,
    iconSize: Dp,
    color: Color,
    title: String,
    destinationCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RowPadding)
            .clip(RoundedCornerShape(Dimens.Large))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { selected = isSelected }
            .testTagAsResourceId(TestTags.LANDSCAPES_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RowPadding),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.LandscapeIconSize)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 11.sp,
                    maxFontSize = MaterialTheme.typography.titleMedium.fontSize,
                    stepSize = 0.5.sp,
                ),
            )
            Text(
                text = if (destinationCount == 1) {
                    mokoString(SharedRes.strings.destinations_subtitle_single, destinationCount)
                } else {
                    mokoString(SharedRes.strings.destinations_subtitle_pattern, destinationCount)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DestinationCarousel(
    destinations: List<Destination>,
    selectedDestinationId: String?,
    onDestinationClick: (Destination) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedDestinationId) {
        val index = destinations.indexOfFirst { it.id == selectedDestinationId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = RowPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
    ) {
        items(destinations, key = { it.id }) { destination ->
            LandscapeDestinationCard(
                destination = destination,
                isSelected = destination.id == selectedDestinationId,
                onClick = { onDestinationClick(destination) },
            )
        }
    }
}

@Preview
@Composable
private fun DestinationGroupRowPreview() {
    val type = DestinationType.PEAK
    HuKiTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            DestinationGroupRow(
                icon = mokoImage(type.iconRes),
                iconSize = Dimens.IconMedium,
                color = mokoColor(type.colorRes),
                title = mokoString(type.title),
                destinationCount = 2,
                destinations = listOf(
                    PreviewLandscapeDestination,
                    PreviewLandscapeDestination.copy(id = "2", name = "Galya-tető"),
                ),
                isSelected = true,
                selectedDestinationId = PreviewLandscapeDestination.id,
                onClick = {},
                onDestinationClick = {},
            )
            DestinationGroupRow(
                icon = mokoImage(type.iconRes),
                iconSize = Dimens.IconMedium,
                color = mokoColor(type.colorRes),
                title = mokoString(type.title),
                destinationCount = 1,
                destinations = emptyList(),
                isSelected = false,
                selectedDestinationId = null,
                onClick = {},
                onDestinationClick = {},
            )
        }
    }
}
