package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceIconRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage

@Composable
fun SearchResultItem(place: Place, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subtitle = place.address
    val category = place.placeCategory
    val backgroundColor = if (category != null) {
        mokoColor(category.categoryColorRes)
    } else {
        mokoColor(SharedRes.colors.colorPlaceCategoryFallback)
    }
    val iconRes = category?.iconRes ?: toPlaceIconRes(place.osmType)
    val distance = place.distance

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.SEARCH_RESULT_ITEM),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.Large, vertical = Dimens.MediumLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.IconContainer)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(Dimens.IconMedium),
                    imageVector = mokoImage(iconRes),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (!distance.isNullOrBlank()) {
                Text(
                    text = distance,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = Dimens.Small),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SearchResultItemPreview() {
    HuKiTheme {
        Column {
            previewPlaces.forEach { place ->
                SearchResultItem(place = place, onClick = {})
            }
        }
    }
}

private val previewPlaces = listOf(
    Place(
        osmId = "1",
        name = "Unknown Node",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Fallback icon - node",
        location = Location(47.0, 19.0),
        osmType = OsmType.NODE,
        distance = "2 km",
    ),
    Place(
        osmId = "2",
        name = "Unknown Way",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Fallback icon - way",
        location = Location(47.0, 19.0),
        osmType = OsmType.WAY,
    ),
    Place(
        osmId = "3",
        name = "Unknown Relation",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Fallback icon - relation",
        location = Location(47.0, 19.0),
        osmType = OsmType.RELATION,
    ),
    Place(
        osmId = "4",
        name = "Dobogókő",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Pilis Mountains, Hungary",
        location = Location(47.7181, 18.8948),
        placeCategory = PlaceCategory.PEAK,
        osmType = OsmType.NODE,
        distance = "2 km",
    ),
    Place(
        osmId = "5",
        name = "Balaton",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Lake, Transdanubia, Hungary",
        location = Location(46.83, 17.73),
        placeCategory = PlaceCategory.LAKE,
        osmType = OsmType.RELATION,
    ),
    Place(
        osmId = "6",
        name = "Dobogókői Kilátó",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Viewpoint, Pilis",
        location = Location(47.72, 18.89),
        placeCategory = PlaceCategory.VIEWPOINT,
        osmType = OsmType.NODE,
        distance = "2 km",
    ),
    Place(
        osmId = "7",
        name = "Visegrádi vár",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Castle, Visegrád",
        location = Location(47.79, 18.97),
        placeCategory = PlaceCategory.CASTLE,
        osmType = OsmType.WAY,
    ),
    Place(
        osmId = "8",
        name = "Gulyás Csárda",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Restaurant, Budapest",
        location = Location(47.5, 19.05),
        placeCategory = PlaceCategory.RESTAURANT,
        osmType = OsmType.NODE,
    ),
)
