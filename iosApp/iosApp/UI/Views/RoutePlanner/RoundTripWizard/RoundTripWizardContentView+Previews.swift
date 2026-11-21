import Shared
import SwiftUI

private let previewStart = RoutePlannerWaypoint(
    id: "start",
    name: RawStringDesc(string: "My location"),
    placeName: nil,
    location: Location(latitude: 47.72, longitude: 18.89, altitude: nil),
    customName: nil,
    comment: nil
)

private let previewSentinel = RoutePlannerWaypoint(
    id: "sentinel",
    name: nil,
    placeName: nil,
    location: nil,
    customName: nil,
    comment: nil
)

private func previewWizardState(
    isLoading: Bool = false,
    error: InfoViewData? = nil,
    selectedStopId: String? = nil
) -> RoutePlannerUiState {
    RoutePlannerUiState(
        routeProfile: .onTrails,
        waypoints: [previewStart, previewSentinel],
        routePlan: nil,
        isRoutePlanLoading: isLoading,
        routePlanError: error,
        isWaypointSearchVisible: false,
        waypointSearchTargetId: nil,
        isPickingOnMap: false,
        selectedStopId: selectedStopId,
        renamingStopId: nil,
        isMovingStop: false,
        myLocation: nil,
        mode: .roundTripWizard,
        roundTrip: RoundTripOptions(distanceKm: 9, seed: 7, heading: 90)
    )
}

#Preview("Expanded") {
    RoundTripWizardContentView(strings: Strings(), uiState: previewWizardState(), onEvent: { _ in })
}

#Preview("Start actions") {
    RoundTripWizardContentView(
        strings: Strings(),
        uiState: previewWizardState(selectedStopId: "start"),
        onEvent: { _ in }
    )
}

#Preview("Loading") {
    RoundTripWizardContentView(strings: Strings(), uiState: previewWizardState(isLoading: true), onEvent: { _ in })
}

#Preview("Minimized") {
    RoundTripWizardContentView(
        strings: Strings(),
        uiState: previewWizardState(),
        onEvent: { _ in },
        detent: .minimized
    )
}
