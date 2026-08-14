import Shared
import SwiftUI

struct RoutePlannerAddStopRowView: View {
    let strings: Strings
    let isRoundTripEnabled: Bool
    var rowHeight: CGFloat = 44
    let onAddStopClicked: () -> Void
    let onRoundTripClicked: () -> Void

    private let primaryColor = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        HStack(spacing: 14) {
            addStopButton
            roundTripButton
        }
        .frame(minHeight: rowHeight)
    }

    private var addStopButton: some View {
        Button(action: onAddStopClicked) {
            HStack(spacing: 14) {
                RoutePlannerWaypointIconColumn(hasConnectorAbove: true, hasConnectorBelow: false) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 20))
                        .foregroundStyle(primaryColor)
                        .frame(width: 20, height: 20)
                }
                Text(strings.get(id: SharedRes.strings().route_planner_add_waypoint))
                    .font(.system(size: 15))
                    .foregroundStyle(primaryColor)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_a11y_add_stop))
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_ADD_STOP_ROW)
    }

    private var roundTripButton: some View {
        Button(action: onRoundTripClicked) {
            Image(systemName: "arrow.trianglehead.counterclockwise")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(primaryColor)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
        }
        .buttonStyle(.plain)
        .contentShape(.capsule)
        .disabled(!isRoundTripEnabled)
        .opacity(isRoundTripEnabled ? 1 : 0.35)
        .animation(.smooth(duration: 0.2), value: isRoundTripEnabled)
        .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_a11y_round_trip))
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_ROUND_TRIP_BUTTON)
    }
}

#Preview {
    List {
        RoutePlannerAddStopRowView(
            strings: Strings(),
            isRoundTripEnabled: true,
            onAddStopClicked: {},
            onRoundTripClicked: {}
        )
        RoutePlannerAddStopRowView(
            strings: Strings(),
            isRoundTripEnabled: false,
            onAddStopClicked: {},
            onRoundTripClicked: {}
        )
    }
}
