package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.ui.components.SectionHeader
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun RecentGpxSection(
    files: List<GpxFileItem>,
    onFileSelected: (GpxFileItem) -> Unit,
    onSeeAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.ExtraLarge)
            .testTag(TestTags.RECENT_GPX_SECTION),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        SectionHeader(
            title = mokoString(SharedRes.strings.search_recent_gpx_title),
            actionText = mokoString(SharedRes.strings.see_all),
            onActionClick = onSeeAllClicked,
            actionModifier = Modifier.testTag(TestTags.RECENT_GPX_SEE_ALL_BUTTON),
        )
        Card(
            modifier = Modifier.padding(horizontal = Dimens.Large),
            shape = RoundedCornerShape(Dimens.Large),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            files.forEachIndexed { index, file ->
                RecentGpxItem(
                    file = file,
                    onClick = { onFileSelected(file) },
                )
                if (index < files.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Dimens.Large + Dimens.IconContainer + Dimens.MediumLarge),
                        color = MaterialTheme.colorScheme.dividerColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentGpxItem(file: GpxFileItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(TestTags.RECENT_GPX_ITEM)
            .padding(horizontal = Dimens.Large, vertical = Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.IconContainer)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = SharedRes.images.ic_gpx.drawableResId),
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSmall),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.Small),
        ) {
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GpxStat(
                    iconResId = R.drawable.ic_clock,
                    value = mokoString(TravelTimeFormatter.formatTravelTime(file.travelTime)),
                )
                GpxStat(
                    iconResId = R.drawable.ic_distance,
                    value = DistanceFormatter.formatDistance(file.totalDistance),
                )
                GpxStat(
                    iconResId = R.drawable.ic_up_double,
                    value = DistanceFormatter.formatMeters(file.incline),
                )
                GpxStat(
                    iconResId = R.drawable.ic_down_double,
                    value = DistanceFormatter.formatMeters(file.decline),
                )
            }
        }
    }
}

@Composable
private fun GpxStat(@DrawableRes iconResId: Int, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        )
        Text(
            modifier = Modifier.padding(start = 1.dp),
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun RecentGpxSectionPreview() {
    HuKiTheme {
        RecentGpxSection(
            files = previewRecentGpxFiles,
            onFileSelected = {},
            onSeeAllClicked = {},
        )
    }
}

private val previewRecentGpxFiles = listOf(
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
    ),
    GpxFileItem(
        fileName = "pilis-korte.gpx",
        fileUri = "uri/pilis-korte.gpx",
        trackId = "pilis",
        title = "Pilis-tető körtúra",
        totalDistance = 11.2.kilometers,
        travelTime = 3.hours.plus(14.minutes),
        incline = 540,
        decline = 540,
        lastModified = Clock.System.now(),
        lastOpened = Clock.System.now(),
    ),
)
