import Shared

extension MapView {
    func dismissInfoWindows() {
        if !uiState.mapUiState.distanceInfoWindows.isEmpty {
            onDistanceInfoWindowDismissed()
        }
        if uiState.mapUiState.okt?.infoWindow != nil {
            onOktInfoWindowDismissed()
        }
        if uiState.mapUiState.landscapes?.selectedDestinationId != nil {
            onLandscapesEvent(LandscapesUiEventsLandscapesInfoWindowDismissed.shared)
        }
    }
}
