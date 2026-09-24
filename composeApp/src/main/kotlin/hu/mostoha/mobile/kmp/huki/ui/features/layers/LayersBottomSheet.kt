package hu.mostoha.mobile.kmp.huki.ui.features.layers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
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
        dragHandle = null,
        sheetState = sheetState,
    ) {
        Column(
            modifier = modifier
                .padding(
                    start = Dimens.Medium,
                    top = Dimens.Medium,
                    end = Dimens.Medium,
                    bottom = Dimens.Large,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = mokoString(SharedRes.strings.layers_base_layers_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onDismissRequest() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                            contentDescription = mokoString(SharedRes.strings.a11y_close),
                        )
                    }
                },
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = Dimens.ExtraLarge),
                horizontalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                BaseLayer.entries.forEach { baseLayer ->
                    LayersItem(
                        title = mokoString(baseLayer.title),
                        imageRes = baseLayer.image.drawableResId,
                        selected = baseLayer == selectedBaseLayer,
                        onClick = {
                            onBaseLayerSelected(baseLayer)
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.Medium))
            Text(
                text = mokoString(SharedRes.strings.layers_overlay_layers_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(Dimens.Medium))
            Row(
                modifier = Modifier
                    .padding(horizontal = Dimens.ExtraLarge),
                horizontalArrangement = Arrangement.spacedBy(56.dp),
            ) {
                LayersItem(
                    title = mokoString(SharedRes.strings.layers_overlay_hiking_title),
                    imageRes = SharedRes.images.ic_layers_hiking.drawableResId,
                    selected = isHikingLayerSelected,
                    onClick = {
                        onHikingLayerSelected()
                    },
                )
                LayersItem(
                    title = mokoString(SharedRes.strings.layers_overlay_gpx_title),
                    imageRes = SharedRes.images.ic_layers_gpx.drawableResId,
                    selected = isGpxLayerSelected,
                    onClick = {
                        onGpxLayerSelected()
                    },
                )
            }
        }
    }
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
            selectedBaseLayer = BaseLayer.SATELLITE,
            onBaseLayerSelected = {},
            onHikingLayerSelected = {},
            onGpxLayerSelected = {},
            onDismissRequest = {},
        )
    }
}
