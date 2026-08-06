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
                viewModel.onEvent(event: MainUiEventsGpxFileReopened(uri: uri))
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
        .presentationDetents([layersDetent])
        .presentationDragIndicator(.hidden)
    }

    private var layersDetent: PresentationDetent {
        if isPad {
            return .height(400)
        }
        return isLandscape ? .large : .height(360)
    }

    private func gpxExpandedDetent(_ gpxDetails: GpxDetails) -> PresentationDetent {
        let hasEnd = gpxDetails.waypoints.contains { $0.type == .end }
        return .height(Dimens.gpxDetailsExpandedHeight(hasEnd: hasEnd, isPad: isPad))
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
                isPad
                    ? [gpxExpandedDetent(gpxDetails)]
                    : [.height(Dimens.gpxDetailsCollapsedDetentHeight), gpxExpandedDetent(gpxDetails)],
                selection: $gpxDetent
            )
            .presentationDragIndicator(isPad ? .hidden : .visible)
            .presentationBackgroundInteraction(.enabled)
            .presentationContentInteraction(.resizes)
            .onChange(of: gpxDetails.fileUri, initial: true) {
                gpxDetent = isPad
                    ? gpxExpandedDetent(gpxDetails)
                    : .height(Dimens.gpxDetailsCollapsedDetentHeight)
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
