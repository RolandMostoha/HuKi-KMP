package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.StringListValue
import com.mapbox.maps.extension.compose.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.compose.style.sources.generated.rememberRasterSourceState
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.data.DefaultViewportTransitionOptions
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.model.domain.Layer
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.mapper.isFollow
import hu.mostoha.mobile.kmp.huki.model.mapper.toCameraOptions
import hu.mostoha.mobile.kmp.huki.model.mapper.toDuration
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.ui.components.mokoString
import hu.mostoha.mobile.kmp.huki.util.MapConfiguration
import hu.mostoha.mobile.kmp.huki.util.MapConfiguration.MAP_FOLLOW_ANIM_DURATION
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.utils.navigateToAppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun MapContent(
    onEvent: (MainUiEvents) -> Unit,
    uiEffect: Flow<MainUiEffects>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(MapConfiguration.HUNGARY_CAMERA_POSITION.toCameraOptions())
    }
    val mapState = rememberMapState {
        gesturesSettings = GesturesSettings { rotateEnabled = MapConfiguration.MAP_ROTATION_ENABLED }
    }

    LaunchedEffect(uiEffect) {
        uiEffect.collect { effect ->
            when (effect) {
                is MainUiEffects.UpdateCamera -> {
                    mapViewportState.flyTo(
                        cameraOptions = CameraOptions.Builder()
                            .apply {
                                effect.location?.let { center(it.toPoint()) }
                                effect.zoom?.let { zoom(it) }
                                effect.bearing?.let { bearing(it) }
                                effect.pitch?.let { pitch(it) }
                            }
                            .build(),
                        animationOptions = MapAnimationOptions.mapAnimationOptions {
                            duration(MapConfiguration.MAP_CAMERA_ANIM_DURATION.inWholeMilliseconds)
                        },
                    )
                }
                is MainUiEffects.ShowMyLocation -> {
                    when (effect.myLocationStatus) {
                        MyLocationStatus.Following -> {
                            mapViewportState.transitionToFollowPuckState(
                                followPuckViewportStateOptions = FollowPuckViewportStateOptions
                                    .Builder()
                                    .zoom(MapConfiguration.FOLLOW_LOCATION_ZOOM_LEVEL)
                                    .pitch(0.0)
                                    .bearing(FollowPuckViewportStateBearing.Constant(0.0))
                                    .build(),
                                defaultTransitionOptions = DefaultViewportTransitionOptions
                                    .Builder()
                                    .maxDurationMs(effect.animated.toDuration(MAP_FOLLOW_ANIM_DURATION))
                                    .build(),
                            )
                        }
                        MyLocationStatus.FollowingLiveCompass -> {
                            mapViewportState.transitionToFollowPuckState(
                                followPuckViewportStateOptions = FollowPuckViewportStateOptions
                                    .Builder()
                                    .zoom(MapConfiguration.FOLLOW_LOCATION_ZOOM_LEVEL)
                                    .pitch(MapConfiguration.FOLLOW_LOCATION_PITCH)
                                    .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                    .build(),
                                defaultTransitionOptions = DefaultViewportTransitionOptions
                                    .Builder()
                                    .maxDurationMs(effect.animated.toDuration(MAP_FOLLOW_ANIM_DURATION))
                                    .build(),
                            )
                        }
                        MyLocationStatus.Default, MyLocationStatus.NotAvailable -> Unit
                    }
                }
                is MainUiEffects.NavigateToAppSettings -> context.navigateToAppSettings()
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
                modifier = Modifier
                    .testTag(TestTags.MAIN_SCALE_BAR)
                    .padding(Dimens.Default),
                contentPadding = insetPadding,
                height = 3.dp,
                textSize = 10.sp,
            )
        },
        compass = {
            Compass(
                modifier = Modifier.padding(Dimens.Default),
                contentPadding = insetPadding,
            ) {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = SharedRes.images.ic_my_location_compass.drawableResId),
                    contentDescription = mokoString(SharedRes.strings.my_location_a11y_compass),
                )
            }
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
        val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

        MapEffect(Unit) { mapView ->
            mapView.location.updateSettings {
                enabled = true
                locationPuck = LocationPuck2D(
                    topImage = ImageHolder.from(SharedRes.images.ic_my_location_top_image.drawableResId),
                    bearingImage = ImageHolder.from(SharedRes.images.ic_my_location_bearing.drawableResId),
                    shadowImage = ImageHolder.from(com.mapbox.maps.R.drawable.mapbox_user_stroke_icon),
                )
                puckBearingEnabled = true
                puckBearing = PuckBearing.HEADING
                showAccuracyRing = true
                accuracyRingColor = SharedRes.colors.accuracyRing.getColor(context)
                pulsingEnabled = true
                pulsingColor = primaryColor
            }
            mapView.viewport.addStatusObserver { from, to, reason ->
                Logger.d { "Mapbox: Viewport status: from=$from, to=$to, reason=$reason" }
                if (from.isFollow() && to is ViewportStatus.Idle) {
                    onEvent(MainUiEvents.FollowingDisabled)
                }
            }
        }
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
        onEvent = {},
        uiEffect = emptyFlow(),
    )
}
