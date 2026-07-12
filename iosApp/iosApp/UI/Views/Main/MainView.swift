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
                .navigationDestination(for: MenuRoute.self) { _ in
                    MenuView(
                        onDestinationsClicked: { navigationPath.append(DestinationsRoute.destinations) },
                        onGpxCollectionClicked: { navigationPath.append(GpxCollectionRoute.gpxCollection) },
                        onPlaceHistoryClicked: { navigationPath.append(PlaceHistoryRoute.placeHistory) },
                        onLocationIqClicked: { navigationPath.append(LocationIqRoute.locationIq) }
                    )
                }
                .navigationDestination(for: DestinationsRoute.self) { _ in
                    DestinationsView(
                        onShowOnMap: { destination in
                            viewModel.onEvent(event: MainUiEventsSearchDestinationSelected(destination: destination))
                            navigationPath = NavigationPath()
                        }
                    )
                }
                .navigationDestination(for: GpxCollectionRoute.self) { _ in
                    GpxCollectionView(
                        onOpenTutorial: { navigationPath.append(GpxTutorialRoute.gpxTutorial) },
                        onOpenGpx: { uri in
                            viewModel.onEvent(event: MainUiEventsGpxFileSelected(uri: uri))
                            navigationPath = NavigationPath()
                        }
                    )
                }
                .navigationDestination(for: PlaceHistoryRoute.self) { _ in
                    PlaceHistoryView(
                        onOpenPlace: { osmType, osmId in
                            let event = MainUiEventsHistoryPlaceSelected(osmType: osmType, osmId: osmId)
                            viewModel.onEvent(event: event)
                            navigationPath = NavigationPath()
                        }
                    )
                }
                .navigationDestination(for: LocationIqRoute.self) { _ in
                    LocationIqView()
                }
                .navigationDestination(for: GpxTutorialRoute.self) { _ in
                    GpxTutorialView()
                }
        }
        .onOpenURL { url in
            handleIncomingGpx(url)
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
                    onMyLocationReceived: {
                        viewModel.onEvent(event: MainUiEventsMyLocationReceived.shared)
                    },
                    onCompassClicked: {
                        viewModel.onEvent(event: MainUiEventsCompassClicked.shared)
                    },
                    onWaypointClicked: { waypoint in
                        viewModel.onEvent(event: MainUiEventsGpxWaypointClicked(waypoint: waypoint))
                    },
                    onDistanceInfoWindowDismissed: {
                        viewModel.onEvent(event: MainUiEventsDistanceInfoWindowDismissed.shared)
                    },
                    mapUiEffects: viewModel.mapUiEffects
                )
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
                        onMenuClick: {
                            navigationPath.append(MenuRoute.menu)
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
                                },
                                onDestinationSelected: { destination in
                                    let event = MainUiEventsSearchDestinationSelected(destination: destination)
                                    viewModel.onEvent(event: event)
                                },
                                onGpxFileSelected: { uri in
                                    viewModel.onEvent(event: MainUiEventsGpxFileSelected(uri: uri))
                                },
                                onSeeAllGpxClicked: {
                                    viewModel.onEvent(event: MainUiEventsSheetDismissed())
                                    navigationPath.append(GpxCollectionRoute.gpxCollection)
                                },
                                onSeeAllPlacesClicked: {
                                    viewModel.onEvent(event: MainUiEventsSheetDismissed())
                                    navigationPath.append(PlaceHistoryRoute.placeHistory)
                                },
                                onSeeAllDestinationsClicked: {
                                    viewModel.onEvent(event: MainUiEventsSheetDismissed())
                                    navigationPath.append(DestinationsRoute.destinations)
                                },
                                onLocationIqClicked: {
                                    viewModel.onEvent(event: MainUiEventsSheetDismissed())
                                    navigationPath.append(LocationIqRoute.locationIq)
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
                gpxControlMenu(uiState: uiState)
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

}

private extension MainView {
    @ViewBuilder
    func gpxControlMenu(uiState: MainUiState) -> some View {
        if uiState.sheet == nil, uiState.mapUiState.gpxDetails != nil {
            GpxControlMenu(
                strings: strings,
                isRouteVisible: uiState.mapUiState.gpxRouteVisible,
                isAllDistancesVisible: uiState.mapUiState.allDistancesVisible,
                onToggleLine: {
                    viewModel.onEvent(event: MainUiEventsGpxRouteVisibilityToggled())
                },
                onToggleDistances: {
                    viewModel.onEvent(event: MainUiEventsGpxDistancesVisibilityToggled())
                },
                onOverview: {
                    viewModel.onEvent(event: MainUiEventsGpxOverviewClicked())
                },
                onClear: {
                    viewModel.onEvent(event: MainUiEventsGpxCloseClicked())
                }
            )
            .safeAreaPadding(.horizontal)
            .padding(.bottom, isLandscape ? 12 : 0)
            .transition(.opacity)
        }
    }

    func handleMainEffects(_ effect: MainUiEffects) {
        switch onEnum(of: effect) {
        case .navigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        case .showGpxFilePicker:
            showFileImporter = true
        }
    }

    func handleIncomingGpx(_ url: URL) {
        let needsAccess = url.startAccessingSecurityScopedResource()
        defer { if needsAccess { url.stopAccessingSecurityScopedResource() } }
        let tempDir = FileManager.default
            .temporaryDirectory
            .appendingPathComponent(UUID().uuidString, isDirectory: true)
        let destination = tempDir.appendingPathComponent(url.lastPathComponent)
        do {
            try FileManager.default.createDirectory(at: tempDir, withIntermediateDirectories: true)
            try FileManager.default.copyItem(at: url, to: destination)
            viewModel.onEvent(event: MainUiEventsGpxFileSelected(uri: destination.absoluteString))
            navigationPath = NavigationPath()
        } catch {
            print("Failed to copy incoming GPX file: \(error)")
        }
    }
}
