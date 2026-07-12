@preconcurrency import MapboxMaps
import Shared
import SwiftUI

struct MapView: View {
    let uiState: MainUiState
    let onFollowingDisabled: () -> Void
    let onMyLocationReceived: () -> Void
    let onCompassClicked: () -> Void
    let onWaypointClicked: (GpxWaypoint) -> Void
    let onDistanceInfoWindowDismissed: () -> Void
    let mapUiEffects: SkieSwiftFlow<MapUiEffects>

    private let viewportObserver = ViewportObserver()
    private let strings = Strings()

    private var isCompassVisible: Bool {
        if case .followingLiveCompass = onEnum(of: uiState.myLocationState.myLocationStatus) {
            return true
        }
        return false
    }

    @State private var viewport = Viewport.camera(
        center: MapConstants.shared.HUNGARY_CAMERA_POSITION.location.coordinate,
        zoom: MapConstants.shared.HUNGARY_CAMERA_POSITION.zoom,
        bearing: MapConstants.shared.HUNGARY_CAMERA_POSITION.bearing,
        pitch: MapConstants.shared.HUNGARY_CAMERA_POSITION.pitch
    )

    var body: some View {
        MapReader { proxy in
            ZStack(alignment: .topTrailing) {
                Map(viewport: $viewport) {
                    if uiState.myLocationState.permissionState == PermissionState.granted {
                        Puck2D(bearing: .heading)
                            .showsAccuracyRing(true)
                            .accuracyRingColor(SharedRes.colors().accuracyRing.getUIColor())
                            .pulsing(.init(color: SharedRes.colors().primaryLight.getUIColor()))
                            .topImage(SharedRes.images().ic_my_location_top_image.toUIImage())
                            .bearingImage(SharedRes.images().ic_my_location_bearing.toUIImage())
                            .scale(1.2)
                    }
                    if uiState.mapUiState.hikingLayerVisible {
                        RasterSource(id: OverlayLayer.turistautak.layerId)
                            .tiles(OverlayLayer.turistautak.tiles)
                            .tileSize(Double(OverlayLayer.turistautak.tileSize))
                            .minzoom(Double(OverlayLayer.turistautak.minZoom))
                            .maxzoom(Double(OverlayLayer.turistautak.maxZoom))
                        RasterLayer(id: OverlayLayer.turistautak.layerId, source: OverlayLayer.turistautak.layerId)
                    }
                    if uiState.mapUiState.gpxLayerVisible {
                        if let gpxDetails = uiState.mapUiState.gpxDetails {
                            if uiState.mapUiState.gpxRouteVisible {
                                let feature = Feature(geometry: .lineString(gpxDetails.locations.lineString))

                                GeoJSONSource(id: gpxDetails.layerId)
                                    .data(.feature(feature))

                                LineLayer(id: gpxDetails.layerId, source: gpxDetails.layerId)
                                    .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH)
                                    .lineColor(SharedRes.colors().primary.getUIColor())
                                    .lineBorderWidth(SharedDimens.shared.GPX_STROKE_WIDTH)
                                    .lineBorderColor(SharedRes.colors().mapStroke.getUIColor())
                            }

                            PointAnnotationGroup(gpxDetails.waypoints, id: \.location.id) { waypoint in
                                PointAnnotation(coordinate: waypoint.location.coordinate)
                                    .image(waypoint.type.icon.annotationImage)
                                    .iconSize(
                                        waypoint.type == .intermediate
                                            ? SharedDimens.shared.GPX_WAYPOINT_MARKER_SCALE
                                            : SharedDimens.shared.GPX_EDGE_LOCATION_MARKER_SCALE
                                    )
                                    .onTapGesture {
                                        onWaypointClicked(waypoint)
                                    }
                            }
                            ForEvery(uiState.mapUiState.distanceInfoWindows, id: \.location.id) { info in
                                let waypointType = gpxDetails.waypoints
                                    .first { $0.location.id == info.location.id }?.type
                                let iconScale = CGFloat(
                                    waypointType == .intermediate
                                        ? SharedDimens.shared.GPX_WAYPOINT_MARKER_SCALE
                                        : SharedDimens.shared.GPX_EDGE_LOCATION_MARKER_SCALE
                                )
                                let markerHeight = (waypointType?.icon.toUIImage()?.size.height ?? 0) * iconScale
                                let offsetY = markerHeight / 2 + Dimens.infoWindowMarkerPadding
                                MapViewAnnotation(coordinate: info.location.coordinate) {
                                    DistanceInfoWindowView(info: info)
                                        .onTapGesture {
                                            onDistanceInfoWindowDismissed()
                                        }
                                }
                                .variableAnchors([
                                    ViewAnnotationAnchorConfig(anchor: .bottom, offsetY: offsetY)
                                ])
                                .allowOverlap(true)
                            }
                        }
                    }
                }
                .mapStyle(uiState.mapUiState.baseLayer.mapStyle)
                .gestureOptions(GestureOptions(
                    rotateEnabled: MapConstants.shared.MAP_ROTATION_ENABLED
                ))
                .onMapTapGesture { _ in
                    if !uiState.mapUiState.distanceInfoWindows.isEmpty {
                        onDistanceInfoWindowDismissed()
                    }
                }
                .ornamentOptions(OrnamentOptions(
                    scaleBar: ScaleBarViewOptions(
                        position: .topLeft,
                        margins: .init(x: 16.0, y: 16.0),
                        visibility: .adaptive
                    ),
                    // The built-in compass is hidden, we are using a custom one
                    compass: CompassViewOptions(visibility: .hidden),
                    logo: LogoViewOptions(
                        position: .topRight,
                        margins: .init(x: 40.0, y: 22.0)
                    ),
                    attributionButton: AttributionButtonOptions(
                        position: .topRight,
                        margins: .init(x: 0.0, y: 0.0)
                    )
                ))
                .onAppear {
                    viewportObserver.onFollowingDisabled = {
                        onFollowingDisabled()
                    }
                    proxy.viewport?.addStatusObserver(viewportObserver)
                }
                .onDisappear {
                    proxy.viewport?.removeStatusObserver(viewportObserver)
                }
                .accessibilityIdentifier(TestTags.shared.MAP_MAPBOX)
                .task {
                    for await effect in mapUiEffects {
                        handleMapEffects(effect)
                    }
                }
                .task(id: uiState.myLocationState.permissionState == PermissionState.granted) {
                    // Waits for the first GPS fix, then stops. Auto-cancelled on disappear.
                    guard uiState.myLocationState.permissionState == PermissionState.granted,
                          let location = proxy.location else { return }
                    let locationChanges = AsyncStream<Void> { continuation in
                        let cancelable = location.onLocationChange.observe { _ in
                            continuation.yield(())
                        }
                        continuation.onTermination = { _ in
                            cancelable.cancel()
                        }
                    }
                    for await _ in locationChanges {
                        onMyLocationReceived()
                        break
                    }
                }
                .ignoresSafeArea()

                if isCompassVisible {
                    CompassOrnamentView(
                        proxy: proxy,
                        accessibilityLabel: strings.get(id: SharedRes.strings().my_location_a11y_compass),
                        onTap: onCompassClicked
                    )
                    .padding(.top, CGFloat(SharedDimens.shared.MAP_COMPASS_TOP_PADDING))
                    .padding(.trailing, 16)
                    .transition(.opacity)
                }
            }
            .animation(.smooth(duration: 0.3), value: isCompassVisible)
        }
    }

    private func handleMapEffects(_ effect: MapUiEffects) {
        switch onEnum(of: effect) {
        case .updateCamera(let effect):
            updateCamera(effect)
        case .showMyLocation(let effect):
            showMyLocation(effect)
        }
    }

    private func updateCamera(_ effect: MapUiEffectsUpdateCamera) {
        withViewportAnimation(.default(maxDuration: AnimationConstants.shared.MAP_CAMERA_ANIM_DURATION_S)) {
            switch onEnum(of: effect.target) {
            case .center(let target):
                viewport = .camera(
                    center: target.location.coordinate,
                    zoom: target.zoom?.cgFloat,
                    bearing: effect.bearing?.cgFloat ?? 0,
                    pitch: effect.pitch?.cgFloat ?? 0
                )
            case .bounds(let target):
                viewport = .overview(
                    geometry: target.locations.lineString,
                    bearing: effect.bearing?.cgFloat ?? 0,
                    pitch: effect.pitch?.cgFloat ?? 0,
                    geometryPadding: effect.contentPadding?.edgeInsets ?? .init(),
                    maxZoom: target.maxZoom?.doubleValue
                )
            }
        }
    }

    private func showMyLocation(_ effect: MapUiEffectsShowMyLocation) {
        let duration = effect.animated ? AnimationConstants.shared.MAP_FOLLOW_ANIM_DURATION_S : 0

        withViewportAnimation(.default(maxDuration: duration)) {
            switch onEnum(of: effect.myLocationStatus) {
            case .following:
                viewport = .followPuck(
                    zoom: MapConstants.shared.FOLLOW_LOCATION_ZOOM_LEVEL,
                    bearing: .constant(0.0),
                    pitch: 0.0
                )
            case .followingLiveCompass:
                viewport = .followPuck(
                    zoom: MapConstants.shared.FOLLOW_LOCATION_ZOOM_LEVEL,
                    bearing: .heading,
                    pitch: MapConstants.shared.FOLLOW_LOCATION_LIVE_COMPASS_PITCH
                )
            default:
                break
            }
        }
    }
}
