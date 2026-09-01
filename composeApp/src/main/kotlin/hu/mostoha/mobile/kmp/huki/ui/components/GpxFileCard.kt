package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
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
                GpxFileOriginChip(
                    origin = file.origin,
                    modifier = Modifier.padding(top = Dimens.ExtraSmall),
                )
            }
            GpxFileOptionsMenu(
                onRenameClick = onRenameClick,
                onShareClick = onShareClick,
                onDeleteClick = onDeleteClick,
            )
        }
        GpxFileStatsRow(
            file = file,
            modifier = Modifier.padding(top = Dimens.ExtraSmall),
        )
    }
}

@Composable
private fun GpxFileOriginChip(origin: GpxOrigin, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(horizontal = Dimens.Medium, vertical = Dimens.ExtraSmall),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(origin.iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.size(Dimens.IconExtraSmall),
        )
        Text(
            text = mokoString(origin.label),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

@get:DrawableRes
private val GpxOrigin.iconResId: Int
    get() = when (this) {
        GpxOrigin.EXTERNAL -> R.drawable.ic_download
        GpxOrigin.ROUTE_PLANNER -> R.drawable.ic_touch_long
    }

private val GpxOrigin.label: StringResource
    get() = when (this) {
        GpxOrigin.EXTERNAL -> SharedRes.strings.gpx_collection_origin_external
        GpxOrigin.ROUTE_PLANNER -> SharedRes.strings.gpx_collection_origin_route_planner
    }

@Composable
private fun GpxFileStatsRow(file: GpxFileItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.Large,
                end = Dimens.Large,
            ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        StatChip(
            iconResId = R.drawable.ic_clock,
            value = mokoString(TravelTimeFormatter.formatTravelTime(file.travelTime)),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = R.drawable.ic_place_circle,
            value = DistanceFormatter.formatDistance(file.totalDistance),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = R.drawable.ic_up_double,
            value = DistanceFormatter.formatMeters(file.incline),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            iconResId = R.drawable.ic_down_double,
            value = DistanceFormatter.formatMeters(file.decline),
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun GpxFileCardPreview() {
    HuKiTheme {
        Column {
            GpxFileCard(
                file = previewFile(GpxOrigin.EXTERNAL),
                onClick = {},
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {},
            )
            GpxFileCard(
                file = previewFile(GpxOrigin.ROUTE_PLANNER),
                onClick = {},
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {},
            )
        }
    }
}

private fun previewFile(origin: GpxOrigin) =
    GpxFileItem(
        fileName = "okt15.gpx",
        fileUri = "uri/okt15.gpx",
        trackId = "okt15",
        title = "OKT-15 Rozália téglagyár – Dobogókő",
        totalDistance = 22.6.kilometers,
        travelTime = 7.hours.plus(28.minutes),
        incline = 1986,
        decline = 1642,
        lastModified = Clock.System.now(),
        lastOpened = Clock.System.now(),
        origin = origin,
    )
