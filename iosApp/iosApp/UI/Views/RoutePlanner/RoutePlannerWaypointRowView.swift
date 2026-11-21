import Shared
import SwiftUI

struct RoutePlannerWaypointRowView: View {
    let strings: Strings
    let waypoint: RoutePlannerWaypoint
    let waypointType: WaypointType
    let hasConnectorAbove: Bool
    var hasConnectorBelow = true
    var rowHeight: CGFloat = 44
    var onClicked: () -> Void = {}
    var onRemoveClicked: () -> Void = {}

    var body: some View {
        HStack(spacing: 14) {
            RoutePlannerWaypointIconColumn(hasConnectorAbove: hasConnectorAbove, hasConnectorBelow: hasConnectorBelow) {
                RoutePlannerWaypointTypeIcon(type: waypointType)
            }
            Text(title)
                .font(.system(size: 15))
                .foregroundStyle(.primary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                .contentShape(.rect)
                .onTapGesture(perform: onClicked)
                .accessibilityAddTraits(.isButton)
            removeButton
        }
        .frame(maxWidth: .infinity, minHeight: rowHeight, alignment: .leading)
    }

    private var removeButton: some View {
        Button(action: onRemoveClicked) {
            Image(systemName: "xmark")
                .font(.system(size: 15, weight: .regular))
                .foregroundStyle(Color(.secondaryLabel))
                .frame(width: 44, height: rowHeight)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_a11y_delete_stop))
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_DELETE_STOP_BUTTON)
    }

    private var title: String {
        guard let name = waypoint.displayName else {
            return strings.get(id: SharedRes.strings().route_planner_waypoint_empty)
        }
        return strings.get(desc: name)
    }
}

struct RoutePlannerWaypointTypeIcon: View {
    let type: WaypointType

    var body: some View {
        Image(uiImage: type.icon.toUIImage()!)
            .resizable()
            .scaledToFit()
            .frame(width: 20, height: 20)
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
                location: nil,
                customName: nil,
                comment: nil
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
                location: nil,
                customName: nil,
                comment: nil
            ),
            waypointType: .intermediate,
            hasConnectorAbove: true
        )
        RoutePlannerWaypointRowView(
            strings: Strings(),
            waypoint: RoutePlannerWaypoint(
                id: "3",
                name: RawStringDesc(string: "Prédikálószék"),
                placeName: nil,
                location: nil,
                customName: nil,
                comment: nil
            ),
            waypointType: .end,
            hasConnectorAbove: true,
            hasConnectorBelow: false
        )
    }
}
