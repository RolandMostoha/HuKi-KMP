package hu.mostoha.mobile.kmp.huki.ui.features.landscapes

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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationChallenge
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

private const val TITLE_MIN_SCALE = 0.8f
private val TownIconSize = 12.dp

@Composable
fun LandscapeDestinationCard(
    destination: Destination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = mokoColor(destination.type.colorRes)
    val surfaceColor = mokoColor(SharedRes.colors.mapStroke)
    val containerColor by animateColorAsState(if (isSelected) categoryColor else MaterialTheme.colorScheme.surface)
    val titleColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .size(width = Dimens.LandscapeDestinationCardWidth, height = Dimens.LandscapeDestinationCardHeight)
            .clip(RoundedCornerShape(Dimens.Large))
            .background(containerColor)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { selected = isSelected }
            .testTagAsResourceId(TestTags.landscapesDestinationCard(destination.id))
            .padding(Dimens.Medium),
        verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.OktStampIconSize)
                .clip(CircleShape)
                .background(if (isSelected) surfaceColor else categoryColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = mokoImage(destination.type.iconRes),
                contentDescription = null,
                tint = if (isSelected) categoryColor else surfaceColor,
                modifier = Modifier.size(Dimens.IconExtraSmall),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = destination.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MaterialTheme.typography.titleSmall.fontSize * TITLE_MIN_SCALE,
                maxFontSize = MaterialTheme.typography.titleSmall.fontSize,
                stepSize = 0.5.sp,
            ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_place),
                contentDescription = null,
                tint = secondaryColor,
                modifier = Modifier.size(TownIconSize),
            )
            Text(
                text = destination.town,
                style = MaterialTheme.typography.labelSmall,
                color = secondaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun LandscapeDestinationCardPreview() {
    HuKiTheme {
        Row(
            modifier = Modifier
                .background(mokoColor(SharedRes.colors.primary).copy(alpha = 0.15f))
                .padding(Dimens.Large),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
        ) {
            LandscapeDestinationCard(destination = PreviewLandscapeDestination, isSelected = true, onClick = {})
            LandscapeDestinationCard(destination = PreviewLandscapeDestination, isSelected = false, onClick = {})
        }
    }
}

internal val PreviewLandscapeDestination = Destination(
    id = "1",
    name = "Kékestető",
    town = "Mátraszentimre",
    type = DestinationType.PEAK,
    location = Location(latitude = 47.8721, longitude = 20.0102),
    description = SharedRes.strings.destinations_type_peak,
    popularity = 10,
    challenges = setOf(DestinationChallenge.HIGHEST_PEAKS),
)
