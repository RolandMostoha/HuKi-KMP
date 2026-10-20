package hu.mostoha.mobile.kmp.huki.ui.features.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DestinationTypeChip
import hu.mostoha.mobile.kmp.huki.util.categoryTint
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

private val CardCornerRadius = 20.dp
private val ChipSpacing = 56.dp

@Composable
fun DestinationPreviewCard(
    destination: Destination,
    modifier: Modifier = Modifier,
    landscapeText: String? = null,
    distanceText: String? = null,
) {
    val typeColor = mokoColor(destination.type.colorRes)
    val chipTint = typeColor.categoryTint()
    val subtitle = landscapeText?.let { "${destination.town} · $it" } ?: destination.town
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(typeColor),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                    ),
                ),
        )
        Column(modifier = Modifier.padding(Dimens.Large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                DestinationTypeChip(type = destination.type)
                Spacer(modifier = Modifier.weight(1f))
                if (distanceText != null) {
                    DistanceChip(text = distanceText, typeColor = chipTint)
                }
            }
            if (destination.challenges.isNotEmpty()) {
                Text(
                    text = destination.challenges
                        .sortedBy { it.ordinal }
                        .map { mokoString(it.title) }
                        .joinToString(" · "),
                    modifier = Modifier.padding(top = Dimens.Small),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(ChipSpacing))
            Text(
                text = destination.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_place),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                )
                Spacer(modifier = Modifier.width(Dimens.ExtraSmall))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = mokoString(destination.description),
                modifier = Modifier.padding(top = Dimens.MediumLarge),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun DistanceChip(text: String, typeColor: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Dimens.MediumLarge, vertical = Dimens.Small),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = typeColor,
        maxLines = 1,
    )
}

@Preview
@Composable
private fun DestinationPreviewCardPreview() {
    HuKiTheme {
        DestinationPreviewCard(
            destination = previewDestination,
            landscapeText = "Mátra",
            distanceText = "89 km",
        )
    }
}

private val previewDestination = Destination(
    osmId = "1",
    name = "Kékestető",
    town = "Mátraszentimre",
    type = DestinationType.PEAK,
    location = Location(47.8709, 20.0106),
    description = SharedRes.strings.destinations_section_title,
    popularity = 10,
)
