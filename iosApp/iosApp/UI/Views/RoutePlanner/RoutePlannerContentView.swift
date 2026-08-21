import Shared
import SwiftUI

struct RoutePlannerContentView: View {
    let strings: Strings
    let uiState: RoutePlannerUiState
    let onEvent: (RoutePlannerUiEvents) -> Void
    var detent: RoutePlannerDetent = .expanded
    var onExpandRequest: () -> Void = {}
    var onHeightsChange: (RoutePlannerSheetHeights) -> Void = { _ in }

    @State private var minimizedHeight = Dimens.routePlannerMinimizedDetentHeight
    @State private var profilePickerHeight = Self.defaultProfilePickerHeight

    var body: some View {
        contentStack(showsPlanner: detent != .minimized)
            .animation(.smooth(duration: 0.3), value: uiState.isRoutePlanLoading)
            .animation(.smooth(duration: 0.3), value: uiState.routePlanError)
            .animation(.smooth(duration: 0.3), value: uiState.routeStats)
            .animation(.smooth(duration: 0.3), value: detent)
            .background(alignment: .top) { minimizedHeightReader }
            .onChange(of: heights, initial: true) { _, newHeights in
                onHeightsChange(newHeights)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_SHEET)
    }

    private func contentStack(showsPlanner: Bool) -> some View {
        VStack(spacing: Self.stackSpacing) {
            header
            if showsPlanner {
                profilePicker
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
            routeRegion(showsPlanner: showsPlanner)
            if uiState.isSaveButtonVisible {
                saveButton
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
        }
        .padding(.top, 20)
        .padding(.bottom, 8)
        .fixedSize(horizontal: false, vertical: !isFullScreen || !showsPlanner)
    }

    private func routeRegion(showsPlanner: Bool) -> some View {
        ZStack {
            VStack(spacing: Self.stackSpacing) {
                if showsPlanner && uiState.isStopListVisible {
                    waypointsList
                        .transition(.opacity.combined(with: .move(edge: .bottom)))
                }
                RoutePlannerRouteStatusView(
                    strings: strings,
                    error: uiState.routePlanError,
                    routeStats: uiState.routeStats,
                    isPlanExpected: uiState.isRoutePlanExpected,
                    onRetryClicked: { onEvent(RoutePlannerUiEventsRetryClicked.shared) }
                )
                .padding(.horizontal, 16)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
            .opacity(uiState.isRoutePlanLoading ? 0 : 1)
            .allowsHitTesting(!uiState.isRoutePlanLoading)
            .accessibilityHidden(uiState.isRoutePlanLoading)
            if uiState.isRoutePlanLoading {
                RoutePlannerLoadingView(strings: strings)
                    .transition(.opacity)
            }
        }
    }

    private var minimizedHeightReader: some View {
        contentStack(showsPlanner: false)
            .hidden()
            .accessibilityHidden(true)
            .transaction { $0.animation = nil }
            .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                minimizedHeight = height
            }
    }

    private var header: some View {
        HStack(spacing: 14) {
            Text(strings.get(id: SharedRes.strings().route_planner_title))
                .font(.system(size: 24, weight: .bold))
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_TITLE)
            CloseButton(
                action: { onEvent(RoutePlannerUiEventsCloseClicked.shared) },
                accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_CLOSE_BUTTON
            )
        }
        .padding(.leading, 24)
        .padding(.trailing, 16)
    }

    private var profilePicker: some View {
        RoutePlannerProfilePickerView(
            strings: strings,
            selectedProfile: uiState.routeProfile,
            onProfileSelected: { profile in
                onEvent(RoutePlannerUiEventsProfileSelected(routeProfile: profile))
            }
        )
        .padding(.horizontal, 16)
        .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
            profilePickerHeight = height
        }
    }

