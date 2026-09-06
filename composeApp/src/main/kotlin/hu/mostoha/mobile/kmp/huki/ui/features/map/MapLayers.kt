package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
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
        lineBorderWidth = DoubleValue(SharedDimens.GPX_STROKE_WIDTH)
        lineCap = LineCapValue.ROUND
        lineJoin = LineJoinValue.ROUND
    }
}

val WaypointType.markerScale: Double
    get() = if (this == WaypointType.INTERMEDIATE) {
        SharedDimens.GPX_WAYPOINT_MARKER_SCALE
    } else {
        SharedDimens.GPX_EDGE_LOCATION_MARKER_SCALE
    }
