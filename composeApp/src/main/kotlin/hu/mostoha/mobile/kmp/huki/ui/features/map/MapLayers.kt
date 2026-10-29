package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.StringValue
import com.mapbox.maps.extension.compose.style.layers.ImageValue
import com.mapbox.maps.extension.compose.style.layers.generated.IconRotationAlignmentValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.compose.style.layers.generated.SymbolPlacementValue
import com.mapbox.maps.extension.compose.style.rememberStyleImage
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.generated.Expression
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.SLOPE_COLOR_PROPERTY
import hu.mostoha.mobile.kmp.huki.theme.MapLighting
import hu.mostoha.mobile.kmp.huki.theme.OutdoorsColorTheme
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import kotlin.math.ceil

@Composable
@MapboxMapComposable
fun RouteLineLayer(
    sourceState: GeoJsonSourceState,
    layerId: String,
    routeColor: Color,
    routeStrokeColor: Color,
) {
    LineLayer(
        sourceState = sourceState,
        layerId = layerId,
    ) {
        lineWidth = DoubleValue(SharedDimens.GPX_LINE_WIDTH)
        lineColor = ColorValue(routeColor)
        lineBorderColor = ColorValue(routeStrokeColor)
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineBorderColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
        lineBorderWidth = DoubleValue(SharedDimens.MAP_LINE_STROKE_WIDTH)
        lineCap = LineCapValue.ROUND
        lineJoin = LineJoinValue.ROUND
    }
}

@Composable
@MapboxMapComposable
fun SlopeRouteLineLayer(
    routeSourceState: GeoJsonSourceState,
    slopeSourceState: GeoJsonSourceState,
    layerId: String,
    casingColor: Color,
) {
    LineLayer(
        sourceState = routeSourceState,
        layerId = "${layerId}_casing",
    ) {
        lineWidth = DoubleValue(SharedDimens.GPX_LINE_WIDTH)
        lineColor = ColorValue(casingColor)
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
        lineCap = LineCapValue.ROUND
        lineJoin = LineJoinValue.ROUND
    }
    LineLayer(
        sourceState = slopeSourceState,
        layerId = layerId,
    ) {
        lineWidth = DoubleValue(SharedDimens.GPX_LINE_WIDTH - 2 * SharedDimens.MAP_LINE_STROKE_WIDTH)
        lineColor = ColorValue(Expression.get(SLOPE_COLOR_PROPERTY))
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
        lineCap = LineCapValue.ROUND
        lineJoin = LineJoinValue.ROUND
    }
}

private const val DIRECTION_ARROW_IMAGE_ID = "route_direction_arrow"

@Composable
@MapboxMapComposable
fun RouteDirectionLayer(sourceState: GeoJsonSourceState, layerId: String, arrowColor: Color) {
    val density = LocalDensity.current
    val arrowBitmap = remember(density) { directionArrowBitmap(density) }
    val arrowImage = rememberStyleImage(
        imageId = DIRECTION_ARROW_IMAGE_ID,
        imageBitmap = arrowBitmap,
        scale = density.density,
        sdf = true,
    )
    SymbolLayer(
        sourceState = sourceState,
        layerId = layerId,
    ) {
        minZoom = LongValue(SharedDimens.GPX_DIRECTION_ARROW_MIN_ZOOM)
        symbolPlacement = SymbolPlacementValue.LINE
        symbolSpacing = DoubleValue(SharedDimens.GPX_DIRECTION_ARROW_SPACING)
        iconImage = ImageValue(arrowImage)
        // An SDF icon takes its color from iconColor, which can opt out of the dark map's color grading
        iconColor = ColorValue(arrowColor)
        iconColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        iconEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
        iconRotationAlignment = IconRotationAlignmentValue.MAP
        // Upright icons flip on westbound segments, which would point arrows the wrong way
        iconKeepUpright = BooleanValue(false)
        iconAllowOverlap = BooleanValue(true)
        iconIgnorePlacement = BooleanValue(true)
    }
}

/**
 * A notched arrowhead pointing along +x, the direction of the line in a line-placed symbol.
 */
private fun directionArrowBitmap(density: Density): ImageBitmap {
    val length = with(density) { SharedDimens.GPX_DIRECTION_ARROW_LENGTH.dp.toPx() }
    val width = with(density) { SharedDimens.GPX_DIRECTION_ARROW_WIDTH.dp.toPx() }
    val notch = length * SharedDimens.GPX_DIRECTION_ARROW_NOTCH_RATIO.toFloat()
    val bitmap = ImageBitmap(ceil(length).toInt(), ceil(width).toInt())
    val arrowhead = Path().apply {
        moveTo(0f, 0f)
        lineTo(length, width / 2)
        lineTo(0f, width)
        lineTo(notch, width / 2)
        close()
    }
    Canvas(bitmap).drawPath(arrowhead, Paint().apply { color = Color.White })
    return bitmap
}

val WaypointType.markerScale: Double
    get() = if (this == WaypointType.INTERMEDIATE) {
        SharedDimens.GPX_WAYPOINT_MARKER_SCALE
    } else {
        SharedDimens.GPX_EDGE_LOCATION_MARKER_SCALE
    }
