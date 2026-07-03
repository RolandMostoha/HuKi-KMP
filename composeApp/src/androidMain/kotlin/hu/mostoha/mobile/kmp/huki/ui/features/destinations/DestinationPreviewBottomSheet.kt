package hu.mostoha.mobile.kmp.huki.ui.features.destinations

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
            SecondaryButton(
                iconResId = R.drawable.ic_maps,
                text = mokoString(SharedRes.strings.destinations_action_show_on_map),
                onClick = onShowOnMapClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.Large)
                    .testTagAsResourceId(TestTags.DESTINATION_PREVIEW_SHOW_ON_MAP_BUTTON),
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
            SecondaryButton(
                iconResId = R.drawable.ic_maps,
                text = mokoString(SharedRes.strings.destinations_action_show_on_map),
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.Large),
            )
        }
    }
}

private val previewDestination = Destination(
    osmId = "1",
    name = "Kékestető",
    town = "Mátraszentimre",
    type = DestinationType.PEAK,
    location = Location(47.8709, 20.0106),
    description = SharedRes.strings.destinations_section_title,
    popularity = 10,
)
