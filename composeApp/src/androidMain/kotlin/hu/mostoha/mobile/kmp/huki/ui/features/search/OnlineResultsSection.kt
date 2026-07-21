package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoViewData
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun OnlineResultsSection(
    places: List<Place>,
    error: InfoViewData?,
    isLoading: Boolean,
    onPlaceSelected: (Place) -> Unit,
    onRetryClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (places.isEmpty() && !isLoading && error == null) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.SectionSpacing)
            .testTag(TestTags.SEARCH_ONLINE_SECTION),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionHeaderSpacing),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mokoString(SharedRes.strings.search_online_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
            )
            LocationIqAttribution(onClick = onLocationIqClicked)
        }
        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.ExtraLarge),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoadingIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(Dimens.Medium))
                Text(
                    text = mokoString(SharedRes.strings.search_searching),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (places.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Large),
                verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
            ) {
                places.forEach { place ->
                    SearchResultItem(
                        place = place,
                        onClick = { onPlaceSelected(place) },
                    )
                }
            }
        } else if (error != null) {
            InfoView(
                infoViewData = error,
                primaryActionText = mokoString(SharedRes.strings.search_error_retry),
                onPrimaryActionClick = onRetryClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.Large, start = Dimens.Large, end = Dimens.Large),
            )
        }
    }
}

@Composable
private fun LocationIqAttribution(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentDescription = mokoString(SharedRes.strings.menu_a11y_open_location_iq)
    Row(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { this.contentDescription = contentDescription }
            .padding(Dimens.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mokoString(SharedRes.strings.search_powered_by),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(Dimens.Small))
        Icon(
            painter = painterResource(id = SharedRes.images.ic_location_iq_logo.drawableResId),
            contentDescription = null,
            modifier = Modifier.height(18.dp),
            tint = Color.Unspecified,
        )
    }
}

@Preview
@Composable
private fun OnlineResultsSectionPreview() {
    HuKiTheme {
        OnlineResultsSection(
            places = listOf(
                Place(
                    osmId = "1",
                    name = "Dobogókő",
                    placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                    address = "Pilis Mountains, Hungary",
                    location = Location(47.7181, 18.8948),
                    placeCategory = PlaceCategory.PEAK,
                ),
            ),
            error = null,
            isLoading = false,
            onPlaceSelected = {},
            onRetryClicked = {},
            onLocationIqClicked = {},
        )
    }
}

@Preview(name = "Online error")
@Composable
private fun OnlineResultsSectionErrorPreview() {
    HuKiTheme {
        OnlineResultsSection(
            places = emptyList(),
            error = NetworkError.NO_INTERNET.toInfoViewData(),
            isLoading = false,
            onPlaceSelected = {},
            onRetryClicked = {},
            onLocationIqClicked = {},
        )
    }
}
