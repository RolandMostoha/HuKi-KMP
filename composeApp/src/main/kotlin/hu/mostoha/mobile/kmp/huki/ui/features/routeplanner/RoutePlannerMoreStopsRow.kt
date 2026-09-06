package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun RoutePlannerMoreStopsRow(
    hiddenStopCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
) {
    RoutePlannerRow(
        icon = {
            RoutePlannerRowIcon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_more_horiz),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        rowHeight = rowHeight,
        modifier = modifier
            .testTag(TestTags.ROUTE_PLANNER_MORE_STOPS_ROW)
            .clickable(onClick = onClick),
    ) {
        RoutePlannerRowText(text = mokoString(SharedRes.strings.route_planner_more_stops, hiddenStopCount))
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.IconSmall),
        )
    }
}

@Preview
@Composable
private fun RoutePlannerMoreStopsRowPreview() {
    HuKiTheme {
        RoutePlannerMoreStopsRow(hiddenStopCount = 3, onClick = {})
    }
}

@Preview
@Composable
private fun RoutePlannerMoreStopsRowManyPreview() {
    HuKiTheme {
        RoutePlannerMoreStopsRow(hiddenStopCount = 12, rowHeight = Dimens.RoutePlannerRowHeightExpanded, onClick = {})
    }
}
