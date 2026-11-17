package hu.mostoha.mobile.kmp.huki.ui.features.map

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
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
import hu.mostoha.mobile.kmp.huki.theme.DirectionArrowSdf
import hu.mostoha.mobile.kmp.huki.theme.MapLighting
import hu.mostoha.mobile.kmp.huki.theme.OutdoorsColorTheme
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens

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
fun RouteDirectionLayer(
    sourceState: GeoJsonSourceState,
    layerId: String,
    arrowColor: Color,
    arrowHaloColor: Color,
) {
    val density = LocalDensity.current
    val arrowBitmap = remember(density) { directionArrowBitmap(density.density) }
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
        iconHaloColor = ColorValue(arrowHaloColor)
        iconHaloColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        iconHaloWidth = DoubleValue(SharedDimens.GPX_DIRECTION_ARROW_HALO_WIDTH)
        iconEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
        iconRotationAlignment = IconRotationAlignmentValue.MAP
        // Upright icons flip on westbound segments, which would point arrows the wrong way
        iconKeepUpright = BooleanValue(false)
        iconAllowOverlap = BooleanValue(true)
        iconIgnorePlacement = BooleanValue(true)
    }
}

private fun directionArrowBitmap(density: Float): ImageBitmap {
    val sdf = DirectionArrowSdf.create(density)
    val pixels = IntArray(sdf.alpha.size) { index -> (sdf.alpha[index] shl ALPHA_SHIFT) or WHITE_RGB }
    return Bitmap.createBitmap(pixels, sdf.widthPx, sdf.heightPx, Bitmap.Config.ARGB_8888).asImageBitmap()
}

private const val ALPHA_SHIFT = 24
private const val WHITE_RGB = 0xFFFFFF

val WaypointType.markerScale: Double
    get() = if (this == WaypointType.INTERMEDIATE) {
        SharedDimens.GPX_WAYPOINT_MARKER_SCALE
    } else {
        SharedDimens.GPX_EDGE_LOCATION_MARKER_SCALE
    }
