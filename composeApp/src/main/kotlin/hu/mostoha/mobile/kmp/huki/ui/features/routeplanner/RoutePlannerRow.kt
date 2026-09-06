package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import hu.mostoha.mobile.kmp.huki.theme.Dimens

@Composable
fun RoutePlannerRow(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    hasConnectorAbove: Boolean = true,
    hasConnectorBelow: Boolean = true,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = rowHeight),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoutePlannerWaypointIcon(
            hasConnectorAbove = hasConnectorAbove,
            hasConnectorBelow = hasConnectorBelow,
            modifier = Modifier.height(rowHeight),
            icon = icon,
        )
        content()
    }
}

@Composable
fun RoutePlannerRowIcon(imageVector: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(Dimens.RoutePlannerWaypointIconColumnWidth),
    )
}

@Composable
fun RowScope.RoutePlannerRowText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.weight(1f),
    )
}
