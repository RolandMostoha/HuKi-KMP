package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun GpxFileCard(
    file: GpxFileItem,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                top = Dimens.MediumLarge,
                bottom = Dimens.MediumLarge,
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Row(
            modifier = Modifier.padding(
                start = Dimens.ExtraLarge,
                end = Dimens.Small,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
            ) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val title = file.title
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            GpxFileOptionsMenu(
                onRenameClick = onRenameClick,
                onShareClick = onShareClick,
                onDeleteClick = onDeleteClick,
            )
        }
        GpxFileStatsRow(file = file)
    }
}

@Composable
private fun GpxFileStatsRow(file: GpxFileItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.Large,
                end = Dimens.Large,
            ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        StatChip(
            iconResId = SharedRes.images.ic_gpx_travel_time.drawableResId,
            value = mokoString(TravelTimeFormatter.formatTravelTime(file.travelTime)),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = SharedRes.images.ic_gpx_distance.drawableResId,
            value = DistanceFormatter.formatDistance(file.totalDistance),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = SharedRes.images.ic_gpx_uphill.drawableResId,
            value = DistanceFormatter.formatMeters(file.incline),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = SharedRes.images.ic_gpx_downhill.drawableResId,
            value = DistanceFormatter.formatMeters(file.decline),
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun GpxFileCardPreview() {
    HuKiTheme {
        GpxFileCard(
            file = GpxFileItem(
                fileName = "okt15.gpx",
                fileUri = "uri/okt15.gpx",
                title = "OKT-15 Rozália téglagyár – Dobogókő",
                totalDistance = 22.6.kilometers,
                travelTime = 7.hours.plus(28.minutes),
                incline = 1986,
                decline = 1642,
                lastModified = Clock.System.now(),
            ),
            onClick = {},
            onRenameClick = {},
            onShareClick = {},
            onDeleteClick = {},
        )
    }
}
