package hu.mostoha.mobile.kmp.huki.ui.features.maplabelscale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.MapLabelScale
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLabelScaleBottomSheet(
    sheetState: SheetState,
    labelScale: MapLabelScale,
    onFactorChanged: (Float) -> Unit,
    onFactorCommitted: (Float) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Transparent,
        dragHandle = null,
        sheetState = sheetState,
    ) {
        MapLabelScaleContent(
            labelScale = labelScale,
            onFactorChanged = onFactorChanged,
            onFactorCommitted = onFactorCommitted,
            onDismissRequest = onDismissRequest,
        )
    }
}

@Composable
private fun MapLabelScaleContent(
    labelScale: MapLabelScale,
    onFactorChanged: (Float) -> Unit,
    onFactorCommitted: (Float) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val fontScale = LocalConfiguration.current.fontScale
    var draggedFactor by remember { mutableStateOf<Float?>(null) }
    val displayedFactor = draggedFactor ?: labelScale.effectiveFactor(fontScale)
    val percentText = mokoString(SharedRes.strings.map_label_scale_percent, MapLabelScale.toPercent(displayedFactor))
    val title = mokoString(SharedRes.strings.map_label_scale_title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTagAsResourceId(TestTags.MAP_LABEL_SCALE_SHEET)
            .padding(
                start = Dimens.ExtraLarge,
                top = Dimens.Large,
                end = Dimens.Large,
                bottom = Dimens.ModalSheetBottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onDismissRequest,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.testTagAsResourceId(TestTags.MAP_LABEL_SCALE_CLOSE_BUTTON),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = mokoString(SharedRes.strings.a11y_close),
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Slider(
                value = displayedFactor,
                onValueChange = { factor ->
                    draggedFactor = factor
                    onFactorChanged(factor)
                },
                onValueChangeFinished = {
                    draggedFactor?.let(onFactorCommitted)
                    draggedFactor = null
                },
                valueRange = MapLabelScale.MIN_FACTOR..MapLabelScale.MAX_FACTOR,
                steps = MapLabelScale.FACTOR_STEPS - 1,
                modifier = Modifier
                    .weight(1f)
                    .testTagAsResourceId(TestTags.MAP_LABEL_SCALE_SLIDER)
                    .semantics {
                        contentDescription = title
                        stateDescription = percentText
                    },
            )
            Text(
                text = "A",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                text = percentText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .widthIn(min = SharedDimens.MAP_LABEL_SCALE_PERCENT_MIN_WIDTH.dp)
                    .clearAndSetSemantics {},
            )
        }
        Row(
            modifier = Modifier.padding(top = Dimens.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.IconExtraSmall),
            )
            Text(
                text = mokoString(SharedRes.strings.map_label_scale_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun MapLabelScaleBottomSheetPreview() {
    HuKiTheme {
        MapLabelScaleBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            labelScale = MapLabelScale.DEFAULT,
            onFactorChanged = {},
            onFactorCommitted = {},
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun MapLabelScaleContentPreview() {
    HuKiTheme {
        MapLabelScaleContent(
            labelScale = MapLabelScale(followSystem = false, factor = 1.5f),
            onFactorChanged = {},
            onFactorCommitted = {},
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun MapLabelScaleContentDarkPreview() {
    HuKiTheme(themeMode = ThemeMode.DARK) {
        MapLabelScaleContent(
            labelScale = MapLabelScale(followSystem = false, factor = 1.8f),
            onFactorChanged = {},
            onFactorCommitted = {},
            onDismissRequest = {},
        )
    }
}
