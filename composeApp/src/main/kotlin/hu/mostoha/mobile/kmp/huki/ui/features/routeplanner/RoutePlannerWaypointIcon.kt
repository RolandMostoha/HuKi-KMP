package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme

private const val CONNECTOR_ALPHA = 0.3f

@Composable
fun RoutePlannerWaypointIcon(
    hasConnectorAbove: Boolean,
    hasConnectorBelow: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .width(Dimens.RoutePlannerWaypointIconColumnWidth)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Connector(
            modifier = Modifier
                .weight(1f)
                .alpha(if (hasConnectorAbove) 1f else 0f),
        )
        icon()
        Connector(
            modifier = Modifier
                .weight(1f)
                .alpha(if (hasConnectorBelow) 1f else 0f),
        )
    }
}

@Composable
private fun Connector(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(Dimens.RoutePlannerConnectorWidth)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = CONNECTOR_ALPHA),
                shape = RoundedCornerShape(percent = 50),
            ),
    )
}

@Preview
@Composable
private fun RoutePlannerWaypointIconPreview() {
    HuKiTheme {
        Column {
            listOf(
                false to true,
                true to true,
                true to false,
            ).forEach { (above, below) ->
                RoutePlannerWaypointIcon(
                    hasConnectorAbove = above,
                    hasConnectorBelow = below,
                    modifier = Modifier.height(Dimens.RoutePlannerRowHeight),
                ) {
                    Box(
                        modifier = Modifier
                            .width(Dimens.RoutePlannerWaypointIconColumnWidth)
                            .height(Dimens.RoutePlannerWaypointIconColumnWidth)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(percent = 50)),
                    )
                }
            }
        }
    }
}
