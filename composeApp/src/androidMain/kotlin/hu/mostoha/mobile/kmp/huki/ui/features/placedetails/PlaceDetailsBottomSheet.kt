package hu.mostoha.mobile.kmp.huki.ui.features.placedetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceIconRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.ui.components.PrimaryButton
import hu.mostoha.mobile.kmp.huki.ui.components.SecondaryButton
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.formatter.CoordinateFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun PlaceDetailsBottomSheet(
    placeDetails: PlaceDetails,
    onRoutePlanClick: () -> Unit,
    onMapsNavigationClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHeightMeasured: (Dp) -> Unit = {},
) {
    val density = LocalDensity.current
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag(TestTags.PLACE_DETAILS_SHEET),
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
                .padding(top = Dimens.Small)
                .onGloballyPositioned {
                    onHeightMeasured(with(density) { it.size.height.toDp() })
                },
        ) {
            DragHandle(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalPadding = Dimens.SmallMedium,
            )
            PlaceDetailsHeader(
                placeDetails = placeDetails,
                onCloseClick = onCloseClick,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.ExtraLarge,
                        top = Dimens.Large,
                        end = Dimens.ExtraLarge,
                        bottom = navBarInset + Dimens.Large,
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
            ) {
                PrimaryButton(
                    iconResId = R.drawable.ic_route,
                    text = mokoString(SharedRes.strings.place_details_route_plan),
                    onClick = onRoutePlanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.PLACE_DETAILS_ROUTE_PLAN_BUTTON),
                )
                SecondaryButton(
                    iconResId = SharedRes.images.ic_maps_navigation.drawableResId,
                    text = mokoString(SharedRes.strings.place_details_maps_navigation),
                    onClick = onMapsNavigationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.PLACE_DETAILS_MAPS_NAVIGATION_BUTTON),
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailsHeader(placeDetails: PlaceDetails, onCloseClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.ExtraLarge,
                end = Dimens.Large,
            ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = placeDetails,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "PlaceDetailsHeader",
            modifier = Modifier.weight(1f),
        ) { state ->
            when (state) {
                is PlaceDetails.Loading -> LoadingContent()
                is PlaceDetails.Unresolved -> UnresolvedContent(
                    location = state.location,
                    distance = state.distance,
                )
                is PlaceDetails.PlaceLoaded -> PlaceDetailsContent(place = state.place)
            }
        }
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.testTag(TestTags.PLACE_DETAILS_CLOSE_BUTTON),
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
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.PLACE_DETAILS_LOADING),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(Dimens.IconContainer),
            contentAlignment = Alignment.Center,
        ) {
            LoadingIndicator()
        }
        Text(
            text = mokoString(SharedRes.strings.place_details_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UnresolvedContent(location: Location, distance: String?, modifier: Modifier = Modifier) {
    PlaceHeaderContent(
        title = CoordinateFormatter.formatCoordinates(location),
        icon = mokoImage(toPlaceIconRes(null)),
        iconBackgroundColor = mokoColor(SharedRes.colors.colorPlaceCategoryFallback),
        distance = distance,
        modifier = modifier,
    )
}

@Composable
private fun PlaceDetailsContent(place: Place, modifier: Modifier = Modifier) {
    val category = place.placeCategory
    PlaceHeaderContent(
        title = place.name,
        icon = mokoImage(category?.iconRes ?: toPlaceIconRes(place.osmType)),
        iconBackgroundColor = if (category != null) {
            mokoColor(category.categoryColorRes)
        } else {
            mokoColor(SharedRes.colors.colorPlaceCategoryFallback)
        },
        distance = place.distance,
        modifier = modifier,
        address = place.address,
    )
}

@Composable
private fun PlaceHeaderContent(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    distance: String?,
    modifier: Modifier = Modifier,
    address: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.IconContainer)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(Dimens.IconMedium),
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall)) {
            Text(
                text = title,
                modifier = Modifier.testTag(TestTags.PLACE_DETAILS_TITLE),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = Dimens.PlaceDetailsTitleMinFontSize,
                    maxFontSize = MaterialTheme.typography.titleMedium.fontSize,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!address.isNullOrBlank()) {
                Text(
                    text = address,
                    modifier = Modifier.testTag(TestTags.PLACE_DETAILS_ADDRESS),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!distance.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(Dimens.IconExtraSmall),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_ruler),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = distance,
                        modifier = Modifier.testTag(TestTags.PLACE_DETAILS_DISTANCE),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailsBottomSheetLoadingPreview() {
    HuKiTheme {
        PlaceDetailsBottomSheet(
            placeDetails = PlaceDetails.Loading(Location(47.7181, 18.8948)),
            onRoutePlanClick = {},
            onMapsNavigationClick = {},
            onCloseClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailsBottomSheetUnresolvedPreview() {
    HuKiTheme {
        PlaceDetailsBottomSheet(
            placeDetails = PlaceDetails.Unresolved(
                location = Location(47.7181, 18.8948),
                distance = "12.4 km",
            ),
            onRoutePlanClick = {},
            onMapsNavigationClick = {},
            onCloseClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailsBottomSheetLoadedPreview() {
    HuKiTheme {
        PlaceDetailsBottomSheet(
            placeDetails = PlaceDetails.PlaceLoaded(
                Place(
                    osmId = "1",
                    location = Location(47.7181, 18.8948),
                    name = "Dobogókő",
                    placeSource = PlaceSource.LONG_TAP_ON_MAP,
                    address = "Pilisszentkereszt, Pest, Hungary",
                    placeCategory = PlaceCategory.PEAK,
                    osmType = OsmType.NODE,
                    distance = "12.4 km",
                ),
            ),
            onRoutePlanClick = {},
            onMapsNavigationClick = {},
            onCloseClick = {},
        )
    }
}
