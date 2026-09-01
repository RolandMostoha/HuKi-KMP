package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val DISABLED_ALPHA = 0.35f

@Composable
fun RoutePlannerAddStopRow(
    isRoundTripEnabled: Boolean,
    onAddStopClick: () -> Unit,
    onRoundTripClick: () -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
) {
    val roundTripAlpha by animateFloatAsState(
        targetValue = if (isRoundTripEnabled) 1f else DISABLED_ALPHA,
        label = "roundTripAlpha",
    )
    val addStopLabel = mokoString(SharedRes.strings.route_planner_a11y_add_stop)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = rowHeight),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoutePlannerRow(
            icon = {
                RoutePlannerRowIcon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_add_circle),
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            hasConnectorBelow = false,
            rowHeight = rowHeight,
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.ROUTE_PLANNER_ADD_STOP_ROW)
                .clickable(onClickLabel = addStopLabel, onClick = onAddStopClick),
        ) {
            RoutePlannerRowText(
                text = mokoString(SharedRes.strings.route_planner_add_waypoint),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(
            onClick = onRoundTripClick,
            enabled = isRoundTripEnabled,
            modifier = Modifier
                .size(Dimens.IconContainer)
                .alpha(roundTripAlpha)
                .testTag(TestTags.ROUTE_PLANNER_ROUND_TRIP_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_refresh),
                contentDescription = mokoString(SharedRes.strings.route_planner_a11y_round_trip),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.IconSmall),
            )
        }
    }
}

@Preview
@Composable
private fun RoutePlannerAddStopRowPreview() {
    HuKiTheme {
        RoutePlannerAddStopRow(isRoundTripEnabled = true, onAddStopClick = {}, onRoundTripClick = {})
    }
}

@Preview
@Composable
private fun RoutePlannerAddStopRowDisabledPreview() {
    HuKiTheme {
        RoutePlannerAddStopRow(isRoundTripEnabled = false, onAddStopClick = {}, onRoundTripClick = {})
    }
}
