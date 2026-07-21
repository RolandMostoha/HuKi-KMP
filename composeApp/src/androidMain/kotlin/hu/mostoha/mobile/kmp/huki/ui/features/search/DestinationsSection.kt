package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.SectionHeader
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun DestinationsSection(
    destinations: List<Destination>,
    onDestinationSelected: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    topPadding: Dp = Dimens.SectionSpacing,
    onSeeAllClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .padding(top = topPadding)
            .testTag(TestTags.DESTINATIONS_SECTION),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionHeaderSpacing),
    ) {
        SectionHeader(
            title = mokoString(SharedRes.strings.destinations_section_title),
            actionText = onSeeAllClick?.let { mokoString(SharedRes.strings.see_all) },
            onActionClick = onSeeAllClick,
            actionModifier = Modifier.testTag(TestTags.DESTINATIONS_SEE_ALL_BUTTON),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
            contentPadding = PaddingValues(horizontal = Dimens.Large),
            overscrollEffect = null,
        ) {
            items(
                items = destinations,
                key = { it.osmId },
            ) { destination ->
                DestinationCard(
                    destination = destination,
                    onClick = { onDestinationSelected(destination) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun DestinationsSectionPreview() {
    HuKiTheme {
        DestinationsSection(
            destinations = previewDestinations,
            onDestinationSelected = {},
            onSeeAllClick = {},
        )
    }
}

private val previewDestinations = listOf(
    Destination(
        osmId = "1",
        name = "Dobogókő",
        town = "Pilisszentkereszt",
        type = DestinationType.PEAK,
        location = Location(47.7181, 18.8948),
        description = SharedRes.strings.destinations_section_title,
        popularity = 10,
    ),
    Destination(
        osmId = "2",
        name = "Balaton",
        town = "Siófok",
        type = DestinationType.LAKE,
        location = Location(46.83, 17.73),
        description = SharedRes.strings.destinations_section_title,
        popularity = 9,
    ),
)
