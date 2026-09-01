package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.ui.components.RouteStatsRow
import hu.mostoha.mobile.kmp.huki.ui.components.StatChipStyle
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun RoutePlannerRouteStatus(
    error: InfoViewData?,
    routeStats: RouteStats?,
    isPlanExpected: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        error != null -> InfoView(
            infoViewData = error,
            primaryActionText = mokoString(SharedRes.strings.route_planner_error_retry),
            onPrimaryActionClick = onRetryClick,
            modifier = modifier
                .fillMaxWidth()
                .testTag(TestTags.ROUTE_PLANNER_ERROR),
        )
        routeStats != null -> RouteStatsRow(
            routeStats = routeStats,
            style = StatChipStyle.Compact,
            modifier = modifier
                .fillMaxWidth()
                .testTag(TestTags.ROUTE_PLANNER_STATS),
        )
        isPlanExpected -> Spacer(modifier = modifier.height(Dimens.RoutePlannerRowHeight))
    }
}

@Preview
@Composable
private fun RoutePlannerRouteStatusStatsPreview() {
    HuKiTheme {
        RoutePlannerRouteStatus(
            error = null,
            routeStats = PreviewRoutePlanner.routePlan.routeStats,
            isPlanExpected = true,
            onRetryClick = {},
        )
    }
}

@Preview
@Composable
private fun RoutePlannerRouteStatusEmptyPreview() {
    HuKiTheme {
        RoutePlannerRouteStatus(error = null, routeStats = null, isPlanExpected = true, onRetryClick = {})
    }
}
