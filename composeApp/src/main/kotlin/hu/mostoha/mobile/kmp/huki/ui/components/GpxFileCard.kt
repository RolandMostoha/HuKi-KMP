package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxLandscape
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun GpxFileCard(
    file: GpxFileItem,
    landscape: GpxLandscape?,
    onClick: () -> Unit,
    onReplanClick: () -> Unit,
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            GpxFileOptionsMenu(
                isReplanVisible = file.isReplannable,
                onReplanClick = onReplanClick,
                onRenameClick = onRenameClick,
                onShareClick = onShareClick,
                onDeleteClick = onDeleteClick,
            )
        }
        GpxFileStatsRow(file = file)
        GpxFileTagRow(
            origin = file.origin,
            isRoundTrip = file.isRoundTrip,
            landscape = landscape,
            modifier = Modifier.padding(start = Dimens.ExtraLarge, end = Dimens.Large, top = Dimens.ExtraSmall),
        )
    }
}

@Composable
private fun GpxFileTagRow(
    origin: GpxOrigin,
    isRoundTrip: Boolean,
    landscape: GpxLandscape?,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        GpxFileTag(
            icon = ImageVector.vectorResource(origin.iconResId),
            text = mokoString(origin.label),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        )
        if (isRoundTrip) {
            GpxFileTag(
                icon = ImageVector.vectorResource(R.drawable.ic_refresh),
                text = mokoString(SharedRes.strings.route_planner_round_trip),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag(TestTags.GPX_COLLECTION_ITEM_ROUND_TRIP_TAG),
            )
        }
        if (landscape != null) {
            GpxFileTag(
                icon = mokoImage(landscape.iconRes),
                text = mokoString(landscape.nameRes),
                containerColor = mokoColor(landscape.colorRes),
                contentColor = Color.White,
                modifier = Modifier.testTag(TestTags.GPX_COLLECTION_ITEM_LANDSCAPE_TAG),
            )
        }
    }
}

private val TagIconSize = 14.dp

@Composable
private fun GpxFileTag(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(containerColor)
            .padding(horizontal = Dimens.Medium, vertical = Dimens.Small),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(TagIconSize),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@get:DrawableRes
private val GpxOrigin.iconResId: Int
    get() = when (this) {
        GpxOrigin.EXTERNAL -> R.drawable.ic_download
        GpxOrigin.ROUTE_PLANNER -> R.drawable.ic_touch_long
        GpxOrigin.BUNDLED -> R.drawable.ic_route
    }

private val GpxOrigin.label: StringResource
    get() = when (this) {
        GpxOrigin.EXTERNAL -> SharedRes.strings.gpx_collection_origin_external
        GpxOrigin.ROUTE_PLANNER -> SharedRes.strings.gpx_collection_origin_route_planner
        GpxOrigin.BUNDLED -> SharedRes.strings.gpx_collection_origin_bundled
    }

@Composable
private fun GpxFileStatsRow(file: GpxFileItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ExtraLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StatChip(
            iconResId = R.drawable.ic_clock_filled,
            value = mokoString(TravelTimeFormatter.formatTravelTime(file.travelTime)),
            style = StatChipStyle.Inline,
        )
        StatChip(
            iconResId = R.drawable.ic_place_circle_filled,
            value = DistanceFormatter.formatDistance(file.totalDistance),
            style = StatChipStyle.Inline,
        )
        StatChip(
            iconResId = R.drawable.ic_trending_up,
            value = DistanceFormatter.formatMeters(file.incline),
            style = StatChipStyle.Inline,
        )
        StatChip(
            iconResId = R.drawable.ic_trending_down,
            value = DistanceFormatter.formatMeters(file.decline),
            style = StatChipStyle.Inline,
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
                landscape = GpxLandscape(
                    id = "pilis_hegyseg",
                    nameRes = SharedRes.strings.landscape_pilis_hegyseg,
                    colorRes = SharedRes.colors.landscapePilisHegyseg,
                    iconRes = SharedRes.images.ic_landscapes_mountain_medium,
                ),
                onClick = {},
                onReplanClick = {},
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {},
            )
            GpxFileCard(
                file = previewFile(GpxOrigin.ROUTE_PLANNER),
                landscape = null,
                onClick = {},
                onReplanClick = {},
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
        isReplannable = origin == GpxOrigin.ROUTE_PLANNER,
        isRoundTrip = origin == GpxOrigin.ROUTE_PLANNER,
    )
