import MapboxMaps
import Shared
import SwiftUI
import UniformTypeIdentifiers

struct MainView: View {
    @State var viewModel = KoinViewModelProvider.shared.getMainViewModel()
    @State var navigationPath = NavigationPath()
    @State var gpxDetent: PresentationDetent = .height(Dimens.gpxDetailsCollapsedDetentHeight)
    @State var placeDetailsHeight: CGFloat = Dimens.placeDetailsDetentHeight
    @State private var presentedSheet: Sheet?
    @State private var showFileImporter = false
    @State private var showAlert = false

    let strings = Strings()
    private let filePickerTypes = [UTType(filenameExtension: "gpx")!]
    private let searchBarHeight: CGFloat = 80
    private let mainActionGlassID: String = "main_action_glass_id"

    @Environment(\.layoutMode) var layoutMode
    @Namespace private var mainActionGlassNamespace

    var isLandscape: Bool { layoutMode.isLandscape }
    var isPad: Bool { layoutMode.isPad }

    var body: some View {
        NavigationStack(path: $navigationPath) {
            navigationDestinations {
                mainContent
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
                    onMapLongClicked: { location in
                        viewModel.onEvent(event: MainUiEventsMapLongClicked(location: location))
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
                        onZoomInClicked: {
                            viewModel.onEvent(event: MainUiEventsZoomInClicked.shared)
                        },
                        onZoomOutClicked: {
                            viewModel.onEvent(event: MainUiEventsZoomOutClicked.shared)
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
                .safeAreaPadding(isLandscape ? .leading : .horizontal)
                .ignoresSafeArea(.container, edges: isLandscape ? .bottom : [])
                    .sheet(
                        item: Binding(
                            get: { uiState.sheet },
                            set: { newValue in
                                if newValue == nil, let dismissed = presentedSheet {
                                    presentedSheet = nil
                                    viewModel.onEvent(event: MainUiEventsSheetSwipeDismissed(sheet: dismissed))
                                }
                            }
                        )
                    ) { sheet in
                        sheetContent(for: sheet, uiState: uiState)
                            .onAppear { presentedSheet = sheet }
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
                viewModel.onEvent(event: MainUiEventsGpxFileSelected(uri: url.absoluteString, source: .layers))
            case .failure(let error):
                logError(message: "Failed to import GPX file: \(error)")
            }
        }
    }

}

private extension MainView {
    func navigationDestinations<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        content()
            .navigationDestination(for: MenuRoute.self) { _ in
                MenuView(
                    onSettingsClicked: { navigationPath.append(SettingsRoute.settings) },
                    onDestinationsClicked: { navigationPath.append(DestinationsRoute.destinations) },
                    onGpxCollectionClicked: { navigationPath.append(GpxCollectionRoute.gpxCollection) },
                    onGpxGuideClicked: { navigationPath.append(GpxGuideRoute.gpxGuide) },
                    onTrailSymbolsGuideClicked: { navigationPath.append(TrailSymbolsGuideRoute.trailSymbolsGuide) },
                    onPlaceHistoryClicked: { navigationPath.append(PlaceHistoryRoute.placeHistory) },
                    onLocationIqClicked: { navigationPath.append(LocationIqRoute.locationIq) }
                )
            }
            .navigationDestination(for: SettingsRoute.self) { _ in
                SettingsView()
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
                    onOpenTutorial: { navigationPath.append(GpxGuideRoute.gpxGuide) },
                    onOpenGpx: { uri in
                        viewModel.onEvent(event: MainUiEventsGpxFileReopened(uri: uri))
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
            .navigationDestination(for: GpxGuideRoute.self) { _ in
                GpxGuideView()
            }
            .navigationDestination(for: TrailSymbolsGuideRoute.self) { _ in
                TrailSymbolsGuideView()
            }
    }

    @ViewBuilder
    func gpxControlMenu(uiState: MainUiState) -> some View {
        if uiState.sheet == nil, uiState.mapUiState.gpxDetails != nil, uiState.mapUiState.gpxLayerVisible {
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
            .ignoresSafeArea(.container, edges: isLandscape ? .bottom : [])
            .transition(.opacity)
        }
    }

    func handleMainEffects(_ effect: MainUiEffects) {
        switch onEnum(of: effect) {
        case .navigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        case .showGpxFilePicker:
            showFileImporter = true
        case .openMapsNavigation(let effect):
            MapsNavigator.openDirections(to: effect.location)
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
            viewModel.onEvent(event: MainUiEventsGpxFileSelected(uri: destination.absoluteString, source: .files))
            navigationPath = NavigationPath()
        } catch {
            logError(message: "Failed to copy incoming GPX file: \(error)")
        }
    }
}
