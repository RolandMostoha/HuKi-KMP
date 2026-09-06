package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun RoutePlannerWaypointRow(
    waypoint: RoutePlannerWaypoint,
    waypointType: WaypointType,
    hasConnectorAbove: Boolean,
    onRemoveClick: () -> Unit,
    onEmptyRowClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
    isReorderable: Boolean = true,
    emptyRowTestTag: String = TestTags.ROUTE_PLANNER_EMPTY_STOP_ROW,
) {
    val title = waypoint.name?.let { mokoString(it) } ?: mokoString(SharedRes.strings.route_planner_waypoint_empty)
    val addStopLabel = mokoString(SharedRes.strings.route_planner_a11y_add_stop)
    RoutePlannerRow(
        icon = {
            RoutePlannerRowIcon(imageVector = mokoImage(waypointType.icon), tint = Color.Unspecified)
        },
        hasConnectorAbove = hasConnectorAbove,
        rowHeight = rowHeight,
        modifier = modifier,
    ) {
        if (waypoint.isEmpty) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight)
                    .testTag(emptyRowTestTag)
                    .clickable(onClickLabel = addStopLabel, onClick = onEmptyRowClick),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            RoutePlannerRowText(text = title, color = MaterialTheme.colorScheme.onSurface)
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .size(Dimens.IconContainer)
                    .testTag(TestTags.ROUTE_PLANNER_DELETE_STOP_BUTTON),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = mokoString(SharedRes.strings.route_planner_a11y_delete_stop),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconExtraSmall),
                )
            }
        }
        if (isReorderable) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_drag_handle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragHandleModifier
                    .width(Dimens.IconMedium)
                    .height(rowHeight),
            )
        }
    }
}

@Preview
@Composable
private fun RoutePlannerWaypointRowPreview() {
    HuKiTheme {
        Column {
            RoutePlannerWaypointRow(
                waypoint = PreviewRoutePlanner.myLocationWaypoint,
                waypointType = WaypointType.START,
                hasConnectorAbove = false,
                onRemoveClick = {},
                onEmptyRowClick = {},
            )
            RoutePlannerWaypointRow(
                waypoint = PreviewRoutePlanner.namedWaypoint,
                waypointType = WaypointType.INTERMEDIATE,
                hasConnectorAbove = true,
                onRemoveClick = {},
                onEmptyRowClick = {},
            )
            RoutePlannerWaypointRow(
                waypoint = PreviewRoutePlanner.emptyWaypoint,
                waypointType = WaypointType.END,
                hasConnectorAbove = true,
                onRemoveClick = {},
                onEmptyRowClick = {},
            )
        }
    }
}
