import Shared
import SwiftUI

struct RoutePlannerMoreStopsRowView: View {
    let strings: Strings
    let hiddenStopCount: Int
    var rowHeight: CGFloat = 44
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                RoutePlannerWaypointIconColumn(hasConnectorAbove: true, hasConnectorBelow: true) {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(Color(.secondaryLabel))
                        .frame(width: 20, height: 20)
                }
                Text(strings.get(id: SharedRes.strings().route_planner_more_stops, args: [hiddenStopCount]))
                    .font(.system(size: 15))
                    .foregroundStyle(Color(.secondaryLabel))
                    .lineLimit(1)
                Image(systemName: "chevron.down")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(Color(.tertiaryLabel))
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .frame(maxWidth: .infinity, minHeight: rowHeight, alignment: .leading)
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_MORE_STOPS_ROW)
    }
}

#Preview {
    List {
        RoutePlannerMoreStopsRowView(strings: Strings(), hiddenStopCount: 3, onTap: {})
        RoutePlannerMoreStopsRowView(strings: Strings(), hiddenStopCount: 12, rowHeight: 56, onTap: {})
    }
}
