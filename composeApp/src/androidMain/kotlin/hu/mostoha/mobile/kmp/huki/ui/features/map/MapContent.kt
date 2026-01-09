package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.mapper.toCameraOptions
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun MapContent(
    mapUiState: MapUiState,
    uiEffect: Flow<MainUiEffects>,
    modifier: Modifier = Modifier,
) {
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(mapUiState.cameraPosition.toCameraOptions())
    }
    val mapState = rememberMapState()

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is MainUiEffects.MoveCamera -> {
                    mapViewportState.flyTo(
                        cameraOptions = effect.cameraPosition.toCameraOptions(),
                        animationOptions = MapAnimationOptions.mapAnimationOptions {
                            duration(MapConstants.DEFAULT_MAP_ANIMATION_DURATION.inWholeMilliseconds)
                        },
                    )
                }
            }
        }
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        style = { MapStyle(Style.OUTDOORS) },
        mapViewportState = mapViewportState,
        mapState = mapState,
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
        uiEffect = emptyFlow(),
    )
}
