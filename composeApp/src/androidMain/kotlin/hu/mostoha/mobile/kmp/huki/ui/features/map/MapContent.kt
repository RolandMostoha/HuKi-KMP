package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.util.MapConstants

@Composable
fun MapContent(
    mapUiState: MapUiState,
    modifier: Modifier = Modifier,
) {
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(mapUiState.longitude, mapUiState.latitude))
            zoom(mapUiState.zoomLevel)
        }
    }
    LaunchedEffect(mapUiState.latitude, mapUiState.longitude, mapUiState.zoomLevel) {
        mapViewportState.flyTo(
            cameraOptions = cameraOptions {
                center(Point.fromLngLat(mapUiState.longitude, mapUiState.latitude))
                zoom(mapUiState.zoomLevel)
            },
            animationOptions = MapAnimationOptions.mapAnimationOptions {
                duration(MapConstants.DEFAULT_MAP_ANIMATION_DURATION.inWholeMilliseconds)
            },
        )
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        style = { MapStyle(Style.OUTDOORS) },
        mapViewportState = mapViewportState,
        scaleBar = {
            ScaleBar(
                contentPadding = insetPadding,
            )
        },
        compass = {
            Compass(
                contentPadding = insetPadding,
            )
        },
        attribution = {
            Attribution(
                contentPadding = insetPadding,
            )
        },
        logo = {
            Logo(
                contentPadding = insetPadding,
            )
        },
    )
}

@Preview
@Composable
private fun MapPreview() {
    MapContent(
        mapUiState = MapUiState(),
    )
}
