package hu.mostoha.mobile.kmp.huki.ui.features.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage

@Composable
fun DestinationRow(
    destination: Destination,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rank: Int? = null,
    landscapeText: String? = null,
    distanceText: String? = null,
) {
    val primary = mokoColor(SharedRes.colors.primary)
    val subtitle = landscapeText?.let { "$it · ${destination.town}" } ?: destination.town
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.Large)
            .padding(vertical = Dimens.MediumLarge)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .testTag(TestTags.DESTINATIONS_ITEM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        if (rank != null) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = if (rank <= TOP_RANK_COUNT) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(22.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(Dimens.IconContainer)
                .background(color = mokoColor(destination.type.colorRes), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = mokoImage(destination.type.iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimens.IconSmall),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = destination.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        if (distanceText != null) {
            Text(
                text = distanceText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = primary,
                maxLines = 1,
            )
        }
    }
}

private const val TOP_RANK_COUNT = 3

@Preview
@Composable
private fun DestinationRowPreview() {
    HuKiTheme {
        Column {
            DestinationRow(destination = previewDestination, onClick = {}, rank = 1, landscapeText = "Pilis")
            DestinationRow(
                destination = previewDestination,
                onClick = {},
                landscapeText = "Pilis",
                distanceText = "2.4 km",
            )
            DestinationRow(destination = previewDestination, onClick = {})
        }
    }
}

private val previewDestination = Destination(
    osmId = "1",
    name = "Dobogókő",
    town = "Pilisszentkereszt",
    type = DestinationType.PEAK,
    location = Location(47.7181, 18.8948),
    description = SharedRes.strings.destinations_section_title,
    popularity = 10,
)
