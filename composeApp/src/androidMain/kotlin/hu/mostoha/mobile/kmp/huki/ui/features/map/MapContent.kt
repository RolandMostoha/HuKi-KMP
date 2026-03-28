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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.StringListValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.compose.style.sources.generated.rememberRasterSourceState
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.data.DefaultViewportTransitionOptions
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.MapUiEffects
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.isFollow
import hu.mostoha.mobile.kmp.huki.model.mapper.toCameraOptions
import hu.mostoha.mobile.kmp.huki.model.mapper.toDuration
import hu.mostoha.mobile.kmp.huki.model.mapper.toEdgeInset
import hu.mostoha.mobile.kmp.huki.model.mapper.toLineString
import hu.mostoha.mobile.kmp.huki.model.mapper.toMapStyle
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.util.MapConfiguration
import hu.mostoha.mobile.kmp.huki.util.MapConfiguration.MAP_FOLLOW_ANIM_DURATION
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.toComposeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun MapContent(
    mapUiState: MapUiState,
    mapUiEffects: Flow<MapUiEffects>,
    onEvent: (MainUiEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(MapConfiguration.HUNGARY_CAMERA_POSITION.toCameraOptions())
    }
    val mapState = rememberMapState {
        gesturesSettings = GesturesSettings { rotateEnabled = MapConfiguration.MAP_ROTATION_ENABLED }
    }

    LaunchedEffect(mapUiEffects) {
        mapUiEffects.collect { effect ->
            when (effect) {
                is MapUiEffects.UpdateCamera -> mapViewportState.moveCamera(density, effect)
                is MapUiEffects.ShowMyLocation -> mapViewportState.followLocation(effect)
            }
        }
    }

    MapboxMap(
        modifier = modifier
            .testTag(TestTags.MAP_MAPBOX)
            .fillMaxSize(),
        style = { MapStyle(mapUiState.baseLayer.toMapStyle()) },
        mapViewportState = mapViewportState,
        mapState = mapState,
        scaleBar = {
            ScaleBar(
                modifier = Modifier
                    .testTag(TestTags.MAIN_SCALE_BAR)
                    .padding(Dimens.Large),
                contentPadding = insetPadding,
                height = 2.dp,
                textSize = 10.sp,
            )
        },
        compass = {
            Compass(
                modifier = Modifier.padding(Dimens.Large),
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
            Attribution(contentPadding = insetPadding)
        },
        logo = {
            Logo(contentPadding = insetPadding)
        },
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val mapStrokeColor = SharedRes.colors.mapStrokeColor.toComposeColor(context)

        MapEffect(Unit) { mapView ->
            mapView.location.updateSettings {
                enabled = true
                locationPuck = LocationPuck2D(
                    topImage = ImageHolder.from(SharedRes.images.ic_my_location_top_image.drawableResId),
                    bearingImage = ImageHolder.from(SharedRes.images.ic_my_location_bearing.drawableResId),
                    shadowImage = ImageHolder.from(SharedRes.images.ic_my_location_shadow.drawableResId),
                )
                puckBearingEnabled = true
                puckBearing = PuckBearing.HEADING
                showAccuracyRing = true
                accuracyRingColor = SharedRes.colors.accuracyRing.getColor(context)
                pulsingEnabled = true
                pulsingColor = primaryColor.toArgb()
            }
            mapView.viewport.addStatusObserver { from, to, reason ->
                Logger.d { "Mapbox: Viewport status: from=$from, to=$to, reason=$reason" }
                if (from.isFollow() && to is ViewportStatus.Idle) {
                    onEvent(MainUiEvents.FollowingDisabled)
                }
            }
        }
        if (mapUiState.hikingLayerVisible) {
            RasterLayer(
                layerId = OverlayLayer.TURISTAUTAK.layerId,
                sourceState = rememberRasterSourceState {
                    tileSize = LongValue(OverlayLayer.TURISTAUTAK.tileSize)
                    tiles = StringListValue(OverlayLayer.TURISTAUTAK.tiles)
                    minZoom = LongValue(OverlayLayer.TURISTAUTAK.minZoom)
                    maxZoom = LongValue(OverlayLayer.TURISTAUTAK.maxZoom)
                },
            )
        }
        mapUiState.gpxDetails?.let { gpxDetails ->
            val geoJsonSource = rememberGeoJsonSourceState(key = gpxDetails.layerId) {
                lineMetrics = BooleanValue(true)
            }
            LaunchedEffect(key1 = gpxDetails.layerId) {
                geoJsonSource.data = GeoJSONData(gpxDetails.locations.toLineString())
            }
            LineLayer(
                sourceState = geoJsonSource,
                layerId = gpxDetails.layerId,
            ) {
                lineWidth = DoubleValue(SharedDimens.GPX_LINE_WIDTH)
                lineColor = ColorValue(primaryColor)
                lineBorderColor = ColorValue(mapStrokeColor)
                lineBorderWidth = DoubleValue(SharedDimens.GPX_STROKE_WIDTH)
                lineCap = LineCapValue.ROUND
                lineJoin = LineJoinValue.ROUND
            }

            gpxDetails.waypoints.forEach { waypoint ->
                val rememberIconImage = rememberIconImage(waypoint.type.icon.drawableResId)
                PointAnnotation(waypoint.location.toPoint()) {
                    iconImage = rememberIconImage
                    iconSize = if (waypoint.type == WaypointType.INTERMEDIATE) {
                        SharedDimens.GPX_WAYPOINT_MARKER_SCALE
                    } else {
                        SharedDimens.GPX_EDGE_LOCATION_MARKER_SCALE
                    }
                }
            }
        }
    }
}

private fun MapViewportState.followLocation(effect: MapUiEffects.ShowMyLocation) {
    val transitionOptions = DefaultViewportTransitionOptions.Builder()
        .maxDurationMs(effect.animated.toDuration(MAP_FOLLOW_ANIM_DURATION))
        .build()
    when (effect.myLocationStatus) {
        MyLocationStatus.Following -> {
            this.transitionToFollowPuckState(
                followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                    .zoom(MapConfiguration.FOLLOW_LOCATION_ZOOM_LEVEL)
                    .pitch(0.0)
                    .bearing(FollowPuckViewportStateBearing.Constant(0.0))
                    .build(),
                defaultTransitionOptions = transitionOptions,
            )
        }
        MyLocationStatus.FollowingLiveCompass -> {
            this.transitionToFollowPuckState(
                followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                    .zoom(MapConfiguration.FOLLOW_LOCATION_ZOOM_LEVEL)
                    .pitch(MapConfiguration.FOLLOW_LOCATION_LIVE_COMPASS_PITCH)
                    .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                    .build(),
                defaultTransitionOptions = transitionOptions,
            )
        }
        MyLocationStatus.Default, MyLocationStatus.NotAvailable -> Unit
    }
}

private fun MapViewportState.moveCamera(density: Density, effect: MapUiEffects.UpdateCamera) {
    val transitionOptions = DefaultViewportTransitionOptions.Builder()
        .maxDurationMs(MapConfiguration.MAP_CAMERA_ANIM_DURATION.inWholeMilliseconds)
        .build()
    this.transitionToOverviewState(
        overviewViewportStateOptions = OverviewViewportStateOptions.Builder()
            .geometry(effect.bounds.toLineString())
            .apply {
                effect.bearing?.let { bearing(it) }
                effect.pitch?.let { pitch(it) }
                effect.contentPadding?.let { padding(it.toEdgeInset(density)) }
            }
            .build(),
        defaultTransitionOptions = transitionOptions,
    )
}

@Preview
@Composable
private fun MapContentPreview() {
    MapContent(
        mapUiState = MapUiState(),
        mapUiEffects = emptyFlow(),
        onEvent = {},
    )
}
