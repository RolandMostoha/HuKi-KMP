import Shared
import SwiftUI

extension RoutePlannerContentView {
    func minimizedSelectedStop(_ waypoint: RoutePlannerWaypoint) -> some View {
        let index = uiState.stops.firstIndex { $0.id == waypoint.id } ?? 0
        return selectedStopCard(
            waypoint,
            type: uiState.stopType(index: Int32(index)),
            hasConnectorAbove: false,
            hasConnectorBelow: false,
            rowHeight: Self.minimizedRowHeight,
            isStandalone: true
        )
        .padding(.horizontal, 16)
    }

    func selectedStopCard(
        _ waypoint: RoutePlannerWaypoint,
        type: WaypointType,
        hasConnectorAbove: Bool,
        hasConnectorBelow: Bool,
        rowHeight: CGFloat,
        isStandalone: Bool = false
    ) -> some View {
        RoutePlannerStopCardRowView(
            strings: strings,
            title: waypoint.displayName.map { strings.get(desc: $0) } ?? "",
            hasConnectorAbove: hasConnectorAbove,
            hasConnectorBelow: hasConnectorBelow,
            isRemovable: true,
            rowHeight: rowHeight,
            actions: [
                RoutePlannerStopCardAction(
                    id: TestTags.shared.ROUTE_PLANNER_STOP_MOVE,
                    systemImage: "arrow.up.and.down.and.arrow.left.and.right",
                    title: strings.get(id: SharedRes.strings().route_planner_stop_move),
                    action: { onEvent(RoutePlannerUiEventsStopMoveClicked(waypointId: waypoint.id)) }
                ),
                RoutePlannerStopCardAction(
                    id: TestTags.shared.ROUTE_PLANNER_STOP_REPLACE,
                    systemImage: "magnifyingglass",
                    title: strings.get(id: SharedRes.strings().route_planner_stop_replace),
                    action: { onEvent(RoutePlannerUiEventsStopReplaceClicked(waypointId: waypoint.id)) }
                ),
                RoutePlannerStopCardAction(
                    id: TestTags.shared.ROUTE_PLANNER_STOP_RENAME,
                    systemImage: "bubble.and.pencil",
                    title: strings.get(id: SharedRes.strings().route_planner_stop_rename),
                    action: { onEvent(RoutePlannerUiEventsStopRenameClicked(waypointId: waypoint.id)) }
                )
            ],
            onTitleClicked: { onEvent(RoutePlannerUiEventsStopClicked(waypointId: waypoint.id)) },
            onRemoveClicked: { onEvent(RoutePlannerUiEventsWaypointRemoved(id: waypoint.id)) },
            isStandalone: isStandalone,
            icon: { RoutePlannerWaypointTypeIcon(type: type) }
        )
    }
}
