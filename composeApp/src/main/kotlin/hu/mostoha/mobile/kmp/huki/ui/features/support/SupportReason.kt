package hu.mostoha.mobile.kmp.huki.ui.features.support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor

@Composable
fun SupportReason(
    title: String,
    message: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "chevron")
    Column(modifier = modifier.padding(top = if (showDivider) Dimens.Large else Dimens.SmallMedium)) {
        if (showDivider) {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.dividerColor)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.Small)
                .horizontalBleed(Dimens.SmallMedium)
                .clip(RoundedCornerShape(Dimens.MediumLarge))
                .clickable { onExpandedChange(!isExpanded) }
                .semantics { heading() }
                .heightIn(min = 48.dp)
                .padding(horizontal = Dimens.SmallMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(Dimens.IconSmall)
                    .rotate(chevronRotation),
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onExpandedChange(false) },
                    )
                    .padding(bottom = Dimens.MediumLarge),
            )
        }
    }
}

/**
 * Widens the touch feedback past the text column, keeping the content aligned with it.
 */
private fun Modifier.horizontalBleed(bleed: Dp): Modifier =
    layout { measurable, constraints ->
        val bleedPx = bleed.roundToPx()
        val placeable = measurable.measure(constraints.offset(horizontal = bleedPx * 2))
        layout(placeable.width - bleedPx * 2, placeable.height) {
            placeable.place(-bleedPx, 0)
        }
    }

@Preview
@Composable
private fun SupportReasonPreview() {
    HuKiTheme {
        var isExpanded by remember { mutableStateOf(true) }
        Surface {
            SupportReason(
                title = "Miért csinálom?",
                message = "Az appot hobbiból, szabadidőmben fejlesztem.",
                isExpanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                modifier = Modifier.padding(Dimens.Large),
            )
        }
    }
}
