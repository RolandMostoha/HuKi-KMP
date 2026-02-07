import MapboxMaps
import Shared
import SwiftUI

struct MainView: View {
    @StateObject private var mainViewModel = MainViewModelWrapper()
    @State private var viewport = Viewport.camera()

    private let strings = Strings()
    private let viewportObserver = ViewportObserver()

    var body: some View {
        ZStack {
            MapReader { proxy in
                Map(viewport: $viewport) {
                    if mainViewModel.uiState.myLocationState.permissionState == PermissionState.granted {
                        Puck2D(bearing: .heading)
                            .showsAccuracyRing(true)
                            .accuracyRingColor(SharedRes.colors().accuracyRing.getUIColor())
                            .pulsing(.init(color: SharedRes.colors().primaryLight.getUIColor()))
                            .topImage(SharedRes.images().ic_my_location_top_image.toUIImage())
                            .bearingImage(SharedRes.images().ic_my_location_bearing.toUIImage())
                            .scale(1.2)
                    }
                    RasterSource(id: Layer.turistautak.layerId)
                        .tiles(Layer.turistautak.tiles)
                        .tileSize(Double(Layer.turistautak.tileSize))
                        .minzoom(Double(Layer.turistautak.minZoom))
                        .maxzoom(Double(Layer.turistautak.maxZoom))
                    RasterLayer(id: Layer.turistautak.layerId, source: Layer.turistautak.layerId)
                }
                .mapStyle(.outdoors)
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
                        mainViewModel.viewModel.onEvent(event: MainUiEventsFollowingDisabled())
                    }
                    proxy.viewport?.addStatusObserver(viewportObserver)
                }
                .onDisappear {
                    proxy.viewport?.removeStatusObserver(viewportObserver)
                }
                .accessibilityIdentifier(TestTags.shared.MAP_MAPBOX)
            }
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button(
                        action: {
                            mainViewModel.viewModel.onEvent(event: MainUiEventsMyLocationClicked.shared)
                        },
                        label: {
                            Image(systemName: {
                                switch mainViewModel.uiState.myLocationState.myLocationStatus {
                                case is MyLocationStatusDefault, is MyLocationStatusNotAvailable:
                                    return "location.north"
                                case is MyLocationStatusFollowing:
                                    return "location.fill"
                                case is MyLocationStatusFollowingLiveCompass:
                                    return "location.north.line.fill"
                                default:
                                    return "location"
                                }
                            }())
                                .fontWeight(.bold)
                                .padding(8)
                        }
                    )
                    .accessibilityIdentifier(TestTags.shared.MAIN_MY_LOCATION_BUTTON)
                    .buttonBorderShape(.circle)
                    .glassIfAvailable()
                    .accessibilityLabel(
                        strings.get(
                            id: {
                                switch mainViewModel.uiState.myLocationState.myLocationStatus {
                                case is MyLocationStatusDefault:
                                    return SharedRes.strings().my_location_a11y_default
                                case is MyLocationStatusFollowing:
                                    return SharedRes.strings().my_location_a11y_following
                                case is MyLocationStatusFollowingLiveCompass:
                                    return SharedRes.strings().my_location_a11y_live_compass
                                case is MyLocationStatusNotAvailable:
                                    return SharedRes.strings().my_location_a11y_not_available
                                default:
                                    return SharedRes.strings().my_location_a11y_not_available
                                }
                            }(),
                            args: []
                        )
                    )
                }
                .safeAreaPadding()
                .padding(.bottom, 64)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .ignoresSafeArea()
        .task { handleInitialState(await mainViewModel.observeUiState()) }
        .task {
            await mainViewModel.observeUiEffect { effect in
                handleEffect(effect)
            }
        }
    }

    @MainActor
    private func handleInitialState(_ initialUiState: MainUiState) {
        viewport = .camera(
            center: initialUiState.mapUiState.cameraPosition.location.coordinate,
            zoom: initialUiState.mapUiState.cameraPosition.zoom,
            bearing: initialUiState.mapUiState.cameraPosition.bearing,
            pitch: initialUiState.mapUiState.cameraPosition.pitch
        )
    }

    @MainActor
    private func handleEffect(_ effect: MainUiEffects) {
        switch effect {
        case let effect as MainUiEffectsUpdateCamera:
            withViewportAnimation(.default(maxDuration: MapConfiguration.shared.MAP_CAMERA_ANIM_DURATION_S)) {
                viewport = .camera(
                    center: effect.location?.coordinate,
                    zoom: effect.zoom?.cgFloat,
                    bearing: effect.bearing?.doubleValue,
                    pitch: effect.pitch?.cgFloat
                )
            }
        case let effect as MainUiEffectsShowMyLocation:
            let duration = effect.animated ? MapConfiguration.shared.MAP_FOLLOW_ANIM_DURATION_S : 0

            withViewportAnimation(.default(maxDuration: duration)) {
                switch effect.myLocationStatus {
                case is MyLocationStatusFollowing:
                    viewport = .followPuck(
                        zoom: MapConfiguration.shared.FOLLOW_LOCATION_ZOOM_LEVEL,
                        bearing: .constant(0.0),
                        pitch: 0.0
                    )
                case is MyLocationStatusFollowingLiveCompass:
                    viewport = .followPuck(
                        zoom: MapConfiguration.shared.FOLLOW_LOCATION_ZOOM_LEVEL,
                        bearing: .heading,
                        pitch: MapConfiguration.shared.FOLLOW_LOCATION_PITCH
                    )
                default:
                    break
                }
            }
        case _ as MainUiEffectsNavigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        default:
            break
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
    }
}
