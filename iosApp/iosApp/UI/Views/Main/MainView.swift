import MapboxMaps
import Shared
import SwiftUI

struct MainView: View {
    @State private var viewModel = KoinViewModelProvider.shared.getMainViewModel()
    @State private var showLayersBottomSheet = false

    private let strings = Strings()

    var body: some View {
        ZStack {
            Observing(viewModel.uiState) { uiState in
                MapView(
                    uiState: uiState,
                    onFollowingDisabled: {
                        viewModel.onEvent(event: MainUiEventsFollowingDisabled())
                    },
                    mapUiEffects: viewModel.mapUiEffects
                )
                HStack {
                    Spacer()
                    VStack {
                        Spacer()
                        FloatingActionContainer(
                            strings: strings,
                            uiState: uiState,
                            onLayersClicked: { viewModel.onEvent(event: MainUiEventsLayersClicked.shared) },
                            onMyLocationClicked: { viewModel.onEvent(event: MainUiEventsMyLocationClicked.shared) }
                        )
                    }
                    .safeAreaPadding()
                    .padding(.bottom, 64)
                    .sheet(isPresented: $showLayersBottomSheet) {
                        LayersSheetView(
                            strings: strings,
                            selectedBaseLayer: uiState.mapUiState.baseLayer,
                            isHikingLayerSelected: uiState.mapUiState.hikingLayerVisible,
                            isGpxLayerSelected: uiState.mapUiState.gpxLayerVisible,
                            onBaseLayerSelected: { baseLayer in
                                viewModel.onEvent(event: MainUiEventsBaseLayerSelected(baseLayer: baseLayer))
                            },
                            onHikingLayerSelected: {
                                viewModel.onEvent(event: MainUiEventsHikingLayerSelected())
                            },
                            onGpxLayerSelected: {
                                viewModel.onEvent(event: MainUiEventsGpxLayerSelected())
                            },
                            onDismissRequest: {
                                showLayersBottomSheet = false
                            }
                        )
                        .presentationDetents([.height(360)])
                        .presentationDragIndicator(.hidden)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .ignoresSafeArea()
        .task {
            for await effect in viewModel.mainUiEffects {
                handleMainEffects(effect)
            }
        }
    }

    private struct FloatingActionContainer: View {
        let strings: Strings
        let uiState: MainUiState
        let onLayersClicked: () -> Void
        let onMyLocationClicked: () -> Void

        private let mainActionGlassID: String = "main_action_glass_id"
        @Namespace private var mainActionGlassNamespace

        var body: some View {
            GlassContainer {
                VStack {
                    Button(
                        action: {
                            onLayersClicked()
                        },
                        label: {
                            Image(systemName: "map.fill")
                                .fontWeight(.bold)
                                .floatingButtonPadding(.top)
                        }
                    )
                    .glassButtonStyle()
                    .glassUnion(id: mainActionGlassID, namespace: mainActionGlassNamespace)
                    .accessibilityLabel(strings.get(id: SharedRes.strings().layers_a11y_fab, args: []))
                    Button(
                        action: {
                            onMyLocationClicked()
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
                                .foregroundColor(Color(SharedRes.colors().primary.getUIColor()))
                                .floatingButtonPadding(.bottom)
                        }
                    )
                    .glassButtonStyle()
                    .glassUnion(id: mainActionGlassID, namespace: mainActionGlassNamespace)
                    .accessibilityIdentifier(TestTags.shared.MAIN_FAB_MY_LOCATION_BUTTON)
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
            }
        }
    }

    private func handleMainEffects(_ effect: MainUiEffects) {
        switch onEnum(of: effect) {
        case .navigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        case .showLayersBottomSheet:
            showLayersBottomSheet.toggle()
        }
    }
}
