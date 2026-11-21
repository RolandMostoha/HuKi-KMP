import Shared
import SwiftUI

private func previewStop(_ id: String, name: String? = nil) -> RoutePlannerWaypoint {
    RoutePlannerWaypoint(
        id: id,
        name: name.map { RawStringDesc(string: $0) },
        placeName: nil,
        location: nil,
        customName: nil,
        comment: nil
    )
}

private let previewWaypoints = [
    previewStop("1", name: "My location"),
    previewStop("2", name: "Ram-hegy"),
    previewStop("3", name: "Prédikálószék"),
    previewStop("4")
]

private let previewRoundTripLocation = Location(latitude: 47.72, longitude: 18.89, altitude: nil)

private let previewRoundTripWaypoints = [
    RoutePlannerWaypoint(
        id: "1",
        name: RawStringDesc(string: "My location"),
        placeName: nil,
        location: previewRoundTripLocation,
        customName: nil,
        comment: nil
    ),
    previewStop("2", name: "Ram-hegy"),
    RoutePlannerWaypoint(
        id: "3",
        name: RawStringDesc(string: "My location"),
        placeName: nil,
        location: previewRoundTripLocation,
        customName: nil,
        comment: nil
    ),
    previewStop("4")
]

private let previewManyWaypoints = [
    previewStop("1", name: "My location"),
    previewStop("2", name: "Ram-hegy"),
    previewStop("3", name: "Prédikálószék"),
    previewStop("4", name: "Vadálló-kövek"),
    previewStop("5", name: "Dobogókő"),
    previewStop("6", name: "Rám-szakadék"),
    previewStop("7")
]

private let previewMaxStopCount = Int(RoutePlanConstants.shared.MAX_WAYPOINT_COUNT)

private let previewMaxWaypoints: [RoutePlannerWaypoint] = (1...previewMaxStopCount).map { index in
    previewStop("\(index)", name: "Stop \(index)")
} + [previewStop("spare")]

private let previewEmptyWaypoints = [
    previewStop("1", name: "My location"),
    previewStop("2"),
    previewStop("3")
]

private func previewUiState(
    profile: RoutePlannerProfile = .onTrails,
    waypoints: [RoutePlannerWaypoint],
    isLoading: Bool = false,
    error: InfoViewData? = nil,
    selectedStopId: String? = nil
) -> RoutePlannerUiState {
    RoutePlannerUiState(
        routeProfile: profile,
        waypoints: waypoints,
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
        mode: .planner,
        roundTrip: nil
    )
}

#Preview("Empty route") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewEmptyWaypoints),
        onEvent: { _ in }
    )
}

#Preview("Planning route") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(profile: .shortestRoute, waypoints: previewWaypoints, isLoading: true),
        onEvent: { _ in }
    )
}

#Preview("Round trip") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewRoundTripWaypoints),
        onEvent: { _ in }
    )
}

#Preview("Many stops") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewManyWaypoints, isLoading: true),
        onEvent: { _ in },
        maxExpandedHeight: 520
    )
}

#Preview("Minimized") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewWaypoints, isLoading: true),
        onEvent: { _ in },
        detent: .minimized
    )
}

#Preview("Minimized selected stop") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewWaypoints, selectedStopId: previewWaypoints.last?.id),
        onEvent: { _ in },
        detent: .minimized
    )
}

#Preview("Max stops") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewMaxWaypoints),
        onEvent: { _ in },
        maxExpandedHeight: 700
    )
}

#Preview("Failed route") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewWaypoints, error: NetworkError.noInternet.toInfoViewData()),
        onEvent: { _ in }
    )
}
