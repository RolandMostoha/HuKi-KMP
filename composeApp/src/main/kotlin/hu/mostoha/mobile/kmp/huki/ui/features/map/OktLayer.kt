package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.ViewAnnotationAnchorConfig
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.StringValue
import com.mapbox.maps.extension.compose.style.layers.LayerInteractionsState
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.OktUiEvents
import hu.mostoha.mobile.kmp.huki.features.map.OktUiState
import hu.mostoha.mobile.kmp.huki.model.domain.OktLine
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarkerType
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoWindowTitle
import hu.mostoha.mobile.kmp.huki.model.mapper.toLocation
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.MapLighting
import hu.mostoha.mobile.kmp.huki.theme.OutdoorsColorTheme
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val BASE_LAYER_ID = "okt_base_layer"
private const val BASE_HIT_LAYER_ID = "okt_base_hit_layer"
private const val SELECTED_LAYER_ID = "okt_selected_layer"
private const val LINE_HIT_WIDTH = 24.0
private const val LINE_HIT_OPACITY = 0.01
private const val RAISED_MARKER_PRIORITY = 1L
private const val SELECTED_MARKER_PRIORITY = 2L
private const val INFO_WINDOW_PRIORITY = 3L

@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
fun OktLayer(okt: OktUiState, onEvent: (OktUiEvents) -> Unit) {
    val baseSource = rememberOktLineSource(okt.baseLine)
    val selectedSource = rememberOktLineSource(okt.selectedLine)
    OktLineLayer(
        sourceState = baseSource,
        layerId = BASE_LAYER_ID,
        lineWidth = SharedDimens.OKT_BASE_LINE_WIDTH,
        lineColor = mokoColor(SharedRes.colors.oktBlue),
    )
    // Wider hit area than the visible line; Mapbox skips fully transparent layers in hit-testing
    LineLayer(sourceState = baseSource, layerId = BASE_HIT_LAYER_ID) {
        lineWidth = DoubleValue(LINE_HIT_WIDTH)
        lineColor = ColorValue(Color.Black)
        lineOpacity = DoubleValue(LINE_HIT_OPACITY)
        interactionsState = LayerInteractionsState().onClicked { _, context ->
            onEvent(OktUiEvents.OktLineClicked(context.coordinateInfo.coordinate.toLocation()))
            true
        }
    }
    OktLineLayer(
        sourceState = selectedSource,
        layerId = SELECTED_LAYER_ID,
        lineWidth = SharedDimens.OKT_SELECTED_LINE_WIDTH,
        lineColor = mokoColor(SharedRes.colors.oktBlue),
    )
    okt.markers.forEach { marker ->
        key(marker.id) {
            OktMarkerAnnotation(
                marker = marker,
                isSelected = marker.id == okt.selectedMarkerId,
                priority = okt.markerPriority(marker),
                onClick = { onEvent(OktUiEvents.OktMarkerClicked(marker)) },
            )
        }
    }
    okt.infoWindow?.let { info ->
        key(info.marker.id) {
            val offsetY = with(LocalDensity.current) {
                (info.marker.type.markerSize(isSelected = true) / 2 + Dimens.InfoWindowMarkerPadding).toPx().toDouble()
            }
            ViewAnnotation(
                options = ViewAnnotationOptions.Builder()
                    .annotatedFeature(AnnotatedFeature(info.marker.location.toPoint()))
                    .variableAnchors(
                        listOf(
                            ViewAnnotationAnchorConfig.Builder()
                                .anchor(ViewAnnotationAnchor.BOTTOM)
                                .offsetY(offsetY)
                                .build(),
                        ),
                    )
                    .allowOverlap(true)
                    .allowOverlapWithPuck(true)
                    .priority(INFO_WINDOW_PRIORITY)
                    .build(),
            ) {
                OktInfoWindow(
                    info = info,
                    onPlaceDetailsClick = { onEvent(OktUiEvents.OktInfoWindowPlaceDetailsClicked) },
                )
            }
        }
    }
}

