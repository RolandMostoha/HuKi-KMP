import MapboxMaps
import Shared
import SwiftUI
import UniformTypeIdentifiers

struct MainView: View {
    @State private var viewModel = KoinViewModelProvider.shared.getMainViewModel()
    @State private var showFileImporter = false
    @State private var showAlert = false
    @State private var navigationPath = NavigationPath()

    private let strings = Strings()
    private let filePickerTypes = [UTType(filenameExtension: "gpx")!]
    private let searchBarHeight: CGFloat = 80
    private let mainActionGlassID: String = "main_action_glass_id"

    @Environment(\.verticalSizeClass) private var verticalSizeClass
    @Namespace private var mainActionGlassNamespace

    private var isLandscape: Bool {
        verticalSizeClass == .compact
    }

    var body: some View {
        NavigationStack(path: $navigationPath) {
            mainContent
                .navigationDestination(for: SettingsRoute.self) { _ in
                    SettingsView()
                }
        }
    }

    private var mainContent: some View {
        ZStack {
            Observing(viewModel.uiState) { uiState in
                MapView(
                    uiState: uiState,
                    onFollowingDisabled: {
                        viewModel.onEvent(event: MainUiEventsFollowingDisabled())
                    },
                    onGpxRouteClicked: { gpxDetails in
                        viewModel.onEvent(event: MainUiEventsGpxRouteClicked(gpxDetails: gpxDetails))
                    },
                    mapUiEffects: viewModel.mapUiEffects
                )
                .ignoresSafeArea()
                VStack {
                    Spacer()
                    FloatingActionContainer(
                        strings: strings,
                        uiState: uiState,
                        mainActionGlassID: mainActionGlassID,
                        mainActionGlassNamespace: mainActionGlassNamespace,
                        onLayersClicked: {
                            viewModel.onEvent(event: MainUiEventsLayersClicked.shared)
                        },
                        onMyLocationClicked: {
                            viewModel.onEvent(event: MainUiEventsMyLocationClicked.shared)
                        },
                        onSearchTap: {
                            viewModel.onEvent(event: MainUiEventsSearchClicked.shared)
                        },
                        onSettingsClick: {
                            navigationPath.append(SettingsRoute.settings)
                        }
                    )
                    .padding(.bottom, isLandscape ? 12 : 0)
                }
                .safeAreaPadding(.horizontal)
                    .sheet(
                        item: Binding(
                            get: { uiState.sheet },
                            set: { newValue in
                                if newValue == nil {
                                    viewModel.onEvent(event: MainUiEventsSheetDismissed())
                                }
                            }
                        )
                    ) { sheet in
                        switch onEnum(of: sheet) {
                        case .search:
                            SearchSheetView(
                                strings: strings,
                                onDismiss: {
                                    viewModel.onEvent(event: MainUiEventsSheetDismissed())
                                },
                                onPlaceSelected: { place in
                                    viewModel.onEvent(event: MainUiEventsSearchPlaceSelected(place: place))
                                }
                            )
                            .presentationDetents([.large])
                            .presentationDragIndicator(.visible)
                            .presentationCompactAdaptation(.sheet)
                        case .layers:
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
                                    viewModel.onEvent(event: MainUiEventsSheetDismissed())
                                }
                            )
                            .presentationDetents([.height(360)])
                            .presentationDragIndicator(.hidden)
                        case .gpx:
                            if let gpxDetails = uiState.mapUiState.gpxDetails {
                                GpxDetailsSheetView(
                                    strings: strings,
                                    gpxDetails: gpxDetails,
                                    onStartClick: {
                                        viewModel.onEvent(event: MainUiEventsGpxStartNavigationClicked())
                                    },
                                    onDismissRequest: {
                                        viewModel.onEvent(event: MainUiEventsGpxCloseClicked())
                                    }
                                )
                                .presentationDetents([.height(260)])
                                .presentationDragIndicator(.hidden)
                                .presentationBackgroundInteraction(.enabled)
                            } else {
                                EmptyView()
                            }
                        }
                    }
                .alert(
                    uiState.alert.map { strings.get(id: $0.title) } ?? "",
                    isPresented: $showAlert,
                    presenting: uiState.alert
                ) { _ in
                    Button(strings.get(id: SharedRes.strings().alert_ok)) {
                        viewModel.onEvent(event: MainUiEventsAlertDismissed.shared)
                    }
                } message: { alert in
                    Text(strings.get(id: alert.message))
                }
                .onChange(of: uiState.alert != nil) { _, newValue in
                    showAlert = newValue
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .task {
            for await effect in viewModel.mainUiEffects {
                handleMainEffects(effect)
            }
        }
        .fileImporter(isPresented: $showFileImporter, allowedContentTypes: filePickerTypes) { result in
            switch result {
            case .success(let url):
                viewModel.onEvent(event: MainUiEventsGpxFileSelected(uri: url.absoluteString))
            case .failure(let error):
                print(error)
            }
        }
    }

    private func handleMainEffects(_ effect: MainUiEffects) {
        switch onEnum(of: effect) {
        case .navigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        case .showGpxFilePicker:
            showFileImporter = true
        }
    }
}
