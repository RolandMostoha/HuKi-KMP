package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.core.content.ContextCompat
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.WaypointMarkerOrder
import hu.mostoha.mobile.kmp.huki.model.mapper.toLineString
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.util.mokoPainter
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val ROUTE_PLAN_LAYER_ID = "route_plan"

private const val DRAGGED_MARKER_SCALE = 1.25f
private const val PULSE_MAX_SCALE = 2.4f
private const val PULSE_ALPHA = 0.45f
private const val PULSE_DURATION_MILLIS = 1400
private val MarkerTouchTarget = 44.dp

@Composable
@MapboxMapComposable
fun RoutePlanLayer(
    routePlan: RoutePlan?,
    markers: List<GpxWaypoint>,
    selectedMarker: GpxWaypoint?,
    isRoutePlanLoading: Boolean,
    routeColor: Color,
    routeStrokeColor: Color,
    onMarkerDropped: (from: Location, to: Location) -> Unit,
    onMarkerClick: (Location) -> Unit,
) {
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    MapEffect(Unit) { mapView -> mapboxMap = mapView.mapboxMap }
    if (routePlan != null) {
        val geoJsonSource = rememberGeoJsonSource {
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
            RoutePlanMarkerAnnotation(
                waypoint = waypoint,
                isSelected = waypoint == selectedMarker,
                isRoutePlanLoading = isRoutePlanLoading,
                mapboxMap = mapboxMap,
                pulseColor = routeColor,
                onDropped = onMarkerDropped,
                onClick = { onMarkerClick(waypoint.location) },
            )
        }
    }
}

private fun Location.movedBy(delta: Offset, map: MapboxMap): Location {
    val origin = map.pixelForCoordinate(toPoint())
    val point = map.coordinateForPixel(ScreenCoordinate(origin.x + delta.x, origin.y + delta.y))
    return Location(latitude = point.latitude(), longitude = point.longitude())
}

// A plan's stops must stay visible over the location puck, and a ViewAnnotation is a real view
// above every style layer, unlike the PointAnnotation symbols the GPX route draws.
@Composable
private fun RoutePlanMarkerAnnotation(
    waypoint: GpxWaypoint,
    isSelected: Boolean,
    isRoutePlanLoading: Boolean,
    mapboxMap: MapboxMap?,
    pulseColor: Color,
    onDropped: (from: Location, to: Location) -> Unit,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val stopLabel = mokoString(SharedRes.strings.route_planner_a11y_select_stop)
    val icon = waypoint.type.icon
    val markerSize = rememberMarkerSize(waypoint.type, icon)
    // A drop keeps the marker where it landed while the route replans, then it snaps to where the route puts it
    var markerLocation by remember { mutableStateOf(waypoint.location) }
    var droppedLocation by remember { mutableStateOf(waypoint.location) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(isRoutePlanLoading) {
        if (!isRoutePlanLoading && !isDragging) {
            markerLocation = waypoint.location
            droppedLocation = waypoint.location
        }
    }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val selectedScale = if (isSelected) SharedDimens.ROUTE_PLAN_SELECTED_MARKER_SCALE.toFloat() else 1f
    val markerScale by animateFloatAsState(
        targetValue = if (isDragging) selectedScale * DRAGGED_MARKER_SCALE else selectedScale,
        label = "markerScale",
    )
    ViewAnnotation(
        options = ViewAnnotationOptions.Builder()
            .annotatedFeature(AnnotatedFeature(markerLocation.toPoint()))
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .ignoreCameraPadding(true)
            .build(),
    ) {
        // A resized annotation is placed by its old size for a frame, so it always keeps room for the selected pulse
        Box(
            modifier = Modifier.size(
                width = markerSize.width * PULSE_MAX_SCALE,
                height = markerSize.height * PULSE_MAX_SCALE,
            ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                RoutePlanMarkerPulse(
                    diameter = maxOf(markerSize.width, markerSize.height),
                    color = pulseColor,
                )
            }
            // The icon is smaller than a finger, so the touch area around it is larger than what it draws
            Box(
                modifier = Modifier
                    .size(
                        width = max(markerSize.width, MarkerTouchTarget),
                        height = max(markerSize.height, MarkerTouchTarget),
                    )
                    .onGloballyPositioned { layoutCoordinates = it }
                    .clickable(
                        interactionSource = null,
                        // The marker's own selected state is the feedback, a ripple would fill its square bounds
                        indication = null,
                        onClickLabel = stopLabel,
                        role = Role.Button,
                        onClick = onClick,
                    )
                    .pointerInput(mapboxMap) {
                        val map = mapboxMap ?: return@pointerInput
                        // The annotation view itself moves with the marker, so the finger is followed in window space
                        var lastWindowPosition = Offset.Zero
                        detectDragGesturesAfterLongPress(
                            onDragStart = { position ->
                                lastWindowPosition = layoutCoordinates?.localToWindow(position) ?: position
                                isDragging = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val windowPosition = layoutCoordinates?.localToWindow(change.position)
                                if (windowPosition != null) {
                                    markerLocation = markerLocation.movedBy(windowPosition - lastWindowPosition, map)
                                    lastWindowPosition = windowPosition
                                }
                            },
                            onDragEnd = {
                                if (markerLocation != droppedLocation) {
                                    onDropped(droppedLocation, markerLocation)
                                    droppedLocation = markerLocation
                                }
                                isDragging = false
                            },
                            onDragCancel = {
                                markerLocation = droppedLocation
                                isDragging = false
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier
                        .size(width = markerSize.width, height = markerSize.height)
                        .graphicsLayer {
                            scaleX = markerScale
                            scaleY = markerScale
                        },
                    painter = mokoPainter(icon),
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun rememberMarkerSize(type: WaypointType, icon: ImageResource): DpSize {
    val context = LocalContext.current
    val density = LocalDensity.current
    return remember(type, icon) {
        val scale = type.markerScale.toFloat()
        val drawable = ContextCompat.getDrawable(context, icon.drawableResId)
        with(density) {
            DpSize(
                width = (drawable?.intrinsicWidth ?: 0).toDp() * scale,
                height = (drawable?.intrinsicHeight ?: 0).toDp() * scale,
            )
        }
    }
}

@Composable
private fun RoutePlanMarkerPulse(diameter: Dp, color: Color) {
    val transition = rememberInfiniteTransition(label = "markerPulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = PULSE_DURATION_MILLIS, easing = LinearOutSlowInEasing),
        ),
        label = "markerPulseProgress",
    )
    Box(
        modifier = Modifier
            .size(diameter)
            .graphicsLayer {
                val scale = 1f + (PULSE_MAX_SCALE - 1f) * progress
                scaleX = scale
                scaleY = scale
                alpha = 1f - progress
            }
            .background(color.copy(alpha = PULSE_ALPHA), CircleShape),
    )
}
