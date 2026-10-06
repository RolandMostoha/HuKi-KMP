package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarkerType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@Composable
fun OktInfoWindow(info: OktInfoWindowData, onPlaceDetailsClick: () -> Unit, modifier: Modifier = Modifier) {
    val primary = mokoColor(SharedRes.colors.primary)
    Surface(
        modifier = modifier
            .width(Dimens.OktInfoWindowWidth)
            .testTagAsResourceId(TestTags.OKT_INFO_WINDOW),
        shape = InfoWindowShape(
            cornerRadius = Dimens.Large,
            tailWidth = Dimens.InfoWindowTailWidth,
            tailHeight = Dimens.InfoWindowTailHeight,
        ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimens.Small,
    ) {
        Row(
            modifier = Modifier.padding(
                start = Dimens.MediumLarge,
                top = Dimens.Medium,
                end = Dimens.MediumLarge,
                bottom = Dimens.Medium + Dimens.InfoWindowTailHeight,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
            ) {
                Text(
                    text = mokoString(info.title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                info.marker.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val distance = info.distance
                val travelTime = info.travelTime
                if (distance != null && travelTime != null) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
                    ) {
                        OktInfoPill(iconResId = R.drawable.ic_place_circle, text = distance)
                        OktInfoPill(iconResId = R.drawable.ic_clock, text = mokoString(travelTime))
                    }
                }
            }
            FilledIconButton(
                onClick = onPlaceDetailsClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = primary.copy(alpha = 0.15f),
                    contentColor = primary,
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag(TestTags.OKT_INFO_WINDOW_PLACE_DETAILS_BUTTON),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_directions),
                    contentDescription = mokoString(SharedRes.strings.okt_info_window_navigate),
                )
            }
        }
    }
}

@Composable
private fun OktInfoPill(iconResId: Int, text: String) {
    val oktBlue = mokoColor(SharedRes.colors.oktBlue)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(oktBlue.copy(alpha = 0.15f))
            .padding(horizontal = Dimens.Medium, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = null,
            tint = oktBlue,
            modifier = Modifier.size(Dimens.IconExtraSmall),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = oktBlue,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun OktInfoWindowPreview() {
    HuKiTheme {
        OktInfoWindow(
            info = OktInfoWindowData(
                marker = OktMarker(
                    id = "OKTPH_16",
                    location = Location(latitude = 47.0, longitude = 17.1),
                    type = OktMarkerType.STAMP,
                    name = "Ötvös",
                    description = "OKK útjelző oszlop - A Celldömölk - Zalaegerszeg vasútvonal- és a 7331-es főút " +
                        "kereszteződésétől 440 m-re délre, bekanyarodva az erdőbe. (OKTPH_16)",
                ),
                title = StringDesc.Raw("Ötvös stamping point"),
                distance = "4.2 km",
                travelTime = StringDesc.Raw("1h 20m"),
            ),
            onPlaceDetailsClick = {},
            modifier = Modifier.padding(Dimens.Large),
        )
    }
}
