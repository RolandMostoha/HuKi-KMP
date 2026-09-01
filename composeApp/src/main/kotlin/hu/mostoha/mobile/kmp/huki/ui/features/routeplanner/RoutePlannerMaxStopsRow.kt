package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val MAX_STOPS_MAX_LINES = 2

@Composable
fun RoutePlannerMaxStopsRow(
    maxStopCount: Int,
    modifier: Modifier = Modifier,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
) {
    RoutePlannerRow(
        icon = {
            RoutePlannerRowIcon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_warning),
                tint = mokoColor(SharedRes.colors.warning),
            )
        },
        hasConnectorBelow = false,
        rowHeight = rowHeight,
        modifier = modifier
            .testTag(TestTags.ROUTE_PLANNER_MAX_STOPS_ROW)
            .semantics(mergeDescendants = true) {},
    ) {
        RoutePlannerRowText(
            text = mokoString(SharedRes.strings.route_planner_max_stops_reached, maxStopCount),
            maxLines = MAX_STOPS_MAX_LINES,
        )
    }
}

@Preview
@Composable
private fun RoutePlannerMaxStopsRowPreview() {
    HuKiTheme {
        RoutePlannerMaxStopsRow(maxStopCount = 10)
    }
}
