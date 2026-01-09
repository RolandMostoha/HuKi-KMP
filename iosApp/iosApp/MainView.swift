import MapboxMaps
import Shared
import SwiftUI

struct MainView: View {
    @ObservedObject private var mainViewModel = MainViewModelWrapper()
    @State private var viewport = Viewport.camera()

    var body: some View {
        ZStack {
            Map(viewport: $viewport).mapStyle(.outdoors)
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
                    .buttonBorderShape(.circle)
                    .glassIfAvailable()
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
