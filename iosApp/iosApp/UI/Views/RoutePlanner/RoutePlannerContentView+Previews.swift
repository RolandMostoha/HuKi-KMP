import Shared
import SwiftUI

private let previewWaypoints = [
    RoutePlannerWaypoint(id: "1", name: RawStringDesc(string: "My location"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "2", name: RawStringDesc(string: "Ram-hegy"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "3", name: RawStringDesc(string: "Prédikálószék"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "4", name: nil, placeName: nil, location: nil)
]

private let previewRoundTripLocation = Location(latitude: 47.72, longitude: 18.89, altitude: nil)

private let previewRoundTripWaypoints = [
    RoutePlannerWaypoint(
        id: "1",
        name: RawStringDesc(string: "My location"),
        placeName: nil,
        location: previewRoundTripLocation
    ),
    RoutePlannerWaypoint(id: "2", name: RawStringDesc(string: "Ram-hegy"), placeName: nil, location: nil),
    RoutePlannerWaypoint(
        id: "3",
        name: RawStringDesc(string: "My location"),
        placeName: nil,
        location: previewRoundTripLocation
    ),
    RoutePlannerWaypoint(id: "4", name: nil, placeName: nil, location: nil)
]

private let previewManyWaypoints = [
    RoutePlannerWaypoint(id: "1", name: RawStringDesc(string: "My location"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "2", name: RawStringDesc(string: "Ram-hegy"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "3", name: RawStringDesc(string: "Prédikálószék"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "4", name: RawStringDesc(string: "Vadálló-kövek"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "5", name: RawStringDesc(string: "Dobogókő"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "6", name: RawStringDesc(string: "Rám-szakadék"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "7", name: nil, placeName: nil, location: nil)
]

private let previewMaxStopCount = Int(RoutePlanConstants.shared.MAX_WAYPOINT_COUNT)

private let previewMaxWaypoints: [RoutePlannerWaypoint] = (1...previewMaxStopCount).map { index in
    RoutePlannerWaypoint(id: "\(index)", name: RawStringDesc(string: "Stop \(index)"), placeName: nil, location: nil)
} + [RoutePlannerWaypoint(id: "spare", name: nil, placeName: nil, location: nil)]

private let previewEmptyWaypoints = [
    RoutePlannerWaypoint(id: "1", name: RawStringDesc(string: "My location"), placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "2", name: nil, placeName: nil, location: nil),
    RoutePlannerWaypoint(id: "3", name: nil, placeName: nil, location: nil)
]

private func previewUiState(
    profile: RoutePlannerProfile = .onTrails,
    waypoints: [RoutePlannerWaypoint],
    isLoading: Bool = false,
    error: InfoViewData? = nil
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
        myLocation: nil
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

#Preview("Collapsed stops") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewManyWaypoints, isLoading: true),
        onEvent: { _ in }
    )
}

#Preview("Full screen stops") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewManyWaypoints, isLoading: true),
        onEvent: { _ in },
        detent: .fullScreen
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

#Preview("Max stops") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewMaxWaypoints),
        onEvent: { _ in },
        detent: .fullScreen
    )
}

#Preview("Failed route") {
    RoutePlannerContentView(
        strings: Strings(),
        uiState: previewUiState(waypoints: previewWaypoints, error: NetworkError.noInternet.toInfoViewData()),
        onEvent: { _ in }
    )
}
