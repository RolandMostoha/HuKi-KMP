package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.features.quickguide.DemoGesture
import hu.mostoha.mobile.kmp.huki.ui.features.quickguide.GestureDemoFab
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

private val HintMaxWidth = 480.dp
private val CloseButtonSize = 38.dp
private val CloseIconSize = 20.dp

@Composable
fun RoutePlannerLongPressHint(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .widthIn(max = HintMaxWidth)
            .testTag(TestTags.ROUTE_PLANNER_LONG_PRESS_HINT),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = Dimens.Small,
    ) {
        Row(
            modifier = Modifier.padding(
                start = Dimens.SmallMedium,
                top = Dimens.Small,
                end = Dimens.MediumLarge,
                bottom = Dimens.Small,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            GestureDemoFab(iconResId = R.drawable.ic_touch_long, gesture = DemoGesture.LONG_CLICK)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mokoString(SharedRes.strings.route_planner_long_press_hint_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = mokoString(SharedRes.strings.route_planner_long_press_hint_message),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .size(CloseButtonSize)
                    .testTag(TestTags.ROUTE_PLANNER_LONG_PRESS_HINT_CLOSE),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = mokoString(SharedRes.strings.route_planner_a11y_dismiss_long_press_hint),
                    modifier = Modifier.size(CloseIconSize),
                )
            }
        }
    }
}

@Preview(locale = "hu")
@Composable
private fun RoutePlannerLongPressHintPreview() {
    HuKiTheme {
        RoutePlannerLongPressHint(onDismiss = {}, modifier = Modifier.padding(Dimens.Large))
    }
}

@Preview(locale = "en", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoutePlannerLongPressHintDarkPreview() {
    HuKiTheme {
        RoutePlannerLongPressHint(onDismiss = {}, modifier = Modifier.padding(Dimens.Large))
    }
}
