package hu.mostoha.mobile.kmp.huki.ui.features.okt

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarkerType
import hu.mostoha.mobile.kmp.huki.model.domain.OktStampItem
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun OktStampCard(
    stamp: OktStampItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val oktBlue = mokoColor(SharedRes.colors.oktBlue)
    val onOktBlue = mokoColor(SharedRes.colors.onOktBlue)
    val containerColor by animateColorAsState(if (isSelected) oktBlue else MaterialTheme.colorScheme.surface)
    val titleColor = if (isSelected) onOktBlue else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isSelected) onOktBlue.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    val distanceDescription = mokoString(SharedRes.strings.okt_a11y_stamp_distance, stamp.legDistance)
    Column(
        modifier = modifier
            .size(width = Dimens.OktStampCardWidth, height = Dimens.OktStampCardHeight)
            .clip(RoundedCornerShape(Dimens.Large))
            .background(containerColor)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                selected = isSelected
                contentDescription = "${stamp.marker.name}, $distanceDescription"
            }
            .testTag(TestTags.OKT_STAMP_CARD)
            .padding(Dimens.Medium),
        verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(Dimens.OktStampIconSize)
                    .clip(CircleShape)
                    .background(if (isSelected) onOktBlue else oktBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(SharedRes.images.ic_okt_stamp.drawableResId),
                    contentDescription = null,
                    tint = oktBlue,
                    modifier = Modifier.size(Dimens.IconExtraSmall),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stamp.legDistance,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = secondaryColor,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stamp.marker.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stamp.tag,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = secondaryColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun OktStampCardPreview() {
    HuKiTheme {
        Row(
            modifier = Modifier
                .background(mokoColor(SharedRes.colors.oktBlue).copy(alpha = 0.15f))
                .padding(Dimens.Large),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OktStampCard(stamp = PreviewOktStamps[0], isSelected = true, onClick = {})
            OktStampCard(stamp = PreviewOktStamps[1], isSelected = false, onClick = {})
        }
    }
}

internal val PreviewOktStamps = listOf(
    OktStampItem(
        marker = OktMarker(
            id = "OKTPH_02",
            location = Location(latitude = 47.33, longitude = 16.5),
            type = OktMarkerType.STAMP,
            name = "Hét-forrás",
        ),
        tag = "OKTPH_02",
        legDistance = "+8 km",
    ),
    OktStampItem(
        marker = OktMarker(
            id = "OKTPH_01_DDKPH_01_2",
            location = Location(latitude = 47.35, longitude = 16.43),
            type = OktMarkerType.STAMP,
            name = "Írott-kő",
        ),
        tag = "OKTPH_01_DDKPH_01_2",
        legDistance = "+43 m",
    ),
)
