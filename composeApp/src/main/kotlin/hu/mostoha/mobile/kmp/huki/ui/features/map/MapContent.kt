package hu.mostoha.mobile.kmp.huki.ui.features.map

import android.content.Context
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import co.touchlab.kermit.Logger
import com.mapbox.geojson.Point
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.LandscapesUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.OktUiEvents
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.mapper.MarkerIcons
import hu.mostoha.mobile.kmp.huki.model.mapper.WaypointMarkerOrder
import hu.mostoha.mobile.kmp.huki.model.mapper.followLocation
import hu.mostoha.mobile.kmp.huki.model.mapper.isFollow
import hu.mostoha.mobile.kmp.huki.model.mapper.isIdle
import hu.mostoha.mobile.kmp.huki.model.mapper.isOverview
import hu.mostoha.mobile.kmp.huki.model.mapper.moveCamera
import hu.mostoha.mobile.kmp.huki.model.mapper.resetBearing
import hu.mostoha.mobile.kmp.huki.model.mapper.toCameraOptions
import hu.mostoha.mobile.kmp.huki.model.mapper.toCameraPosition
import hu.mostoha.mobile.kmp.huki.model.mapper.toLineString
import hu.mostoha.mobile.kmp.huki.model.mapper.toLocation
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.model.mapper.toSymbolScaleBehavior
import hu.mostoha.mobile.kmp.huki.model.mapper.zoom
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.LocalIsDarkTheme
import hu.mostoha.mobile.kmp.huki.theme.LocalThemedContext
import hu.mostoha.mobile.kmp.huki.theme.MapLighting
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens.MAP_COMPASS_TOP_PADDING
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens.MY_LOCATION_PUCK_SCALE
import hu.mostoha.mobile.kmp.huki.util.FeatureFlags
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull

private const val SCALE_BAR_RATIO_PORTRAIT = 0.5f
private const val SCALE_BAR_RATIO_LANDSCAPE = 0.25f
private const val DIRECTION_LAYER_SUFFIX = "_direction"

