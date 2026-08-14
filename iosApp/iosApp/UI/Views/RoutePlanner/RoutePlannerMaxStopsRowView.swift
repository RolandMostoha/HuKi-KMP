import Shared
import SwiftUI

struct RoutePlannerMaxStopsRowView: View {
    let strings: Strings
    let maxStopCount: Int
    var rowHeight: CGFloat = 44

    private let warningColor = Color(SharedRes.colors().warning.getUIColor())

    var body: some View {
        HStack(spacing: 14) {
            RoutePlannerWaypointIconColumn(hasConnectorAbove: true, hasConnectorBelow: false) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 17))
                    .foregroundStyle(warningColor)
                    .frame(width: 20, height: 20)
            }
            Text(strings.get(id: SharedRes.strings().route_planner_max_stops_reached, args: [maxStopCount]))
                .font(.system(size: 15))
                .foregroundStyle(Color(.secondaryLabel))
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .frame(minHeight: rowHeight)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_MAX_STOPS_ROW)
    }
}

#Preview {
    List {
        RoutePlannerMaxStopsRowView(strings: Strings(), maxStopCount: 10)
        RoutePlannerMaxStopsRowView(strings: Strings(), maxStopCount: 10, rowHeight: 56)
    }
}
