import MapboxMaps
import Shared
import SwiftUI

struct MainView: View {
    @State private var viewModel = KoinViewModelProvider.shared.getMainViewModel()
    @State private var viewport = Viewport.camera(
        center: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.location.coordinate,
        zoom: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.zoom,
        bearing: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.bearing,
        pitch: MapConfiguration.shared.HUNGARY_CAMERA_POSITION.pitch
    )

    private let strings = Strings()
    private let viewportObserver = ViewportObserver()

    var body: some View {
        ZStack {
            Observing(viewModel.uiState) { uiState in
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
                            viewModel.onEvent(event: MainUiEventsFollowingDisabled())
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
                                viewModel.onEvent(event: MainUiEventsMyLocationClicked.shared)
                            },
                            label: {
                                Image(systemName: {
                                    switch onEnum(of: uiState.myLocationState.myLocationStatus) {
                                    case .default, .notAvailable:
                                        return "location.north"
                                    case .following:
                                        return "location.fill"
                                    case .followingLiveCompass:
                                        return "location.north.line.fill"
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
                                    switch onEnum(of: uiState.myLocationState.myLocationStatus) {
                                    case .default:
                                        return SharedRes.strings().my_location_a11y_default
                                    case .following:
                                        return SharedRes.strings().my_location_a11y_following
                                    case .followingLiveCompass:
                                        return SharedRes.strings().my_location_a11y_live_compass
                                    case .notAvailable:
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
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .ignoresSafeArea()
        .task {
            for await effect in viewModel.uiEffect {
                handleEffect(effect)
            }
        }
    }

    private func handleEffect(_ effect: MainUiEffects) {
        switch onEnum(of: effect) {
        case .updateCamera(let effect):
            withViewportAnimation(.default(maxDuration: MapConfiguration.shared.MAP_CAMERA_ANIM_DURATION_S)) {
                viewport = .camera(
                    center: effect.location?.coordinate,
                    zoom: effect.zoom?.cgFloat,
                    bearing: effect.bearing?.doubleValue,
                    pitch: effect.pitch?.cgFloat
                )
            }
        case .showMyLocation(let effect):
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
                        pitch: MapConfiguration.shared.FOLLOW_LOCATION_PITCH
                    )
                default:
                    break
                }
            }
        case .navigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
    }
}
