import Shared
import SwiftUI

struct RoundTripWizardStartCardView: View {
    let strings: Strings
    let start: RoutePlannerWaypoint
    let isExpanded: Bool
    let onEvent: (RoutePlannerUiEvents) -> Void

    private let primaryColor = Color(SharedRes.colors().primary.getUIColor())
    private static let rowHeight: CGFloat = 44
    private static let cornerRadius: CGFloat = 18

    var body: some View {
        VStack(spacing: RoutePlannerStopCard.actionsSpacing) {
            titleRow
            if isExpanded {
                HStack(spacing: 10) {
                    ForEach(actions) { action in
                        RoutePlannerStopCardActionButton(action: action)
                    }
                }
                .padding(.bottom, RoutePlannerStopCard.bottomPadding)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.horizontal, 12)
        .background {
            if isExpanded {
                RoundedRectangle(cornerRadius: Self.cornerRadius, style: .continuous)
                    .fill(primaryColor.opacity(0.08))
                    .strokeBorder(primaryColor.opacity(0.35), lineWidth: 1)
            }
        }
        .animation(.stateChange, value: isExpanded)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.ROUND_TRIP_WIZARD_START_ROW)
    }

    private var titleRow: some View {
        HStack(spacing: 14) {
            RoutePlannerWaypointTypeIcon(type: .roundTrip)
                .frame(width: 20)
            Text(title)
                .font(.system(size: 17, weight: isExpanded ? .semibold : .regular))
                .foregroundStyle(isExpanded ? primaryColor : Color(.label))
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                .contentShape(.rect)
                .onTapGesture(perform: toggle)
            Button(action: toggle) {
                Image(systemName: "ellipsis")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(Color(.secondaryLabel))
                    .frame(width: Self.rowHeight, height: Self.rowHeight)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(strings.get(id: SharedRes.strings().round_trip_wizard_a11y_start_actions))
            .accessibilityIdentifier(TestTags.shared.ROUND_TRIP_WIZARD_START_MORE_BUTTON)
        }
        .frame(height: Self.rowHeight)
    }

    private var title: String {
        start.displayName.map { strings.get(desc: $0) }
            ?? strings.get(id: SharedRes.strings().route_planner_my_location)
    }

    private func toggle() {
        onEvent(RoutePlannerUiEventsStopClicked(waypointId: start.id))
    }

    // Without a location there is nothing to move yet, so the start is picked on the map instead
    private var actions: [RoutePlannerStopCardAction] {
        let firstAction = start.location == nil
            ? RoutePlannerStopCardAction(
                id: TestTags.shared.routePlannerEmptyStopPickOnMap(index: 0),
                systemImage: "scope",
                title: strings.get(id: SharedRes.strings().route_planner_empty_stop_pick_on_map),
                action: { onEvent(RoutePlannerUiEventsPickOnMapClicked(waypointId: start.id)) }
            )
            : RoutePlannerStopCardAction(
                id: TestTags.shared.ROUTE_PLANNER_STOP_MOVE,
                systemImage: "arrow.up.and.down.and.arrow.left.and.right",
                title: strings.get(id: SharedRes.strings().route_planner_stop_move),
                action: { onEvent(RoutePlannerUiEventsStopMoveClicked(waypointId: start.id)) }
            )
        return [
            firstAction,
            RoutePlannerStopCardAction(
                id: TestTags.shared.ROUTE_PLANNER_STOP_REPLACE,
                systemImage: "magnifyingglass",
                title: strings.get(id: SharedRes.strings().route_planner_stop_replace),
                action: { onEvent(RoutePlannerUiEventsStopReplaceClicked(waypointId: start.id)) }
            ),
            RoutePlannerStopCardAction(
                id: TestTags.shared.ROUTE_PLANNER_STOP_RENAME,
                systemImage: "bubble.and.pencil",
                title: strings.get(id: SharedRes.strings().route_planner_stop_rename),
                action: { onEvent(RoutePlannerUiEventsStopRenameClicked(waypointId: start.id)) }
            )
        ]
    }
}
