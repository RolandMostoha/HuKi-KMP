package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.StringListValue
import com.mapbox.maps.extension.compose.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.compose.style.sources.generated.rememberRasterSourceState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.Layer
import hu.mostoha.mobile.kmp.huki.model.mapper.toCameraOptions
import hu.mostoha.mobile.kmp.huki.util.MapConfiguration
import hu.mostoha.mobile.kmp.huki.util.MapConfiguration.MAP_ROTATION_ENABLED
import hu.mostoha.mobile.kmp.huki.util.TestTags
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
    val mapState = rememberMapState {
        gesturesSettings = GesturesSettings { rotateEnabled = MAP_ROTATION_ENABLED }
    }

    LaunchedEffect(uiEffect) {
        uiEffect.collect { effect ->
            when (effect) {
                is MainUiEffects.MoveCamera -> {
                    mapViewportState.flyTo(
                        cameraOptions = effect.cameraPosition.toCameraOptions(),
                        animationOptions = MapAnimationOptions.mapAnimationOptions {
                            duration(MapConfiguration.MAP_CAMERA_ANIMATION_DURATION.inWholeMilliseconds)
                        },
                    )
                }
            }
        }
    }

    MapboxMap(
        modifier = modifier
            .testTag(TestTags.MAP_MAPBOX)
            .fillMaxSize(),
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
    ) {
        RasterLayer(
            layerId = Layer.TURISTAUTAK.layerId,
            sourceState = rememberRasterSourceState {
                tileSize = LongValue(Layer.TURISTAUTAK.tileSize)
                tiles = StringListValue(Layer.TURISTAUTAK.tiles)
                minZoom = LongValue(Layer.TURISTAUTAK.minZoom)
                maxZoom = LongValue(Layer.TURISTAUTAK.maxZoom)
            },
        )
    }
}

@Preview
@Composable
private fun MapPreview() {
    MapContent(
        mapUiState = MapUiState(),
        uiEffect = emptyFlow(),
    )
}
