import Shared
import SwiftUI

extension MainView {
    @ViewBuilder
    func sheetContent(for sheet: Sheet, uiState: MainUiState) -> some View {
        switch onEnum(of: sheet) {
        case .search:
            searchSheet()
        case .layers:
            layersSheet(uiState: uiState)
        case .gpx:
            gpxSheet(uiState: uiState)
        case .placeDetails:
            placeDetailsSheet(uiState: uiState)
        case .routePlanner(let sheet):
            routePlannerSheet(stops: sheet.stops, restored: sheet.restored, mode: sheet.mode)
        case .whatsNew(let sheet):
            whatsNewSheet(whatsNew: sheet.whatsNew, supporterAnimal: uiState.supporterAnimal)
        case .discover:
            discoverSheet()
        case .okt:
            oktSheet(uiState: uiState)
        case .landscapes:
            landscapesSheet(uiState: uiState)
        case .mapLabelScale:
            mapLabelScaleSheet(uiState: uiState)
        }
    }

    @ViewBuilder
    func landscapesSheet(uiState: MainUiState) -> some View {
        if let landscapes = uiState.mapUiState.landscapes {
            LandscapesSheetView(
                strings: strings,
                landscapes: landscapes,
                onLandscapeClicked: { landscapeId in
                    collapseLandscapesSheet()
                    viewModel.onEvent(event: LandscapesUiEventsLandscapeClicked(landscapeId: landscapeId))
                },
                onCategoryClicked: { categoryId in
                    collapseLandscapesSheet()
                    viewModel.onEvent(event: LandscapesUiEventsCategoryClicked(categoryId: categoryId))
                },
                onShowAllClicked: {
                    viewModel.onEvent(event: LandscapesUiEventsLandscapesShowAllClicked.shared)
                },
                onExpandToggleClicked: isLandscape ? nil : toggleLandscapesSheet,
                isExpanded: landscapesDetent == .large,
                onDestinationClicked: { destination in
                    collapseLandscapesSheet()
                    viewModel.onEvent(event: LandscapesUiEventsLandscapesDestinationClicked(destination: destination))
                },
                onDismissRequest: {
                    viewModel.onEvent(event: LandscapesUiEventsLandscapesCloseClicked.shared)
                }
            )
            .presentationDetents(
                isLandscape ? [.large] : [.height(landscapesDetentHeight), .large],
                selection: $landscapesDetent
            )
            .presentationDragIndicator(.visible)
            .presentationBackgroundInteraction(.enabled(upThrough: .height(landscapesDetentHeight)))
            .presentationContentInteraction(.resizes)
            .onAppear {
                // Without a selected category the map has nothing to show yet
                let isCategoryUnselected = landscapes.grouping == .category && landscapes.selectedCategoryId == nil
                landscapesDetent = isLandscape || isCategoryUnselected ? .large : .height(landscapesDetentHeight)
            }
        } else {
            EmptyView()
        }
    }

    private func toggleLandscapesSheet() {
        withAnimation(.snappy) {
            landscapesDetent = landscapesDetent == .large ? .height(landscapesDetentHeight) : .large
        }
    }

    // The selection is shown on the map, which the expanded sheet would cover
    private func collapseLandscapesSheet() {
        guard !isLandscape else { return }
        withAnimation(.snappy) {
            landscapesDetent = .height(landscapesDetentHeight)
        }
    }

    @ViewBuilder
    func oktSheet(uiState: MainUiState) -> some View {
        if let okt = uiState.mapUiState.okt {
            OktSheetView(
                strings: strings,
                okt: okt,
                onSectionClicked: { sectionId in
                    viewModel.onEvent(event: OktUiEventsOktSectionClicked(sectionId: sectionId))
                },
                onSectionStartClicked: { sectionId in
                    viewModel.onEvent(event: OktUiEventsOktSectionStartClicked(sectionId: sectionId))
                },
                onSectionWebsiteClicked: { sectionId in
                    viewModel.onEvent(event: OktUiEventsOktSectionWebsiteClicked(sectionId: sectionId))
                },
                onSectionReverseClicked: { sectionId in
                    viewModel.onEvent(event: OktUiEventsOktSectionReverseClicked(sectionId: sectionId))
                },
                onStampClicked: { marker in
                    viewModel.onEvent(event: OktUiEventsOktMarkerClicked(marker: marker))
                },
                onDismissRequest: {
                    viewModel.onEvent(event: OktUiEventsOktCloseClicked.shared)
                }
            )
            .presentationDetents(
                isLandscape ? [.large] : [.height(oktDetentHeight), .large],
                selection: $oktDetent
            )
            .presentationDragIndicator(.visible)
            .presentationBackgroundInteraction(.enabled(upThrough: .height(oktDetentHeight)))
            .presentationContentInteraction(.scrolls)
            .onAppear {
                oktDetent = isLandscape ? .large : .height(oktDetentHeight)
            }
        } else {
            EmptyView()
        }
    }

