import Shared
import SwiftUI

struct RoutePlannerWaypointRowView: View {
    let strings: Strings
    let waypoint: RoutePlannerWaypoint
    let waypointType: WaypointType
    let hasConnectorAbove: Bool
    var rowHeight: CGFloat = 44
    var onRemoveClicked: () -> Void = {}
    var onEmptyRowClicked: () -> Void = {}
    var emptyRowIdentifier: String = TestTags.shared.ROUTE_PLANNER_EMPTY_STOP_ROW

    var body: some View {
        HStack(spacing: 14) {
            RoutePlannerWaypointIconColumn(hasConnectorAbove: hasConnectorAbove, hasConnectorBelow: true) {
                icon
            }
            if isEmpty {
                emptyTitleButton
            } else {
                Text(title)
                    .font(.system(size: 15))
                    .foregroundStyle(.primary)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
                removeButton
                    .transition(.opacity)
            }
        }
        .frame(maxWidth: .infinity, minHeight: rowHeight, alignment: .leading)
        .animation(.smooth(duration: 0.2), value: isEmpty)
    }

    private var emptyTitleButton: some View {
        Button(action: onEmptyRowClicked) {
            Text(title)
                .font(.system(size: 15))
                .foregroundStyle(Color(.secondaryLabel))
                .lineLimit(1)
                .frame(maxWidth: .infinity, minHeight: rowHeight, alignment: .leading)
                .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_a11y_add_stop))
        .accessibilityIdentifier(emptyRowIdentifier)
    }

    private var removeButton: some View {
        Button(action: onRemoveClicked) {
            Image(systemName: "xmark")
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(Color(.secondaryLabel))
                .frame(width: 32, height: rowHeight)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_a11y_delete_stop))
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_DELETE_STOP_BUTTON)
    }

    private var icon: some View {
        Image(uiImage: waypointType.icon.toUIImage()!)
            .resizable()
            .scaledToFit()
            .frame(width: 20, height: 20)
    }

    private var isEmpty: Bool {
        waypoint.isEmpty
    }

    private var title: String {
        guard let name = waypoint.name else {
            return strings.get(id: SharedRes.strings().route_planner_waypoint_empty)
        }
        return strings.get(desc: name)
    }
}

#Preview {
    List {
        RoutePlannerWaypointRowView(
            strings: Strings(),
            waypoint: RoutePlannerWaypoint(
                id: "1",
                name: RawStringDesc(string: "My location"),
                placeName: nil,
                location: nil
            ),
            waypointType: .start,
            hasConnectorAbove: false
        )
        RoutePlannerWaypointRowView(
            strings: Strings(),
            waypoint: RoutePlannerWaypoint(
                id: "2",
                name: RawStringDesc(string: "Ram-hegy"),
                placeName: nil,
                location: nil
            ),
            waypointType: .intermediate,
            hasConnectorAbove: true
        )
        RoutePlannerWaypointRowView(
            strings: Strings(),
            waypoint: RoutePlannerWaypoint(id: "3", name: nil, placeName: nil, location: nil),
            waypointType: .end,
            hasConnectorAbove: true
        )
    }
}
