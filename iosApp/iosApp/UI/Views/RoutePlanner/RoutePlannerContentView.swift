import Shared
import SwiftUI

struct RoutePlannerContentView: View {
    let strings: Strings
    let uiState: RoutePlannerUiState
    let onEvent: (RoutePlannerUiEvents) -> Void
    var detent: RoutePlannerDetent = .expanded
    var maxExpandedHeight: CGFloat = .infinity
    var onDetentChange: (RoutePlannerDetent) -> Void = { _ in }
    var onHeightsChange: (RoutePlannerSheetHeights) -> Void = { _ in }

    @State private var minimizedHeight = Dimens.routePlannerMinimizedDetentHeight
    @State private var profilePickerHeight = Self.defaultProfilePickerHeight

    var body: some View {
        contentStack(showsPlanner: detent != .minimized, showsSelectedStop: detent == .minimized)
            .animation(.stateChange, value: uiState.isRoutePlanLoading)
            .animation(.stateChange, value: uiState.routePlanError)
            .animation(.stateChange, value: uiState.routeStats)
            .animation(.stateChange, value: detent)
            // The whole stack animates the list's height, or the rows sit centered in a slot that already grew
            .animation(.stateChange, value: uiState.waypoints)
            .animation(.stateChange, value: uiState.isMaxStopsReached)
            .animation(.stateChange, value: uiState.selectedStop?.id)
            .background(alignment: .top) { minimizedHeightReader }
            .onChange(of: heights, initial: true) { _, newHeights in
                onHeightsChange(newHeights)
            }
            // Content that outgrows the sheet for a frame overflows below, or it centers and the sheet dips
            .frame(maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .top)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_SHEET)
    }

    private func contentStack(showsPlanner: Bool, showsSelectedStop: Bool) -> some View {
        VStack(spacing: Self.stackSpacing) {
            header
            if showsPlanner {
                profilePicker
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
            if showsSelectedStop, !uiState.isMovingStop, let stop = uiState.selectedStop {
                minimizedSelectedStop(stop)
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
            routeRegion(showsPlanner: showsPlanner)
            if uiState.isSaveButtonVisible {
                saveButton
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
        }
        .padding(.top, 20)
        .fixedSize(horizontal: false, vertical: true)
    }

    private func routeRegion(showsPlanner: Bool) -> some View {
        ZStack {
            VStack(spacing: Self.routeRegionSpacing) {
                if showsPlanner && uiState.isStopListVisible {
                    VStack(spacing: Self.stopActionsSpacing) {
                        waypointsList
                        stopActions
                    }
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
        contentStack(showsPlanner: false, showsSelectedStop: false)
            .hidden()
            .accessibilityHidden(true)
            .transaction { $0.animation = nil }
            .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                minimizedHeight = height
            }
    }

    private var header: some View {
        HStack(spacing: 8) {
            GlassIconButton(
                systemName: detent == .minimized
                    ? "arrow.up.left.and.arrow.down.right"
                    : "arrow.down.right.and.arrow.up.left",
                accessibilityLabel: strings.get(
                    id: detent == .minimized
                        ? SharedRes.strings().route_planner_a11y_expand
                        : SharedRes.strings().route_planner_a11y_minimize
                ),
                accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_DETENT_BUTTON,
                iconSize: Self.headerIconSize,
                action: { onDetentChange(detent.toggled) }
            )
            VStack(spacing: 4) {
                Text(strings.get(id: SharedRes.strings().route_planner_title))
                    .font(.system(size: 24, weight: .bold))
                    .lineLimit(1)
                    .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_TITLE)
                RoutePlannerGraphhopperAttributionView(
                    strings: strings,
                    action: { onEvent(RoutePlannerUiEventsGraphhopperAttributionClicked.shared) }
                )
            }
            .frame(maxWidth: .infinity)
            CloseButton(
                action: { onEvent(RoutePlannerUiEventsCloseClicked.shared) },
                accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_CLOSE_BUTTON,
                iconSize: Self.headerIconSize
            )
        }
        .padding(.horizontal, 16)
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
        ScrollViewReader { proxy in
            List {
                waypointsSection
            }
            .onChange(of: uiState.stops.map(\.id)) { oldIds, newIds in
                // A stop added below the visible rows would land out of sight
                guard let addedId = newIds.first(where: { !oldIds.contains($0) }) else { return }
                withAnimation(.stateChange) { proxy.scrollTo(addedId) }
            }
            // A stop selected on the map can sit outside the scrolled list
            .onChange(of: uiState.selectedStop?.id) { _, selectedId in
                guard let selectedId else { return }
                withAnimation(.stateChange) { proxy.scrollTo(selectedId) }
            }
        }
        .listStyle(.insetGrouped)
        .environment(\.defaultMinListRowHeight, 0)
        .listSectionSpacing(0)
        .contentMargins(.vertical, 0, for: .scrollContent)
        .scrollContentBackground(.hidden)
        .environment(\.editMode, .constant(.active))
        .scrollBounceBehavior(.basedOnSize)
        // A resized List animates its center, which drops the rows while the sheet grows,
        // so it keeps one height and an animated, clipping frame reveals the part in use
        .frame(height: maxWaypointsListHeight, alignment: .top)
        .frame(height: waypointsListHeight, alignment: .top)
        .clipped()
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_WAYPOINT_LIST)
    }

    private var waypointsSection: some View {
        Section {
            ForEach(Array(uiState.stops.enumerated()), id: \.element.id) { index, waypoint in
                waypointRow(waypoint, index: index, type: uiState.stopType(index: Int32(index)))
            }
            .onMove { indices, destination in
                moveWaypoint(from: indices, to: destination)
            }
            if uiState.isMaxStopsReached {
                RoutePlannerMaxStopsRowView(
                    strings: strings,
                    maxStopCount: Int(RoutePlanConstants.shared.MAX_WAYPOINT_COUNT),
                    rowHeight: rowHeight
                )
                .frame(height: rowHeight)
                .moveDisabled(true)
            }
        }
        .listRowInsets(EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16))
        .listRowBackground(Color(.tertiarySystemFill))
    }

    private var stopActions: some View {
        RoutePlannerStopActionsView(
            strings: strings,
            isAddStopEnabled: uiState.isAddStopEnabled,
            isRoundTrip: uiState.isRoundTrip,
            isRoundTripEnabled: uiState.isRoundTripEnabled,
            onAddStopClicked: { onEvent(RoutePlannerUiEventsAddEmptyStopClicked.shared) },
            onRoundTripClicked: { onEvent(RoutePlannerUiEventsRoundTripClicked.shared) }
        )
        .padding(.horizontal, 16)
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

    /// Below this the list keeps its height and the sheet outgrows the cap instead, as only landscape gets this tight.
    private static let minWaypointsListHeight: CGFloat = 2 * rowHeight
    private static let defaultProfilePickerHeight: CGFloat = 32
    private static let stackSpacing: CGFloat = 16
    private static let routeRegionSpacing: CGFloat = 10
    private static let stopActionsSpacing: CGFloat = 12
    private static let headerIconSize: CGFloat = 24
    private static let rowHeight: CGFloat = 44
    static let minimizedRowHeight: CGFloat = 44

    private var rowHeight: CGFloat {
        Self.rowHeight
    }

    /// The List keeps this one height, its rows pinned to the top, since a resized List animates its center
    /// and drops the rows while the sheet grows. A clipping frame reveals the part in use.
    private var maxWaypointsListHeight: CGFloat {
        let nonListHeight = minimizedHeight + Self.stackSpacing + profilePickerHeight
            + Self.stopActionsSpacing + RoutePlannerStopActionsView.height + Self.routeRegionSpacing
        let available = maxExpandedHeight.isFinite ? maxExpandedHeight - nonListHeight : stopRowsHeight
        return max(available, Self.minWaypointsListHeight)
    }

    private var waypointsListHeight: CGFloat {
        min(stopRowsHeight, maxWaypointsListHeight)
    }

    private var heights: RoutePlannerSheetHeights {
        let stopListHeight = uiState.isStopListVisible
            ? waypointsListHeight
                + Self.stopActionsSpacing + RoutePlannerStopActionsView.height + Self.routeRegionSpacing
            : 0
        let plannerHeight = profilePickerHeight + stopListHeight
        // A moved stop is placed on the map, which its card would cover
        let selectedStopHeight = uiState.selectedStop != nil && !uiState.isMovingStop
            ? Self.stackSpacing + RoutePlannerStopCard.height(rowHeight: Self.minimizedRowHeight)
            : 0
        return RoutePlannerSheetHeights(
            expanded: minimizedHeight + Self.stackSpacing + plannerHeight,
            minimized: minimizedHeight + selectedStopHeight
        )
    }
}

private extension RoutePlannerContentView {
    @ViewBuilder
    func waypointRow(_ waypoint: RoutePlannerWaypoint, index: Int, type: WaypointType) -> some View {
        if waypoint.isEmpty && uiState.isStopExpanded(stop: waypoint) {
            emptyStopCard(waypoint, index: index)
        } else if waypoint.isEmpty {
            RoutePlannerCollapsedEmptyStopRowView(
                strings: strings,
                hasConnectorAbove: index > 0,
                hasConnectorBelow: uiState.hasConnectorBelow(index: Int32(index)),
                isRemovable: uiState.isEmptyStopRemovable,
                rowHeight: rowHeight,
                onClicked: { onEvent(RoutePlannerUiEventsStopClicked(waypointId: waypoint.id)) },
                onRemoveClicked: { onEvent(RoutePlannerUiEventsWaypointRemoved(id: waypoint.id)) }
            )
            .frame(height: rowHeight)
        } else if uiState.isStopExpanded(stop: waypoint) {
            selectedStopCard(
                waypoint,
                type: type,
                hasConnectorAbove: index > 0,
                hasConnectorBelow: uiState.hasConnectorBelow(index: Int32(index)),
                rowHeight: rowHeight
            )
        } else {
            RoutePlannerWaypointRowView(
                strings: strings,
                waypoint: waypoint,
                waypointType: type,
                hasConnectorAbove: index > 0,
                hasConnectorBelow: uiState.hasConnectorBelow(index: Int32(index)),
                rowHeight: rowHeight,
                onClicked: { onEvent(RoutePlannerUiEventsStopClicked(waypointId: waypoint.id)) },
                onRemoveClicked: { onEvent(RoutePlannerUiEventsWaypointRemoved(id: waypoint.id)) }
            )
            .frame(height: rowHeight)
            .accessibilityIdentifier(TestTags.shared.routePlannerStopRow(index: Int32(index)))
        }
    }

    func emptyStopCard(_ waypoint: RoutePlannerWaypoint, index: Int) -> some View {
        RoutePlannerStopCardRowView(
            strings: strings,
            title: strings.get(id: SharedRes.strings().route_planner_add_stop_title),
            hasConnectorAbove: index > 0,
            hasConnectorBelow: uiState.hasConnectorBelow(index: Int32(index)),
            isRemovable: uiState.isEmptyStopRemovable,
            rowHeight: rowHeight,
            actions: [
                RoutePlannerStopCardAction(
                    id: TestTags.shared.routePlannerEmptyStopPickOnMap(index: Int32(index)),
                    systemImage: "scope",
                    title: strings.get(id: SharedRes.strings().route_planner_empty_stop_pick_on_map),
                    action: { onEvent(RoutePlannerUiEventsPickOnMapClicked(waypointId: waypoint.id)) }
                ),
                RoutePlannerStopCardAction(
                    id: TestTags.shared.routePlannerEmptyStopRow(index: Int32(index)),
                    systemImage: "magnifyingglass",
                    title: strings.get(id: SharedRes.strings().route_planner_empty_stop_search),
                    action: { onEvent(RoutePlannerUiEventsAddStopFromSearchClicked(waypointId: waypoint.id)) }
                )
            ],
            onRemoveClicked: { onEvent(RoutePlannerUiEventsWaypointRemoved(id: waypoint.id)) },
            icon: { RoutePlannerEmptyStopIcon() }
        )
    }

    var stopRowsHeight: CGFloat {
        let stopsHeight = uiState.stops.reduce(CGFloat(0)) { height, stop in
            let isCard = uiState.isStopExpanded(stop: stop)
            return height + (isCard ? RoutePlannerStopCard.height(rowHeight: rowHeight) : rowHeight)
        }
        return stopsHeight + (uiState.isMaxStopsReached ? rowHeight : 0)
    }

    func moveWaypoint(from indices: IndexSet, to destination: Int) {
        guard let fromIndex = indices.first else { return }
        let toIndex = destination > fromIndex ? destination - 1 : destination
        onEvent(RoutePlannerUiEventsWaypointMoved(fromIndex: Int32(fromIndex), toIndex: Int32(toIndex)))
    }
}