    func discoverSheet() -> some View {
        DiscoverSheetView(
            strings: strings,
            onHikeRecommendationClicked: { recommendation in
                viewModel.onEvent(event: MainUiEventsHikeRecommendationClicked(recommendation: recommendation))
            },
            onBrowseDestinationsClicked: {
                viewModel.onEvent(event: MainUiEventsDiscoverBrowseDestinationsClicked.shared)
            },
            onInfoClicked: {
                viewModel.onEvent(event: MainUiEventsHikeRecommendationsInfoClicked.shared)
            },
            onGpxGuideClicked: {
                viewModel.onEvent(event: MainUiEventsHikeRecommendationsGpxGuideClicked.shared)
            },
            onOktTypeClicked: { type in
                viewModel.onEvent(event: OktUiEventsOktTypeClicked(type: type))
            },
            onOktInfoClicked: {
                viewModel.onEvent(event: OktUiEventsOktInfoClicked.shared)
            },
            onShowLandscapesClicked: {
                viewModel.onEvent(event: LandscapesUiEventsLandscapesShowClicked.shared)
            },
            onShowCategoriesClicked: {
                viewModel.onEvent(event: LandscapesUiEventsCategoriesShowClicked.shared)
            },
            onDismissRequest: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
            },
            onHeightChange: { height in
                discoverHeight = height
            }
        )
        .presentationDetents([isLandscape ? .large : .height(discoverHeight)])
        .presentationDragIndicator(.hidden)
    }

    func whatsNewSheet(whatsNew: WhatsNew, supporterAnimal: SupportAnimal?) -> some View {
        WhatsNewSheetView(
            strings: strings,
            whatsNew: whatsNew,
            supporterAnimal: supporterAnimal,
            onDismissRequest: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
            },
            onHeightChange: { height in
                whatsNewHeight = height
            }
        )
        .presentationDetents([.height(whatsNewHeight)])
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
            onRecentPlaceSelected: { place in
                viewModel.onEvent(event: MainUiEventsSearchRecentPlaceSelected(place: place))
            },
            onSearchPlaceHistorySelected: { place in
                viewModel.onEvent(event: MainUiEventsSearchResultPlaceHistorySelected(place: place))
            },
            onDestinationSelected: { destination in
                let event = MainUiEventsSearchDestinationSelected(destination: destination)
                viewModel.onEvent(event: event)
            },
            onSearchDestinationSelected: { destination in
                let event = MainUiEventsSearchResultDestinationSelected(destination: destination)
                viewModel.onEvent(event: event)
            },
            onGpxFileSelected: { uri in
                viewModel.onEvent(event: GpxUiEventsGpxFileReopened(uri: uri))
            },
            onSeeAllGpxClicked: { openSearchSeeAll(.gpx, route: GpxCollectionRoute.gpxCollection) },
            onSeeAllPlacesClicked: { openSearchSeeAll(.places, route: PlaceHistoryRoute.placeHistory) },
            onSeeAllDestinationsClicked: { openSearchSeeAll(.destinations, route: DestinationsRoute.destinations) },
            onLocationIqClicked: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
                navigationPath.append(LocationIqRoute.locationIq)
            }
        )
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
        .presentationCompactAdaptation(.sheet)
    }

    private func openSearchSeeAll(_ section: SearchSection, route: some Hashable) {
        viewModel.onEvent(event: MainUiEventsSearchSeeAllClicked(section: section))
        viewModel.onEvent(event: MainUiEventsSheetDismissed())
        navigationPath.append(route)
    }

    func layersSheet(uiState: MainUiState) -> some View {
        LayersSheetView(
            strings: strings,
            selectedBaseLayer: uiState.mapUiState.baseLayer,
            isHikingLayerSelected: uiState.mapUiState.hikingLayerVisible,
            isBikeLayerSelected: uiState.mapUiState.bikeLayerVisible,
            isGpxLayerSelected: uiState.mapUiState.gpxLayerVisible,
            onBaseLayerSelected: { baseLayer in
                viewModel.onEvent(event: MainUiEventsBaseLayerSelected(baseLayer: baseLayer))
            },
            onHikingLayerSelected: {
                viewModel.onEvent(event: MainUiEventsHikingLayerSelected())
            },
            onBikeLayerSelected: {
                viewModel.onEvent(event: MainUiEventsBikeLayerSelected())
            },
            onGpxLayerSelected: {
                viewModel.onEvent(event: GpxUiEventsGpxLayerSelected())
            },
            onDismissRequest: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
            }
        )
        .presentationDetents([layersDetent])
        .presentationDragIndicator(.hidden)
    }

    func mapLabelScaleSheet(uiState: MainUiState) -> some View {
        MapLabelScaleSheetView(
            strings: strings,
            labelScale: uiState.mapUiState.labelScale,
            onFactorChanged: { factor in
                viewModel.onEvent(event: MainUiEventsMapLabelScaleChanged(factor: factor))
            },
            onFactorCommitted: { factor in
                viewModel.onEvent(event: MainUiEventsMapLabelScaleCommitted(factor: factor))
            },
            onDismissRequest: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
            }
        )
        .presentationDragIndicator(.hidden)
    }

    private var layersDetent: PresentationDetent {
        if isPad {
            return .height(Dimens.layersPadDetentHeight)
        }
        return isLandscape ? .large : .height(Dimens.layersDetentHeight)
    }

    private var gpxCollapsedDetent: PresentationDetent {
        .height(gpxCollapsedHeight)
    }

    private func updateGpxCollapsedHeight(_ height: CGFloat) {
        let height = height.rounded()
        guard height != gpxCollapsedHeight else { return }
        let wasCollapsed = gpxDetent == gpxCollapsedDetent
        gpxCollapsedHeight = height
        if wasCollapsed {
            gpxDetent = gpxCollapsedDetent
        }
    }

    private var gpxExpandedDetent: PresentationDetent {
        .height(gpxExpandedHeight)
    }

    private func updateGpxExpandedHeight(_ height: CGFloat) {
        let height = height.rounded()
        guard height != gpxExpandedHeight else { return }
        let wasExpanded = gpxDetent == gpxExpandedDetent
        gpxExpandedHeight = height
        if wasExpanded {
            gpxDetent = gpxExpandedDetent
        }
    }

    @ViewBuilder
    func gpxSheet(uiState: MainUiState) -> some View {
        if let gpxDetails = uiState.mapUiState.gpxDetails {
            GpxDetailsSheetView(
                strings: strings,
                gpxDetails: gpxDetails,
                isRouteVisible: uiState.mapUiState.gpxRouteVisible,
                onStartClick: {
                    viewModel.onEvent(event: GpxUiEventsGpxStartNavigationClicked())
                },
                onRouteVisibilityClick: {
                    viewModel.onEvent(event: GpxUiEventsGpxRouteVisibilityToggled())
                },
                onNavigateToStart: {
                    viewModel.onEvent(event: GpxUiEventsGpxMapsNavigationClicked(type: .start))
                },
                onNavigateToEnd: {
                    viewModel.onEvent(event: GpxUiEventsGpxMapsNavigationClicked(type: .end))
                },
                onShareClick: {
                    viewModel.onEvent(event: GpxUiEventsGpxShareClicked())
                },
                onReverseDirectionClick: {
                    viewModel.onEvent(event: GpxUiEventsGpxDirectionReversed())
                },
                onDismissRequest: {
                    viewModel.onEvent(event: GpxUiEventsGpxCloseClicked())
                },
                onCollapsedHeightChange: updateGpxCollapsedHeight,
                onExpandedHeightChange: updateGpxExpandedHeight
            )
            .sheet(item: $gpxShareItem) { item in
                ShareSheetView(url: item.url)
            }
            .presentationDetents(
                isPad
                    ? [gpxExpandedDetent]
                    : [gpxCollapsedDetent, gpxExpandedDetent],
                selection: $gpxDetent
            )
            .presentationDragIndicator(isPad ? .hidden : .visible)
            .presentationBackgroundInteraction(.enabled)
            .presentationContentInteraction(.resizes)
            .onChange(of: gpxDetails.fileUri, initial: true) {
                gpxDetent = isPad
                    ? gpxExpandedDetent
                    : gpxCollapsedDetent
            }
        } else {
            EmptyView()
        }
    }

    @ViewBuilder
    func placeDetailsSheet(uiState: MainUiState) -> some View {
        if let placeDetails = uiState.mapUiState.placeDetails {
            PlaceDetailsSheetView(
                strings: strings,
                placeDetails: placeDetails,
                routePlanStopCount: uiState.mapUiState.routeStops.count,
                onRoutePlanClick: {
                    viewModel.onEvent(event: MainUiEventsPlaceDetailsRoutePlanClicked())
                },
                onMapsNavigationClick: {
                    viewModel.onEvent(event: MainUiEventsPlaceDetailsMapsNavigationClicked())
                },
                onDismissRequest: {
                    viewModel.onEvent(event: MainUiEventsPlaceDetailsCloseClicked())
                },
                onHeightChange: { height in
                    placeDetailsHeight = height
                }
            )
            .presentationDetents([.height(placeDetailsHeight)])
            .presentationDragIndicator(.visible)
            .presentationBackgroundInteraction(.enabled)
        } else {
            EmptyView()
        }
    }
}
