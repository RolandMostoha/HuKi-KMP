package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.ViewAnnotationAnchorConfig
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.mapper.MarkerIcons
import hu.mostoha.mobile.kmp.huki.model.mapper.toDisplayName
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoPainter
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@Composable
@MapboxMapComposable
fun PlaceMarkersLayer(mapUiState: MapUiState, isDarkMode: Boolean, onEvent: (MainUiEvents) -> Unit) {
    val shownPlace = mapUiState.placeDetails
    mapUiState.routeStops
        .filter { it.location != shownPlace?.location }
        .forEach { stop ->
            key(stop.location) {
                PlaceMarkerAnnotation(
                    location = stop.location,
                    isDarkMode = isDarkMode,
                    baseLayer = mapUiState.baseLayer,
                    accessibilityLabel = mokoString(SharedRes.strings.map_a11y_route_stop, stop.toDisplayName()),
                    onClick = { onEvent(MainUiEvents.RouteStopClicked(stop)) },
                )
            }
        }
    shownPlace?.let { placeDetails ->
        key(placeDetails.location) {
            PlaceMarkerAnnotation(
                location = placeDetails.location,
                isDarkMode = isDarkMode,
                baseLayer = mapUiState.baseLayer,
                animateIn = true,
            )
        }
    }
}

@Composable
private fun PlaceMarkerAnnotation(
    location: Location,
    isDarkMode: Boolean,
    baseLayer: BaseLayer,
    accessibilityLabel: String? = null,
    onClick: (() -> Unit)? = null,
    animateIn: Boolean = false,
) {
    val painter = mokoPainter(MarkerIcons.placeSymbol(isDarkMode, baseLayer))
    val markerSize =
        with(LocalDensity.current) { (painter.intrinsicSize * SharedDimens.PLACE_MARKER_SCALE.toFloat()).toDpSize() }
    // The annotation view clips its content, so the spring overshoot needs room above and beside the pin
    val overshootInset = if (animateIn) markerSize.width * PLACE_MARKER_OVERSHOOT_INSET else 0.dp
    ViewAnnotation(
        options = ViewAnnotationOptions.Builder()
            .annotatedFeature(AnnotatedFeature(location.toPoint()))
            .variableAnchors(listOf(ViewAnnotationAnchorConfig.Builder().anchor(ViewAnnotationAnchor.BOTTOM).build()))
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .ignoreCameraPadding(true)
            .priority(MapConstants.PLACE_MARKER_PRIORITY.toLong())
            .build(),
    ) {
        val scale = remember { Animatable(if (animateIn) 0f else 1f) }
        LaunchedEffect(Unit) {
            scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        }
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .padding(start = overshootInset, top = overshootInset, end = overshootInset)
                .size(markerSize)
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1f),
                )
                .then(
                    if (onClick != null && accessibilityLabel != null) {
                        Modifier
                            .clickable(onClick = onClick)
                            .semantics {
                                role = Role.Button
                                contentDescription = accessibilityLabel
                            }
                            .testTagAsResourceId(TestTags.MAP_ROUTE_STOP_MARKER)
                    } else {
                        Modifier.testTagAsResourceId(TestTags.MAP_PLACE_MARKER)
                    },
                ),
        )
    }
}

private const val PLACE_MARKER_OVERSHOOT_INSET = 0.2f
