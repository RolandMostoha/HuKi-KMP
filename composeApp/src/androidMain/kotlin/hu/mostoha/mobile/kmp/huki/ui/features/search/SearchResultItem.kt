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
import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage

@Composable
fun SearchResultItem(place: Place, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subtitle = place.subtitle
    val category = place.placeCategory
    val backgroundColor = if (category != null) {
        mokoColor(category.categoryColorRes)
    } else {
        mokoColor(SharedRes.colors.colorPlaceCategoryFallback)
    }
    val iconRes = category?.iconRes ?: osmIconRes(place.osmType)

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
                    text = place.title,
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
            Text(
                text = "2 km",
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

private fun osmIconRes(osmType: OsmType?): ImageResource =
    when (osmType) {
        OsmType.WAY -> SharedRes.images.ic_place_type_way
        OsmType.RELATION -> SharedRes.images.ic_place_type_relation
        OsmType.NODE, null -> SharedRes.images.ic_place_type_node
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
        id = "1",
        title = "Unknown Node",
        subtitle = "Fallback icon - node",
        location = Location(47.0, 19.0),
        osmType = OsmType.NODE,
    ),
    Place(
        id = "2",
        title = "Unknown Way",
        subtitle = "Fallback icon - way",
        location = Location(47.0, 19.0),
        osmType = OsmType.WAY,
    ),
    Place(
        id = "3",
        title = "Unknown Relation",
        subtitle = "Fallback icon - relation",
        location = Location(47.0, 19.0),
        osmType = OsmType.RELATION,
    ),
    Place(
        id = "4",
        title = "Dobogókő",
        subtitle = "Pilis Mountains, Hungary",
        location = Location(47.7181, 18.8948),
        placeCategory = PlaceCategory.PEAK,
        osmType = OsmType.NODE,
    ),
    Place(
        id = "5",
        title = "Balaton",
        subtitle = "Lake, Transdanubia, Hungary",
        location = Location(46.83, 17.73),
        placeCategory = PlaceCategory.LAKE,
        osmType = OsmType.RELATION,
    ),
    Place(
        id = "6",
        title = "Dobogókői Kilátó",
        subtitle = "Viewpoint, Pilis",
        location = Location(47.72, 18.89),
        placeCategory = PlaceCategory.VIEWPOINT,
        osmType = OsmType.NODE,
    ),
    Place(
        id = "7",
        title = "Visegrádi vár",
        subtitle = "Castle, Visegrád",
        location = Location(47.79, 18.97),
        placeCategory = PlaceCategory.CASTLE,
        osmType = OsmType.WAY,
    ),
    Place(
        id = "8",
        title = "Gulyás Csárda",
        subtitle = "Restaurant, Budapest",
        location = Location(47.5, 19.05),
        placeCategory = PlaceCategory.RESTAURANT,
        osmType = OsmType.NODE,
    ),
)
