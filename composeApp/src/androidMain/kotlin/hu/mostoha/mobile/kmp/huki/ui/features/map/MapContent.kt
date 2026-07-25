package hu.mostoha.mobile.kmp.huki.ui.features.map

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.mapbox.geojson.Point
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
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
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.followLocation
import hu.mostoha.mobile.kmp.huki.model.mapper.isFollow
import hu.mostoha.mobile.kmp.huki.model.mapper.isIdle
import hu.mostoha.mobile.kmp.huki.model.mapper.isOverview
import hu.mostoha.mobile.kmp.huki.model.mapper.moveCamera
import hu.mostoha.mobile.kmp.huki.model.mapper.resetBearing
import hu.mostoha.mobile.kmp.huki.model.mapper.toCameraOptions
import hu.mostoha.mobile.kmp.huki.model.mapper.toLineString
import hu.mostoha.mobile.kmp.huki.model.mapper.toMapStyle
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.model.mapper.zoom
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens.MAP_COMPASS_TOP_PADDING
import hu.mostoha.mobile.kmp.huki.util.FeatureFlags
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.toComposeColor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val SCALE_BAR_RATIO_PORTRAIT = 0.5f
private const val SCALE_BAR_RATIO_LANDSCAPE = 0.25f

@OptIn(MapboxDelicateApi::class, MapboxExperimental::class)
@Composable
fun MapContent(
    mapUiState: MapUiState,
    mapUiEffects: Flow<MapUiEffects>,
    onEvent: (MainUiEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isLandscape by rememberUpdatedState(
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
    )
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(MapConstants.HUNGARY_CAMERA_POSITION.toCameraOptions())
    }
    val mapState = rememberMapState {
        gesturesSettings = GesturesSettings { rotateEnabled = MapConstants.MAP_ROTATION_ENABLED }
    }
    val mapLoaded = remember { CompletableDeferred<Unit>() }
    var isCameraPanelVisible by remember { mutableStateOf(true) }

    LaunchedEffect(mapUiEffects) {
        // suspend until map is ready
        mapLoaded.await()
        mapUiEffects.collect { effect ->
            when (effect) {
                is MapUiEffects.UpdateCamera -> mapViewportState.moveCamera(density, effect, isLandscape)
                is MapUiEffects.ShowMyLocation -> {
                    mapViewportState.followLocation(effect.myLocationStatus, effect.animated)
                }
                is MapUiEffects.Zoom -> mapViewportState.zoom(effect.zoomIn)
                is MapUiEffects.ResetBearing -> mapViewportState.resetBearing()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier
                .testTag(TestTags.MAP_MAPBOX)
                .fillMaxSize(),
            style = { MapStyle(mapUiState.baseLayer.toMapStyle()) },
            mapViewportState = mapViewportState,
            mapState = mapState,
            onMapClickListener = OnMapClickListener {
                if (mapUiState.distanceInfoWindows.isNotEmpty()) {
                    onEvent(MainUiEvents.DistanceInfoWindowDismissed)
                    true
                } else {
                    false
                }
            },
            onMapLongClickListener = OnMapLongClickListener {
                if (FeatureFlags.DEBUG_SHOW_CAMERA_PANEL) isCameraPanelVisible = true
                false
            },
            scaleBar = {
                ScaleBar(
                    modifier = Modifier
                        .testTag(TestTags.MAIN_SCALE_BAR)
                        .padding(horizontal = Dimens.Large),
                    contentPadding = insetPadding,
                    height = 2.dp,
                    textSize = 10.sp,
                    ratio = if (isLandscape) SCALE_BAR_RATIO_LANDSCAPE else SCALE_BAR_RATIO_PORTRAIT,
                )
            },
            compass = {
                Compass(
                    modifier = Modifier.padding(
                        top = 35.dp,
                        end = Dimens.ExtraLarge,
                    ),
                    contentPadding = insetPadding,
                    resetToNorthUponClick = false,
                ) {
                    Image(
                        modifier = Modifier
                            .size(MAP_COMPASS_TOP_PADDING.dp)
                            .clip(CircleShape)
                            .clickable { onEvent(MainUiEvents.CompassClicked) },
                        painter = painterResource(id = SharedRes.images.ic_my_location_compass.drawableResId),
                        contentDescription = mokoString(SharedRes.strings.my_location_a11y_compass),
                    )
                }
            },
            attribution = {
                Attribution(
                    modifier = Modifier.padding(
                        top = Dimens.Medium,
                        end = Dimens.Small,
                    ),
                    alignment = Alignment.TopEnd,
                    contentPadding = insetPadding,
                )
            },
            logo = {
                Logo(
                    modifier = Modifier.padding(
                        top = Dimens.Medium,
                        end = Dimens.Huge,
                    ),
                    alignment = Alignment.TopEnd,
                    contentPadding = insetPadding,
                )
            },
        ) {
            val primaryOnMapColor = SharedRes.colors.primaryOnMap.toComposeColor(context)
            val primaryLightOnMapColor = SharedRes.colors.primaryLightOnMap.toComposeColor(context)
            val mapStrokeColor = SharedRes.colors.mapStrokeOnMap.toComposeColor(context)

            MapEffect(Unit) { mapView ->
                mapView.mapboxMap.subscribeMapLoaded { mapLoaded.complete(Unit) }
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
                    accuracyRingColor = SharedRes.colors.accuracyRingOnMap.getColor(context)
                    pulsingEnabled = true
                    pulsingColor = primaryLightOnMapColor.toArgb()
                }
                val positionListener = object : OnIndicatorPositionChangedListener {
                    override fun onIndicatorPositionChanged(point: Point) {
                        onEvent(MainUiEvents.MyLocationReceived)
                        mapView.location.removeOnIndicatorPositionChangedListener(this)
                    }
                }
                mapView.location.addOnIndicatorPositionChangedListener(positionListener)
                mapView.viewport.addStatusObserver { from, to, reason ->
                    Logger.d { "Mapbox: Viewport status: from=$from, to=$to, reason=$reason" }
                    if (from.isFollow() && (to.isIdle() || to.isOverview())) {
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
            if (mapUiState.gpxLayerVisible) {
                mapUiState.gpxDetails?.let { gpxDetails ->
                    if (mapUiState.gpxRouteVisible) {
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
                            lineColor = ColorValue(primaryOnMapColor)
                            lineBorderColor = ColorValue(mapStrokeColor)
                            lineBorderWidth = DoubleValue(SharedDimens.GPX_STROKE_WIDTH)
                            lineCap = LineCapValue.ROUND
                            lineJoin = LineJoinValue.ROUND
                        }
                    }

                    gpxDetails.waypoints.forEach { waypoint ->
                        val rememberIconImage = rememberIconImage(waypoint.type.icon.drawableResId)
                        PointAnnotation(waypoint.location.toPoint()) {
                            interactionsState.onClicked {
                                onEvent(MainUiEvents.GpxWaypointClicked(waypoint))
                                true
                            }
                            iconImage = rememberIconImage
                            iconSize = if (waypoint.type == WaypointType.INTERMEDIATE) {
                                SharedDimens.GPX_WAYPOINT_MARKER_SCALE
                            } else {
                                SharedDimens.GPX_EDGE_LOCATION_MARKER_SCALE
                            }
                        }
                    }

                    mapUiState.distanceInfoWindows.forEach { distanceInfoWindowData ->
                        key(distanceInfoWindowData.location) {
                            DistanceInfoWindowAnnotation(
                                info = distanceInfoWindowData,
                                gpxDetails = gpxDetails,
                                onEvent = onEvent,
                            )
                        }
                    }
                }
            }
        }
        MapCameraDebugPanel(
            enabled = FeatureFlags.DEBUG_SHOW_CAMERA_PANEL,
            visible = isCameraPanelVisible,
            cameraState = if (FeatureFlags.DEBUG_SHOW_CAMERA_PANEL) mapViewportState.cameraState else null,
            onMoveCamera = { position -> mapViewportState.setCameraOptions(position.toCameraOptions()) },
            onClose = { isCameraPanelVisible = false },
        )
    }
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