@OptIn(MapboxDelicateApi::class, MapboxExperimental::class)
@Composable
fun MapContent(
    mapUiState: MapUiState,
    mapUiEffects: Flow<MapUiEffects>,
    onEvent: (MainUiEvents) -> Unit,
    modifier: Modifier = Modifier,
    routePlannerBottomInset: Dp? = null,
    listSheetHeight: Dp? = null,
) {
    val density = LocalDensity.current
    val isLandscape by rememberUpdatedState(
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
    )
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()
    val isDarkMode = LocalIsDarkTheme.current
    val themedContext = LocalThemedContext.current ?: LocalContext.current
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(MapConstants.HUNGARY_CAMERA_POSITION.toCameraOptions())
    }
    val mapState = rememberMapState()
    val mapLoaded = remember { CompletableDeferred<Unit>() }
    var isCameraPanelVisible by remember { mutableStateOf(true) }
    var lastRoutePlannerCamera by remember { mutableStateOf<MapUiEffects.UpdateCamera?>(null) }
    val currentRoutePlannerInset by rememberUpdatedState(routePlannerBottomInset)
    val currentListSheetHeight by rememberUpdatedState(listSheetHeight)

    LaunchedEffect(mapUiState.rotationEnabled) {
        mapState.gesturesSettings = GesturesSettings { rotateEnabled = mapUiState.rotationEnabled }
    }

    LaunchedEffect(mapViewportState) {
        snapshotFlow { mapViewportState.cameraState }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { onEvent(MainUiEvents.MapCameraChanged(it.toCameraPosition())) }
    }

    LaunchedEffect(mapUiEffects) {
        // suspend until map is ready
        mapLoaded.await()
        mapUiEffects.collect { effect ->
            if (effect is MapUiEffects.UpdateCamera && effect.contentPadding == ContentPadding.MAP_ROUTE_PLANNER) {
                lastRoutePlannerCamera = effect
            }
            mapViewportState.handleMapEffect(
                effect,
                density,
                isLandscape,
                currentRoutePlannerInset,
                currentListSheetHeight,
            )
        }
    }

    LaunchedEffect(mapUiState.routePlan) {
        if (mapUiState.routePlan == null) {
            lastRoutePlannerCamera = null
        }
    }

    LaunchedEffect(routePlannerBottomInset) {
        val effect = lastRoutePlannerCamera ?: return@LaunchedEffect
        if (routePlannerBottomInset == null) return@LaunchedEffect
        mapViewportState.moveCamera(density, effect, isLandscape, routePlannerBottomInset)
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier
                .testTag(TestTags.MAP_MAPBOX)
                .fillMaxSize(),
            style = {
                MapBaseStyle(
                    baseLayer = mapUiState.baseLayer,
                    isDarkMode = isDarkMode,
                    isHikingLayerVisible = mapUiState.hikingLayerVisible,
                )
            },
            mapViewportState = mapViewportState,
            mapState = mapState,
            onMapClickListener = OnMapClickListener { dismissInfoWindows(mapUiState, onEvent) },
            onMapLongClickListener = OnMapLongClickListener { point ->
                onEvent(MainUiEvents.MapLongClicked(point.toLocation()))
                true
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
            val primaryColor = mokoColor(SharedRes.colors.primary)
            val primaryLightColor = mokoColor(SharedRes.colors.primaryLight)
            val mapStrokeColor = mokoColor(SharedRes.colors.mapStroke)
            val accuracyRingColor = mokoColor(SharedRes.colors.accuracyRing)

            MapEffect(Unit) { mapView ->
                mapView.mapboxMap.subscribeMapLoaded { mapLoaded.complete(Unit) }
                mapView.location.updateSettings {
                    enabled = true
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.HEADING
                    showAccuracyRing = true
                    pulsingEnabled = true
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
            MapEffect(mapUiState.labelScale) { mapView ->
                mapView.mapboxMap.symbolScaleBehavior = mapUiState.labelScale.toSymbolScaleBehavior()
            }
            MapEffect(isDarkMode) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = LocationPuck2D(
                        topImage = themedContext.toImageHolder(SharedRes.images.ic_my_location_top_image),
                        bearingImage = themedContext.toImageHolder(SharedRes.images.ic_my_location_bearing),
                        shadowImage = themedContext.toImageHolder(SharedRes.images.ic_my_location_shadow),
                        scaleExpression = literal(MY_LOCATION_PUCK_SCALE).toJson(),
                    )
                    this.accuracyRingColor = accuracyRingColor.toArgb()
                    pulsingColor = primaryLightColor.toArgb()
                }
            }
            BaseLabelsVisibilityEffect(isHidden = mapUiState.isBaseLabelsHidden)
            mapUiState.landscapes?.let { landscapes ->
                LandscapesLayer(landscapes = landscapes, onEvent = onEvent)
            }
            mapUiState.okt?.let { okt ->
                OktLayer(okt = okt, onEvent = onEvent)
            }
            if (mapUiState.gpxLayerVisible) {
                GpxLayer(
                    mapUiState = mapUiState,
                    onEvent = onEvent,
                    routeColor = primaryColor,
                    routeStrokeColor = mapStrokeColor,
                    isDarkMode = isDarkMode,
                )
            }
            if (mapUiState.routePlan != null || mapUiState.routePlanMarkers.isNotEmpty()) {
                RoutePlanLayer(
                    routePlan = mapUiState.routePlan,
                    markers = mapUiState.routePlanMarkers,
                    routeColor = primaryColor,
                    routeStrokeColor = mapStrokeColor,
                )
            }
            PlaceMarkersLayer(mapUiState = mapUiState, isDarkMode = isDarkMode, onEvent = onEvent)
        }
        MapCameraDebugPanel(
            enabled = FeatureFlags.DEBUG_SHOW_CAMERA_PANEL,
            visible = isCameraPanelVisible,
            cameraState = if (FeatureFlags.DEBUG_SHOW_CAMERA_PANEL) mapViewportState.cameraState else null,
            onMoveCamera = { position -> mapViewportState.setCameraOptions(position.toCameraOptions()) },
            onOpen = { isCameraPanelVisible = true },
            onClose = { isCameraPanelVisible = false },
        )
    }
}

