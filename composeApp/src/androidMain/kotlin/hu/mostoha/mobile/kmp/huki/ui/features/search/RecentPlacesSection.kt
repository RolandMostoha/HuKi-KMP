package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.ui.components.PlaceRow
import hu.mostoha.mobile.kmp.huki.ui.components.SectionHeader
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun RecentPlacesSection(
    places: List<Place>,
    onPlaceSelected: (Place) -> Unit,
    onSeeAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.SectionSpacing)
            .testTag(TestTags.RECENT_PLACES_SECTION),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionHeaderSpacing),
    ) {
        SectionHeader(
            title = mokoString(SharedRes.strings.search_recent_places_title),
            actionText = mokoString(SharedRes.strings.see_all),
            onActionClick = onSeeAllClicked,
            actionModifier = Modifier.testTag(TestTags.RECENT_PLACES_SEE_ALL_BUTTON),
        )
        Card(
            modifier = Modifier
                .padding(horizontal = Dimens.Large)
                .padding(top = Dimens.Medium),
            shape = RoundedCornerShape(Dimens.Large),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            places.forEachIndexed { index, place ->
                PlaceRow(
                    place = place,
                    onClick = { onPlaceSelected(place) },
                    modifier = Modifier.testTag(TestTags.RECENT_PLACES_ITEM),
                )
                if (index < places.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Dimens.Large + Dimens.IconContainer + Dimens.MediumLarge),
                        color = MaterialTheme.colorScheme.dividerColor,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RecentPlacesSectionPreview() {
    HuKiTheme {
        RecentPlacesSection(
            places = previewRecentPlaces,
            onPlaceSelected = {},
            onSeeAllClicked = {},
        )
    }
}

private val previewRecentPlaces = listOf(
    Place(
        osmId = "1",
        name = "Dobogókő",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Pilis Mountains, Hungary",
        location = Location(47.7181, 18.8948),
        placeCategory = PlaceCategory.PEAK,
        osmType = OsmType.NODE,
    ),
    Place(
        osmId = "2",
        name = "Balaton",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Lake, Transdanubia, Hungary",
        location = Location(46.83, 17.73),
        placeCategory = PlaceCategory.LAKE,
        osmType = OsmType.RELATION,
    ),
    Place(
        osmId = "3",
        name = "Visegrádi vár",
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = "Castle, Visegrád",
        location = Location(47.79, 18.97),
        placeCategory = PlaceCategory.CASTLE,
        osmType = OsmType.WAY,
    ),
)