    private var waypointsList: some View {
        List {
            waypointsSection
        }
        .listStyle(.insetGrouped)
        .listSectionSpacing(0)
        .contentMargins(.vertical, 0, for: .scrollContent)
        .scrollContentBackground(.hidden)
        .environment(\.editMode, .constant(.active))
        .frame(height: isFullScreen ? nil : waypointsListHeight(for: detent))
        .frame(maxHeight: isFullScreen ? .infinity : nil)
        .padding(.bottom, -Self.waypointsListBottomInset)
        .animation(.smooth(duration: 0.3), value: uiState.waypoints)
        .animation(.smooth(duration: 0.3), value: uiState.isMaxStopsReached)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_WAYPOINT_LIST)
    }

    private var waypointsSection: some View {
        Section {
            if isStopsCollapsed {
                collapsedStopRows
            } else {
                ForEach(Array(uiState.stops.enumerated()), id: \.element.id) { index, waypoint in
                    waypointRow(waypoint, index: index, type: uiState.stopType(index: Int32(index)))
                }
                .onMove { indices, destination in
                    moveWaypoint(from: indices, to: destination)
                }
            }
            if uiState.isMaxStopsReached {
                RoutePlannerMaxStopsRowView(
                    strings: strings,
                    maxStopCount: Int(RoutePlannerUiState.companion.MAX_WAYPOINT_COUNT),
                    rowHeight: rowHeight
                )
                .moveDisabled(true)
            } else {
                RoutePlannerAddStopRowView(
                    strings: strings,
                    isRoundTripEnabled: uiState.isRoundTripEnabled,
                    rowHeight: rowHeight,
                    onAddStopClicked: { onEvent(RoutePlannerUiEventsAddStopFromSearchClicked(waypointId: nil)) },
                    onRoundTripClicked: { onEvent(RoutePlannerUiEventsRoundTripClicked.shared) }
                )
                .moveDisabled(true)
            }
        }
        .listRowInsets(EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16))
        .listRowBackground(Color(.tertiarySystemFill))
    }

    @ViewBuilder
    private var collapsedStopRows: some View {
        if let first = uiState.stops.first, let last = uiState.stops.last {
            waypointRow(first, index: 0, type: uiState.stopType(index: 0))
                .moveDisabled(true)
            RoutePlannerMoreStopsRowView(
                strings: strings,
                hiddenStopCount: uiState.stops.count - 2,
                rowHeight: rowHeight,
                onTap: onExpandRequest
            )
            .moveDisabled(true)
            waypointRow(last, index: uiState.stops.count - 1, type: .end)
                .moveDisabled(true)
        }
    }

    private func waypointRow(_ waypoint: RoutePlannerWaypoint, index: Int, type: WaypointType) -> some View {
        RoutePlannerWaypointRowView(
            strings: strings,
            waypoint: waypoint,
            waypointType: type,
            hasConnectorAbove: index > 0,
            rowHeight: rowHeight,
            onRemoveClicked: { onEvent(RoutePlannerUiEventsWaypointRemoved(id: waypoint.id)) },
            onEmptyRowClicked: { onEvent(RoutePlannerUiEventsAddStopFromSearchClicked(waypointId: waypoint.id)) },
            emptyRowIdentifier: TestTags.shared.routePlannerEmptyStopRow(index: Int32(index))
        )
    }

    private var saveButton: some View {
        PrimaryButton(
            icon: .system("square.and.arrow.down.fill"),
            title: strings.get(id: SharedRes.strings().route_planner_save),
            action: { onEvent(RoutePlannerUiEventsSaveRouteClicked.shared) }
        )
        .disabled(!uiState.isSaveEnabled)
        .opacity(uiState.isSaveEnabled ? 1 : 0.5)
        .padding(.horizontal, 24)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_SAVE_BUTTON)
    }

    // The inset grouped List needs an explicit height: the rows plus the section's own vertical insets.
    private static let waypointsListBottomInset: CGFloat = 10
    private static let waypointsListVerticalInsets: CGFloat = 36
    private static let waypointsListMaxHeight: CGFloat = 400
    private static let defaultProfilePickerHeight: CGFloat = 32
    private static let stackSpacing: CGFloat = 16
    private static let maxDefaultStopCount = 3

    private var isFullScreen: Bool {
        detent == .fullScreen
    }

    private var isStopsCollapsed: Bool {
        detent == .expanded && uiState.stops.count > Self.maxDefaultStopCount
    }

    private var rowHeight: CGFloat {
        isFullScreen ? 56 : 44
    }

    private func waypointsListHeight(for detent: RoutePlannerDetent) -> CGFloat {
        let collapsedStopCount = 3
        let stopCount = detent == .expanded && uiState.stops.count > Self.maxDefaultStopCount
            ? collapsedStopCount
            : uiState.stops.count
        let rowsHeight = CGFloat(stopCount + 1) * rowHeight
        return min(rowsHeight + Self.waypointsListVerticalInsets, Self.waypointsListMaxHeight)
    }

    private var heights: RoutePlannerSheetHeights {
        let stopListHeight = uiState.isStopListVisible
            ? Self.stackSpacing + waypointsListHeight(for: .expanded) - Self.waypointsListBottomInset
            : 0
        let plannerHeight = profilePickerHeight + stopListHeight
        return RoutePlannerSheetHeights(
            expanded: minimizedHeight + Self.stackSpacing + plannerHeight,
            minimized: minimizedHeight
        )
    }

    private func moveWaypoint(from indices: IndexSet, to destination: Int) {
        guard let fromIndex = indices.first else { return }
        let toIndex = destination > fromIndex ? destination - 1 : destination
        onEvent(RoutePlannerUiEventsWaypointMoved(fromIndex: Int32(fromIndex), toIndex: Int32(toIndex)))
    }
}

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

private let previewMaxWaypoints: [RoutePlannerWaypoint] = (1...10).map { index in
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