@Composable
private fun rememberOktLineSource(line: OktLine): GeoJsonSourceState {
    val source = rememberGeoJsonSourceState(key = line.id)
    LaunchedEffect(line.id) {
        source.data = GeoJSONData(line.geoJson)
    }
    return source
}

@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun OktLineLayer(
    sourceState: GeoJsonSourceState,
    layerId: String,
    lineWidth: Double,
    lineColor: Color,
) {
    val strokeColor = mokoColor(SharedRes.colors.mapStroke)
    LineLayer(sourceState = sourceState, layerId = layerId) {
        this.lineWidth = DoubleValue(lineWidth)
        this.lineColor = ColorValue(lineColor)
        lineBorderWidth = DoubleValue(SharedDimens.MAP_LINE_STROKE_WIDTH)
        lineBorderColor = ColorValue(strokeColor)
        lineCap = LineCapValue.ROUND
        lineJoin = LineJoinValue.ROUND
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineBorderColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
}

@Composable
private fun OktMarkerAnnotation(
    marker: OktMarker,
    isSelected: Boolean,
    priority: Long,
    onClick: () -> Unit,
) {
    ViewAnnotation(
        options = ViewAnnotationOptions.Builder()
            .annotatedFeature(AnnotatedFeature(marker.location.toPoint()))
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .priority(priority)
            .build(),
    ) {
        val description = mokoString(marker.toInfoWindowTitle())
        OktMarkerView(
            type = marker.type,
            isSelected = isSelected,
            modifier = Modifier
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = description
                },
        )
    }
}

@Composable
fun OktMarkerView(type: OktMarkerType, isSelected: Boolean, modifier: Modifier = Modifier) {
    val oktBlue = mokoColor(SharedRes.colors.oktBlue)
    val onOktBlue = mokoColor(SharedRes.colors.onOktBlue)
    val containerColor by animateColorAsState(if (isSelected) oktBlue else onOktBlue)
    val contentColor by animateColorAsState(if (isSelected) onOktBlue else oktBlue)
    val size by animateDpAsState(type.markerSize(isSelected))
    val stampIconSize by animateDpAsState(if (isSelected) Dimens.IconSmall else Dimens.IconExtraSmall)
    Box(
        modifier = Modifier
            .size(size)
            .shadow(elevation = 2.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 2.dp, color = contentColor, shape = CircleShape)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        when (type) {
            OktMarkerType.STAMP -> Icon(
                painter = painterResource(SharedRes.images.ic_okt_stamp.drawableResId),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(stampIconSize),
            )
            OktMarkerType.START, OktMarkerType.END -> Image(
                painter = painterResource(SharedRes.images.ic_okt_symbol.drawableResId),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun OktMarkerType.markerSize(isSelected: Boolean): Dp =
    when {
        this != OktMarkerType.STAMP -> Dimens.OktEdgeMarkerSize
        isSelected -> Dimens.OktSelectedMarkerSize
        else -> Dimens.OktMarkerSize
    }

/**
 * Start and end stay readable by default; once a stamp is picked, the stamps come forward.
 */
private fun OktUiState.markerPriority(marker: OktMarker): Long {
    if (marker.id == selectedMarkerId) return SELECTED_MARKER_PRIORITY
    val isEdge = marker.type != OktMarkerType.STAMP
    return if (isEdge == (selectedMarkerId == null)) RAISED_MARKER_PRIORITY else 0L
}

@Preview
@Composable
private fun OktMarkerViewPreview() {
    HuKiTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(Dimens.Large),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
        ) {
            OktMarkerView(type = OktMarkerType.STAMP, isSelected = false)
            OktMarkerView(type = OktMarkerType.STAMP, isSelected = true)
            OktMarkerView(type = OktMarkerType.START, isSelected = false)
            OktMarkerView(type = OktMarkerType.END, isSelected = true)
        }
    }
}
