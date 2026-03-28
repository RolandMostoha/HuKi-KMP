import MapboxMaps
import Shared
import SwiftUI

struct MapView: View {
    let uiState: MainUiState
    let onFollowingDisabled: () -> Void
    let mapUiEffects: SkieSwiftFlow<MapUiEffects>

    private let viewportObserver = ViewportObserver()

    @State private var viewport = Viewport.camera(
        center: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.location.coordinate,
        zoom: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.zoom,
        bearing: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.bearing,
        pitch: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.pitch
    )

    var body: some View {
        MapReader { proxy in
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
                if let gpxDetails = uiState.mapUiState.gpxDetails {
                    let feature = Feature(geometry: .lineString(gpxDetails.locations.lineString))

                    GeoJSONSource(id: gpxDetails.layerId)
                        .data(.feature(feature))

                    LineLayer(id: gpxDetails.layerId, source: gpxDetails.layerId)
                        .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH)
                        .lineColor(SharedRes.colors().primary.getUIColor())
                        .lineBorderWidth(SharedDimens.shared.GPX_STROKE_WIDTH)
                        .lineBorderColor(SharedRes.colors().mapStrokeColor.getUIColor())

                    PointAnnotationGroup(gpxDetails.waypoints, id: \.location.id) { waypoint in
                        PointAnnotation(coordinate: waypoint.location.coordinate)
                            .image(waypoint.type.icon.annotationImage)
                            .iconSize(
                                waypoint.type == .intermediate
                                    ? SharedDimens.shared.GPX_WAYPOINT_MARKER_SCALE
                                    : SharedDimens.shared.GPX_EDGE_LOCATION_MARKER_SCALE
                            )
                    }
                }
            }
            .mapStyle(uiState.mapUiState.baseLayer.mapStyle)
            .gestureOptions(GestureOptions(
                rotateEnabled: MapConfiguration.shared.MAP_ROTATION_ENABLED
            ))
            .ornamentOptions(OrnamentOptions(
                scaleBar: ScaleBarViewOptions(
                    position: .topLeft,
                    margins: .init(x: 16.0, y: 16.0),
                    visibility: .adaptive
                ),
                compass: CompassViewOptions(
                    position: .topRight,
                    margins: .init(x: 16.0, y: 16.0),
                    image: SharedRes.images().ic_my_location_compass.toUIImage()!
                        .resized(to: CGSize(width: 48, height: 48)),
                    visibility: .adaptive
                )
            ))
            .onAppear {
                viewportObserver.onFollowToIdle = {
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
        withViewportAnimation(.default(maxDuration: MapConfiguration.shared.MAP_CAMERA_ANIM_DURATION_S)) {
            viewport = .overview(
                geometry: effect.bounds.lineString,
                bearing: effect.bearing?.cgFloat ?? 0,
                pitch: effect.pitch?.cgFloat ?? 0,
                geometryPadding: effect.contentPadding?.edgeInsets ?? .init()
            )
        }
    }

    private func showMyLocation(_ effect: MapUiEffectsShowMyLocation) {
        let duration = effect.animated ? MapConfiguration.shared.MAP_FOLLOW_ANIM_DURATION_S : 0

        withViewportAnimation(.default(maxDuration: duration)) {
            switch onEnum(of: effect.myLocationStatus) {
            case .following:
                viewport = .followPuck(
                    zoom: MapConfiguration.shared.FOLLOW_LOCATION_ZOOM_LEVEL,
                    bearing: .constant(0.0),
                    pitch: 0.0
                )
            case .followingLiveCompass:
                viewport = .followPuck(
                    zoom: MapConfiguration.shared.FOLLOW_LOCATION_ZOOM_LEVEL,
                    bearing: .heading,
                    pitch: MapConfiguration.shared.FOLLOW_LOCATION_LIVE_COMPASS_PITCH
                )
            default:
                break
            }
        }
    }
}
