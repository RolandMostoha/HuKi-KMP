package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.ViewAnnotationAnchorConfig
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens

@Composable
fun DistanceInfoWindowAnnotation(
    info: DistanceInfoWindowData,
    gpxDetails: GpxDetails,
    modifier: Modifier = Modifier,
    onEvent: (MainUiEvents) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val waypointType = remember(info.location, gpxDetails.waypoints) {
        gpxDetails.waypoints.find { it.location == info.location }?.type
    }
    val markerHalfHeight = remember(waypointType) {
        val iconScale = if (waypointType == WaypointType.INTERMEDIATE) {
            SharedDimens.GPX_WAYPOINT_MARKER_SCALE
        } else {
            SharedDimens.GPX_EDGE_LOCATION_MARKER_SCALE
        }
        val iconHeight = waypointType?.icon?.drawableResId
            ?.let { ContextCompat.getDrawable(context, it)?.intrinsicHeight }
            ?: 0
        iconHeight * iconScale / 2.0
    }
    val offsetY = markerHalfHeight + with(density) { Dimens.InfoWindowMarkerPadding.toPx() }
    ViewAnnotation(
        options = ViewAnnotationOptions.Builder()
            .annotatedFeature(AnnotatedFeature(info.location.toPoint()))
            .variableAnchors(
                listOf(
                    ViewAnnotationAnchorConfig.Builder()
                        .anchor(ViewAnnotationAnchor.BOTTOM)
                        .offsetY(offsetY)
                        .build(),
                ),
            )
            .allowOverlap(true)
            .build(),
    ) {
        DistanceInfoWindow(
            info = info,
            modifier = modifier.clickable { onEvent(MainUiEvents.DistanceInfoWindowDismissed) },
        )
    }
}
