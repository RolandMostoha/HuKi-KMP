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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
        RoutePlannerConnector(
            modifier = Modifier
                .weight(1f)
                .alpha(if (hasConnectorAbove) 1f else 0f),
        )
        icon()
        RoutePlannerConnector(
            modifier = Modifier
                .weight(1f)
                .alpha(if (hasConnectorBelow) 1f else 0f),
        )
    }
}

/**
 * Draws the connector from [top] to the bottom of the stop, under a [RoutePlannerWaypointIcon] placed at its start.
 */
fun Modifier.routePlannerConnectorBelow(top: Dp, color: Color): Modifier =
    drawBehind {
        val width = Dimens.RoutePlannerConnectorWidth.toPx()
        val height = size.height - top.toPx()
        if (height <= 0f) return@drawBehind
        drawRoundRect(
            color = color,
            topLeft = Offset(Dimens.RoutePlannerWaypointIconColumnWidth.toPx() / 2 - width / 2, top.toPx()),
            size = Size(width, height),
            cornerRadius = CornerRadius(width / 2),
        )
    }

/**
 * The color of the line connecting the stops.
 */
val routePlannerConnectorColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = CONNECTOR_ALPHA)

@Composable
fun RoutePlannerConnector(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(Dimens.RoutePlannerConnectorWidth)
            .background(
                color = routePlannerConnectorColor,
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
