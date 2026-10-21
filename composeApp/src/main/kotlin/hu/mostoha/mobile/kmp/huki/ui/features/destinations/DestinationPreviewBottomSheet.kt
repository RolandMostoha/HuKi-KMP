package hu.mostoha.mobile.kmp.huki.ui.features.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.ui.components.SecondaryButton
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationPreviewBottomSheet(
    destination: Destination,
    sheetState: SheetState,
    onShowOnMapClick: () -> Unit,
    onRouteClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    landscapeText: String? = null,
    distanceText: String? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle(verticalPadding = Dimens.MediumLarge) },
    ) {
        Column(
            modifier = modifier
                .navigationBarsPadding()
                .padding(horizontal = Dimens.Large)
                .padding(bottom = Dimens.Medium)
                .testTagAsResourceId(TestTags.DESTINATION_PREVIEW),
        ) {
            DestinationPreviewCard(
                destination = destination,
                landscapeText = landscapeText,
                distanceText = distanceText,
            )
            DestinationPreviewActions(
                destination = destination,
                onShowOnMapClick = onShowOnMapClick,
                onRouteClick = onRouteClick,
                onWebsiteClick = onWebsiteClick,
                modifier = Modifier.padding(top = Dimens.Large),
            )
        }
    }
}

@Composable
private fun DestinationPreviewActions(
    destination: Destination,
    onShowOnMapClick: () -> Unit,
    onRouteClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        SecondaryButton(
            iconResId = R.drawable.ic_maps,
            text = mokoString(SharedRes.strings.destinations_action_show_on_map),
            onClick = onShowOnMapClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTagAsResourceId(TestTags.DESTINATION_PREVIEW_SHOW_ON_MAP_BUTTON),
        )
        if (destination.routeGpxFile != null) {
            SecondaryButton(
                iconResId = R.drawable.ic_route,
                text = mokoString(SharedRes.strings.destinations_action_open_route),
                onClick = onRouteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTagAsResourceId(TestTags.DESTINATION_PREVIEW_ROUTE_BUTTON),
            )
        }
        if (destination.website != null) {
            SecondaryButton(
                iconResId = R.drawable.ic_language,
                text = mokoString(SharedRes.strings.destinations_action_open_website),
                onClick = onWebsiteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTagAsResourceId(TestTags.DESTINATION_PREVIEW_WEBSITE_BUTTON),
            )
        }
    }
}

@Preview
@Composable
private fun DestinationPreviewBottomSheetPreview() {
    HuKiTheme {
        Column(modifier = Modifier.padding(Dimens.Large)) {
            DestinationPreviewCard(
                destination = previewDestination,
                landscapeText = "Mátra",
                distanceText = "89 km",
            )
            DestinationPreviewActions(
                destination = previewDestination,
                onShowOnMapClick = {},
                onRouteClick = {},
                onWebsiteClick = {},
                modifier = Modifier.padding(top = Dimens.Large),
            )
        }
    }
}

@Preview
@Composable
private fun DestinationPreviewBottomSheetAllActionsPreview() {
    HuKiTheme {
        Column(modifier = Modifier.padding(Dimens.Large)) {
            DestinationPreviewCard(
                destination = previewTrailDestination,
                landscapeText = "Budai-hegység",
            )
            DestinationPreviewActions(
                destination = previewTrailDestination,
                onShowOnMapClick = {},
                onRouteClick = {},
                onWebsiteClick = {},
                modifier = Modifier.padding(top = Dimens.Large),
            )
        }
    }
}

private val previewDestination = Destination(
    id = "1",
    name = "Kékestető",
    town = "Mátraszentimre",
    type = DestinationType.PEAK,
    location = Location(47.8709, 20.0106),
    description = SharedRes.strings.destinations_section_title,
    popularity = 10,
)

private val previewTrailDestination = Destination(
    id = "2",
    name = "Jági tanösvény",
    town = "Pilisszentiván",
    type = DestinationType.EDUCATIONAL_TRAIL,
    location = Location(47.6120670, 18.8845000),
    description = SharedRes.strings.destinations_section_title,
    popularity = 4,
    routeGpxFile = SharedRes.files.edut_jagi_tanosveny_gpx,
    website = "https://pilisiparkerdo.hu",
)
