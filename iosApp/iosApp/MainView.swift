import MapboxMaps
import Shared
import SwiftUI

struct MainView: View {
    @ObservedObject private var mainViewModel = MainViewModelWrapper()
    @State private var viewport = Viewport.camera()

    private let strings = Strings()

    var body: some View {
        ZStack {
            Map(viewport: $viewport) {
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
            .accessibilityIdentifier(TestTags.shared.MAP_MAPBOX)
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button(
                        action: {
                            mainViewModel.viewModel.onEvent(event: MainUiEventsMyLocationClicked.shared)
                        },
                        label: {
                            Image(systemName: "location.fill")
                                .fontWeight(.bold)
                                .padding(8)
                        }
                    )
                    .accessibilityIdentifier(TestTags.shared.MAIN_MY_LOCATION_BUTTON)
                    .buttonBorderShape(.circle)
                    .glassIfAvailable()
                    .accessibilityLabel(strings.get(id: SharedRes.strings().main_my_location_accessibility, args: []))
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

    private func handleInitialState(_ initialUiState: MainUiState) {
        viewport = .camera(
            center: initialUiState.mapUiState.cameraPosition.location.coordinate,
            zoom: mainViewModel.uiState.mapUiState.cameraPosition.zoom
        )
    }

    private func handleEffect(_ effect: MainUiEffects) {
        switch effect {
        case let effect as MainUiEffectsMoveCamera:
            withViewportAnimation(.default(maxDuration: 1)) {
                viewport = .camera(
                    center: effect.cameraPosition.location.coordinate,
                    zoom: effect.cameraPosition.zoom
                )
            }
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