private fun dismissInfoWindows(mapUiState: MapUiState, onEvent: (MainUiEvents) -> Unit): Boolean =
    when {
        mapUiState.distanceInfoWindows.isNotEmpty() -> {
            onEvent(MainUiEvents.DistanceInfoWindowDismissed)
            true
        }
        mapUiState.okt?.infoWindow != null -> {
            onEvent(OktUiEvents.OktInfoWindowDismissed)
            true
        }
        mapUiState.landscapes?.selectedDestinationId != null -> {
            onEvent(LandscapesUiEvents.LandscapesInfoWindowDismissed)
            true
        }
        else -> false
    }

private fun MapViewportState.handleMapEffect(
    effect: MapUiEffects,
    density: Density,
    isLandscape: Boolean,
    routePlannerBottomInset: Dp?,
    listSheetHeight: Dp?,
) {
    when (effect) {
        is MapUiEffects.UpdateCamera -> moveCamera(
            density,
            effect,
            isLandscape,
            routePlannerBottomInset,
            listSheetHeight,
        )
        is MapUiEffects.ShowMyLocation -> followLocation(effect.myLocationStatus, effect.animated)
        is MapUiEffects.Zoom -> zoom(effect.zoomIn)
        is MapUiEffects.ResetBearing -> resetBearing()
    }
}

@OptIn(MapboxDelicateApi::class)
@Composable
@MapboxMapComposable
private fun GpxLayer(
    mapUiState: MapUiState,
    onEvent: (MainUiEvents) -> Unit,
    routeColor: Color,
    routeStrokeColor: Color,
    isDarkMode: Boolean,
) {
    val gpxDetails = mapUiState.gpxDetails ?: return
    if (mapUiState.gpxRouteVisible) {
        val geoJsonSource = rememberGeoJsonSourceState(key = gpxDetails.layerId) {
            lineMetrics = BooleanValue(true)
        }
        LaunchedEffect(key1 = gpxDetails.layerId) {
            geoJsonSource.data = GeoJSONData(gpxDetails.locations.toLineString())
        }
        RouteLineLayer(
            sourceState = geoJsonSource,
            layerId = gpxDetails.layerId,
            routeColor = routeColor,
            routeStrokeColor = routeStrokeColor,
        )
        RouteDirectionLayer(
            sourceState = geoJsonSource,
            layerId = "${gpxDetails.layerId}$DIRECTION_LAYER_SUFFIX",
            arrowColor = routeStrokeColor,
        )
    }
    WaypointMarkerOrder.sort(gpxDetails.waypoints).forEach { waypoint ->
        val jelSymbol = MarkerIcons.waypointJelSymbol(waypoint.type, gpxDetails.jelSymbol)
        val waypointIconImage = if (jelSymbol != null) {
            rememberJelMarkerIconImage(MarkerIcons.jelMarkerFrame(isDarkMode, mapUiState.baseLayer), jelSymbol)
        } else {
            rememberIconImage(MarkerIcons.waypointSymbol(waypoint.type, isDarkMode, mapUiState.baseLayer).drawableResId)
        }
        PointAnnotation(waypoint.location.toPoint()) {
            interactionsState.onClicked {
                onEvent(MainUiEvents.GpxWaypointClicked(waypoint))
                true
            }
            iconImage = waypointIconImage
            iconSize = waypoint.type.markerScale
            iconEmissiveStrength = MapLighting.OVERLAY_EMISSIVE_STRENGTH
        }
    }
    mapUiState.distanceInfoWindows.forEach { distanceInfoWindowData ->
        key(distanceInfoWindowData.id) {
            DistanceInfoWindowAnnotation(
                info = distanceInfoWindowData,
                gpxDetails = gpxDetails,
                onEvent = onEvent,
            )
        }
    }
}

/**
 * Mapbox decodes drawable ids with the MapView's context, which ignores the in-app dark theme.
 */
private fun Context.toImageHolder(image: ImageResource): ImageHolder =
    ImageHolder.from(requireNotNull(ContextCompat.getDrawable(this, image.drawableResId)).toBitmap())

@Preview
@Composable
private fun MapContentPreview() {
    MapContent(
        mapUiState = MapUiState(),
        mapUiEffects = emptyFlow(),
        onEvent = {},
    )
}
