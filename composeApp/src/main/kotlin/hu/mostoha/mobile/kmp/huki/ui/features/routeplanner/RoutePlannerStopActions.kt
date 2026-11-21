package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val CONTAINER_ALPHA = 0.5f
private const val DISABLED_ALPHA = 0.4f

@Composable
fun RoutePlannerStopActions(
    isAddStopEnabled: Boolean,
    isRoundTrip: Boolean,
    isRoundTripEnabled: Boolean,
    onAddStopClick: () -> Unit,
    onRoundTripClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        StopActionButton(
            iconResId = R.drawable.ic_add,
            text = mokoString(SharedRes.strings.route_planner_add_stop_title),
            enabled = isAddStopEnabled,
            onClick = onAddStopClick,
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.ROUTE_PLANNER_ADD_STOP_ROW),
        )
        StopActionButton(
            iconResId = R.drawable.ic_refresh,
            text = mokoString(
                if (isRoundTrip) {
                    SharedRes.strings.route_planner_round_trip_undo
                } else {
                    SharedRes.strings.route_planner_round_trip
                },
            ),
            enabled = isRoundTripEnabled,
            onClick = onRoundTripClick,
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.ROUTE_PLANNER_ROUND_TRIP_BUTTON),
        )
    }
}

@Composable
private fun StopActionButton(
    @DrawableRes iconResId: Int,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CONTAINER_ALPHA)
    val contentColor = MaterialTheme.colorScheme.primary
    val alpha by animateFloatAsState(targetValue = if (enabled) 1f else DISABLED_ALPHA, label = "stopActionAlpha")
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor,
        ),
        modifier = modifier.alpha(alpha),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
        )
    }
}

@Preview
@Composable
private fun RoutePlannerStopActionsPreview() {
    HuKiTheme {
        RoutePlannerStopActions(
            isAddStopEnabled = true,
            isRoundTrip = false,
            isRoundTripEnabled = true,
            onAddStopClick = {},
            onRoundTripClick = {},
        )
    }
}

@Preview(locale = "hu")
@Composable
private fun RoutePlannerStopActionsDisabledPreview() {
    HuKiTheme {
        RoutePlannerStopActions(
            isAddStopEnabled = false,
            isRoundTrip = false,
            isRoundTripEnabled = false,
            onAddStopClick = {},
            onRoundTripClick = {},
        )
    }
}

@Preview(locale = "hu")
@Composable
private fun RoutePlannerStopActionsRoundTripPreview() {
    HuKiTheme {
        RoutePlannerStopActions(
            isAddStopEnabled = false,
            isRoundTrip = true,
            isRoundTripEnabled = true,
            onAddStopClick = {},
            onRoundTripClick = {},
        )
    }
}
