package hu.mostoha.mobile.kmp.huki.ui.features.layers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.ui.components.DragHandle
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun LayersBottomSheet(
    sheetState: SheetState,
    selectedBaseLayer: BaseLayer,
    isHikingLayerSelected: Boolean,
    isGpxLayerSelected: Boolean,
    onBaseLayerSelected: (BaseLayer) -> Unit,
    onHikingLayerSelected: () -> Unit,
    onGpxLayerSelected: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState,
        dragHandle = {
            DragHandle(
                modifier = Modifier.padding(top = Dimens.Small),
                verticalPadding = Dimens.SmallMedium,
            )
        },
    ) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Dimens.Large,
                    end = Dimens.Large,
                    bottom = Dimens.ModalSheetBottomPadding,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mokoString(SharedRes.strings.layers_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
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
            LayersSectionTitle(text = mokoString(SharedRes.strings.layers_base_layers_title))
            Row(modifier = Modifier.fillMaxWidth()) {
                BaseLayer.entries.forEach { baseLayer ->
                    LayersItem(
                        title = mokoString(baseLayer.title),
                        imageRes = baseLayer.image.drawableResId,
                        selected = baseLayer == selectedBaseLayer,
                        onClick = { onBaseLayerSelected(baseLayer) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = Dimens.Large),
                color = MaterialTheme.colorScheme.dividerColor,
            )
            LayersSectionTitle(text = mokoString(SharedRes.strings.layers_overlay_layers_title))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraLarge)) {
                LayersItem(
                    title = mokoString(SharedRes.strings.layers_overlay_hiking_title),
                    imageRes = SharedRes.images.ic_layers_hiking.drawableResId,
                    selected = isHikingLayerSelected,
                    showCheckmark = true,
                    onClick = onHikingLayerSelected,
                )
                LayersItem(
                    title = mokoString(SharedRes.strings.layers_overlay_gpx_title),
                    imageRes = SharedRes.images.ic_layers_gpx.drawableResId,
                    selected = isGpxLayerSelected,
                    showCheckmark = true,
                    onClick = onGpxLayerSelected,
                )
            }
        }
    }
}

@Composable
private fun LayersSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = Dimens.Large, bottom = Dimens.Medium),
    )
}

@Preview
@Composable
private fun LayersBottomSheetPreview() {
    HuKiTheme {
        LayersBottomSheet(
            sheetState = SheetState(
                skipPartiallyExpanded = true,
                positionalThreshold = { 0f },
                velocityThreshold = { 0f },
            ),
            isHikingLayerSelected = true,
            isGpxLayerSelected = false,
            selectedBaseLayer = BaseLayer.TUHU,
            onBaseLayerSelected = {},
            onHikingLayerSelected = {},
            onGpxLayerSelected = {},
            onDismissRequest = {},
        )
    }
}
