package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.mapper.WaypointMarkerOrder
import hu.mostoha.mobile.kmp.huki.model.mapper.toLineString
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint

private const val ROUTE_PLAN_LAYER_ID = "route_plan"

@Composable
@MapboxMapComposable
fun RoutePlanLayer(
    routePlan: RoutePlan?,
    markers: List<GpxWaypoint>,
    routeColor: Color,
    routeStrokeColor: Color,
) {
    if (routePlan != null) {
        val geoJsonSource = rememberGeoJsonSourceState(key = ROUTE_PLAN_LAYER_ID) {
            lineMetrics = BooleanValue(true)
        }
        LaunchedEffect(routePlan.locations) {
            geoJsonSource.data = GeoJSONData(routePlan.locations.toLineString())
        }
        RouteLineLayer(
            sourceState = geoJsonSource,
            layerId = ROUTE_PLAN_LAYER_ID,
            routeColor = routeColor,
            routeStrokeColor = routeStrokeColor,
        )
    }
    WaypointMarkerOrder.sort(markers).forEach { waypoint ->
        key(waypoint.location, waypoint.type) {
            RoutePlanMarkerAnnotation(waypoint = waypoint)
        }
    }
}

// A plan's stops must stay visible over the location puck, and a ViewAnnotation is a real view
// above every style layer, unlike the PointAnnotation symbols the GPX route draws.
@Composable
private fun RoutePlanMarkerAnnotation(waypoint: GpxWaypoint) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val iconResId = waypoint.type.icon.drawableResId
    val markerSize = remember(iconResId, waypoint.type) {
        val scale = waypoint.type.markerScale.toFloat()
        val drawable = ContextCompat.getDrawable(context, iconResId)
        with(density) {
            (drawable?.intrinsicWidth ?: 0).toDp() * scale to (drawable?.intrinsicHeight ?: 0).toDp() * scale
        }
    }
    ViewAnnotation(
        options = ViewAnnotationOptions.Builder()
            .annotatedFeature(AnnotatedFeature(waypoint.location.toPoint()))
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .build(),
    ) {
        Image(
            modifier = Modifier.size(width = markerSize.first, height = markerSize.second),
            painter = painterResource(id = iconResId),
            contentDescription = null,
        )
    }
}
