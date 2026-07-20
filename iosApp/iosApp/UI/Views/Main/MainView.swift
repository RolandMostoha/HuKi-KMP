import MapboxMaps
import Shared
import SwiftUI
import UniformTypeIdentifiers

struct MainView: View {
    @State private var viewModel = KoinViewModelProvider.shared.getMainViewModel()
    @State private var showFileImporter = false
    @State private var showAlert = false
    @State private var navigationPath = NavigationPath()
    @State private var gpxDetent: PresentationDetent = .height(Dimens.gpxDetailsCollapsedDetentHeight)

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
                        sheetContent(for: sheet, uiState: uiState)
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
    func navigationDestinations<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        content()
            .navigationDestination(for: MenuRoute.self) { _ in
                MenuView(
                    onSettingsClicked: { navigationPath.append(SettingsRoute.settings) },
                    onDestinationsClicked: { navigationPath.append(DestinationsRoute.destinations) },
                    onGpxCollectionClicked: { navigationPath.append(GpxCollectionRoute.gpxCollection) },
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

    @ViewBuilder
    func sheetContent(for sheet: Sheet, uiState: MainUiState) -> some View {
        switch onEnum(of: sheet) {
        case .search:
            searchSheet()
        case .layers:
            layersSheet(uiState: uiState)
        case .gpx:
            gpxSheet(uiState: uiState)
        case .whatsNew(let sheet):
            whatsNewSheet(whatsNew: sheet.whatsNew)
        }
    }

    func whatsNewSheet(whatsNew: WhatsNew) -> some View {
        let detentHeight = whatsNew.message != nil
            ? Dimens.whatsNewDetentHeightWithMessage
            : Dimens.whatsNewDetentHeight
        return WhatsNewSheetView(
            strings: strings,
            whatsNew: whatsNew,
            onDismissRequest: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
            }
        )
        .presentationDetents([.height(detentHeight)])
        .presentationDragIndicator(.hidden)
    }

    func searchSheet() -> some View {
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
    }

    func layersSheet(uiState: MainUiState) -> some View {
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
    }

    @ViewBuilder
    func gpxSheet(uiState: MainUiState) -> some View {
        if let gpxDetails = uiState.mapUiState.gpxDetails {
            GpxDetailsSheetView(
                strings: strings,
                gpxDetails: gpxDetails,
                onStartClick: {
                    viewModel.onEvent(event: MainUiEventsGpxStartNavigationClicked())
                },
                onNavigateToStart: {
                    viewModel.onEvent(event: MainUiEventsGpxMapsNavigationClicked(type: .start))
                },
                onNavigateToEnd: {
                    viewModel.onEvent(event: MainUiEventsGpxMapsNavigationClicked(type: .end))
                },
                onDismissRequest: {
                    viewModel.onEvent(event: MainUiEventsGpxCloseClicked())
                }
            )
            .presentationDetents(
                [
                    .height(Dimens.gpxDetailsCollapsedDetentHeight),
                    .height(
                        gpxDetails.waypoints.contains { $0.type == .end }
                            ? Dimens.gpxDetailsExpandedWithEndDetentHeight
                            : Dimens.gpxDetailsExpandedDetentHeight
                    )
                ],
                selection: $gpxDetent
            )
            .presentationDragIndicator(.visible)
            .presentationBackgroundInteraction(.enabled)
            .presentationContentInteraction(.resizes)
            .onChange(of: gpxDetails.fileUri, initial: true) {
                gpxDetent = .height(Dimens.gpxDetailsCollapsedDetentHeight)
            }
        } else {
            EmptyView()
        }
    }

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
            viewModel.onEvent(event: MainUiEventsGpxFileSelected(uri: destination.absoluteString))
            navigationPath = NavigationPath()
        } catch {
            print("Failed to copy incoming GPX file: \(error)")
        }
    }
}
