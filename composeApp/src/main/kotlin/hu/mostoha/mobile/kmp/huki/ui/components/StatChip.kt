package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.UiFormatter

@Composable
fun StatChip(
    @DrawableRes iconResId: Int,
    value: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    style: StatChipStyle = StatChipStyle.Compact,
) {
    val isLarge = style == StatChipStyle.Large
    val isInline = style == StatChipStyle.Inline
    val statContentDescription = if (label != null) "$label, $value" else value
    val containerModifier = modifier
        .then(
            if (isInline) {
                Modifier
            } else {
                Modifier
                    .clip(RoundedCornerShape(if (isLarge) 18.dp else Dimens.Large))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            },
        )
        .semantics(mergeDescendants = true) { contentDescription = statContentDescription }
        .padding(
            horizontal = when (style) {
                StatChipStyle.Large -> Dimens.ExtraSmall
                StatChipStyle.Compact -> Dimens.Small
                StatChipStyle.Inline -> 0.dp
            },
            vertical = when (style) {
                StatChipStyle.Large -> 12.dp
                StatChipStyle.Compact -> Dimens.Medium
                StatChipStyle.Inline -> Dimens.ExtraSmall
            },
        )
    val icon: @Composable () -> Unit = {
        Icon(
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(
                when (style) {
                    StatChipStyle.Large -> Dimens.IconSmall
                    StatChipStyle.Compact -> 16.dp
                    StatChipStyle.Inline -> 18.dp
                },
            ),
        )
    }
    val text: @Composable () -> Unit = {
        Text(
            text = UiFormatter.formatStatValue(
                value = value,
                smallSpanStyle = SpanStyle(fontSize = if (isInline) 11.sp else 9.sp, fontWeight = FontWeight.Medium),
            ),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = when (style) {
                    StatChipStyle.Large -> 16.sp
                    StatChipStyle.Compact -> 13.sp
                    StatChipStyle.Inline -> 15.sp
                },
            ),
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    if (isLarge) {
        Column(
            modifier = containerModifier,
            verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            text()
        }
    } else {
        Row(
            modifier = containerModifier,
            horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            text()
        }
    }
}

/**
 * Rounded pill showing an icon next to a formatted stat value.
 * [StatChipStyle.Compact] lays icon and value out horizontally; [StatChipStyle.Large] stacks them;
 * [StatChipStyle.Inline] is the compact layout without the pill, for rows inside a card.
 */
enum class StatChipStyle { Compact, Large, Inline }

@Preview
@Composable
private fun StatChipPreview() {
    HuKiTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Small)) {
            StatChip(
                iconResId = R.drawable.ic_place_circle_filled,
                value = "22.6 km",
            )
            StatChip(
                iconResId = R.drawable.ic_trending_up,
                value = "1986 m",
                label = "Incline",
                style = StatChipStyle.Large,
            )
        }
    }
}
