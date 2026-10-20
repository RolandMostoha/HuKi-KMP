package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DestinationTypeChip
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.categoryTint
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

private const val DESCRIPTION_MAX_LINES = 4
private const val SECONDARY_TEXT_ALPHA = 0.7f

@Composable
fun DestinationInfoWindow(destination: Destination, onPlaceDetailsClick: () -> Unit, modifier: Modifier = Modifier) {
    val chipTint = mokoColor(destination.type.colorRes).categoryTint()
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SECONDARY_TEXT_ALPHA)
    Surface(
        modifier = modifier
            .width(Dimens.DestinationInfoWindowWidth)
            .testTagAsResourceId(TestTags.LANDSCAPES_INFO_WINDOW),
        shape = InfoWindowShape(
            cornerRadius = Dimens.Large,
            tailWidth = Dimens.InfoWindowTailWidth,
            tailHeight = Dimens.InfoWindowTailHeight,
        ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimens.Small,
    ) {
        Column(
            modifier = Modifier.padding(
                start = Dimens.Large,
                top = Dimens.MediumLarge,
                end = Dimens.MediumLarge,
                bottom = Dimens.Large + Dimens.InfoWindowTailHeight,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
                ) {
                    DestinationTypeChip(
                        type = destination.type,
                        contentColor = chipTint,
                        containerColor = chipTint.copy(alpha = 0.15f),
                        textStyle = MaterialTheme.typography.labelSmall,
                        contentPadding = PaddingValues(horizontal = Dimens.SmallMedium, vertical = Dimens.ExtraSmall),
                        modifier = Modifier.padding(bottom = Dimens.ExtraSmall),
                    )
                    Text(
                        text = destination.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_place),
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = destination.town,
                            style = MaterialTheme.typography.labelMedium,
                            color = secondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                InfoWindowPlaceDetailsButton(
                    onClick = onPlaceDetailsClick,
                    modifier = Modifier.testTag(TestTags.LANDSCAPES_INFO_WINDOW_PLACE_DETAILS_BUTTON),
                )
            }
            Text(
                text = mokoString(destination.description),
                modifier = Modifier.padding(top = Dimens.ExtraSmall),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = secondaryColor,
                maxLines = DESCRIPTION_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun DestinationInfoWindowPreview() {
    HuKiTheme {
        DestinationInfoWindow(
            destination = Destination(
                id = "1",
                name = "Fátyol-vízesés",
                town = "Szilvásvárad",
                type = DestinationType.RIVER,
                location = Location(48.0855, 20.3889),
                description = SharedRes.strings.destinations_description,
                popularity = 8,
            ),
            onPlaceDetailsClick = {},
            modifier = Modifier.padding(Dimens.Large),
        )
    }
}
