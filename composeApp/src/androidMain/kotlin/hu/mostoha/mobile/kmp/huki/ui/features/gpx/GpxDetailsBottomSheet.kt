package hu.mostoha.mobile.kmp.huki.ui.features.gpx

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.UiFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun GpxDetailsBottomSheet(
    gpxDetails: GpxDetails,
    onStartClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(
            topStart = Dimens.ExtraLarge,
            topEnd = Dimens.ExtraLarge,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.Small,
        ),
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(
                    top = Dimens.Small,
                    bottom = Dimens.Small,
                ),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = Dimens.ExtraSmall, bottom = Dimens.Small)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.ExtraLarge,
                        top = Dimens.ExtraSmall,
                        end = Dimens.Large,
                        bottom = Dimens.Medium,
                    ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
                ) {
                    if (gpxDetails.title != null) {
                        Text(
                            text = gpxDetails.fileName,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = gpxDetails.title ?: gpxDetails.fileName,
                        modifier = Modifier.testTag(TestTags.GPX_DETAILS_TITLE),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                        contentDescription = mokoString(SharedRes.strings.a11y_close),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.82f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = Dimens.ExtraSmall),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
            ) {
                GpxStatCard(
                    iconResId = SharedRes.images.ic_gpx_travel_time.drawableResId,
                    label = mokoString(SharedRes.strings.gpx_details_travel_time),
                    value = mokoString(TravelTimeFormatter.formatTravelTime(gpxDetails.travelTime)),
                    modifier = Modifier.weight(1f),
                )
                GpxStatCard(
                    iconResId = SharedRes.images.ic_gpx_distance.drawableResId,
                    label = mokoString(SharedRes.strings.gpx_details_distance),
                    value = DistanceFormatter.formatDistance(gpxDetails.totalDistance),
                    modifier = Modifier.weight(1f),
                )
                GpxStatCard(
                    iconResId = SharedRes.images.ic_gpx_uphill.drawableResId,
                    label = mokoString(SharedRes.strings.gpx_details_incline),
                    value = DistanceFormatter.formatMeters(gpxDetails.incline),
                    modifier = Modifier.weight(1f),
                )
                GpxStatCard(
                    iconResId = SharedRes.images.ic_gpx_downhill.drawableResId,
                    label = mokoString(SharedRes.strings.gpx_details_decline),
                    value = DistanceFormatter.formatMeters(gpxDetails.decline),
                    modifier = Modifier.weight(1f),
                )
            }
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.ExtraLarge,
                        top = Dimens.Large,
                        end = Dimens.ExtraLarge,
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_fab_my_location_live_compass),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = mokoString(SharedRes.strings.gpx_details_start),
                    modifier = Modifier.padding(start = Dimens.Small),
                )
            }
        }
    }
}

@Composable
private fun GpxStatCard(
    @DrawableRes iconResId: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label, $value"
            }
            .padding(horizontal = Dimens.ExtraSmall, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = UiFormatter.formatStatValue(
                value = value,
                smallSpanStyle = SpanStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                ),
            ),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GpxDetailsBottomSheetPreview() {
    HuKiTheme {
        GpxDetailsBottomSheet(
            gpxDetails = GpxDetails(
                fileName = "3_Kossuth__Fenyves_vo_HuKi018.gpx",
                fileUri = "",
                title = "OKT-15 - Rozalia teglagyar - Dobogoko",
                locations = emptyList(),
                waypoints = emptyList(),
                bounds = emptyList(),
                totalDistance = 6.5.kilometers,
                travelTime = 2.hours.plus(15.minutes),
                altitudeRange = 63 to 313,
                incline = 250,
                decline = 63,
            ),
            onStartClick = {},
            onDismissRequest = {},
        )
    }
}
