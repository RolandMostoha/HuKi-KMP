import Shared
import SwiftUI

struct RoutePlannerStopActionsView: View {
    let strings: Strings
    let isAddStopEnabled: Bool
    let isRoundTripEnabled: Bool
    let onAddStopClicked: () -> Void
    let onRoundTripClicked: () -> Void

    static let height: CGFloat = 44

    var body: some View {
        HStack(spacing: 12) {
            RoutePlannerStopActionButton(
                systemImage: "plus",
                title: strings.get(id: SharedRes.strings().route_planner_add_stop_title),
                isEnabled: isAddStopEnabled,
                action: onAddStopClicked
            )
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_ADD_STOP_ROW)
            RoutePlannerStopActionButton(
                systemImage: "arrow.trianglehead.counterclockwise",
                title: strings.get(id: SharedRes.strings().route_planner_round_trip),
                isEnabled: isRoundTripEnabled,
                action: onRoundTripClicked
            )
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_ROUND_TRIP_BUTTON)
        }
    }
}

private struct RoutePlannerStopActionButton: View {
    let systemImage: String
    let title: String
    let isEnabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Label(title, systemImage: systemImage)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .frame(maxWidth: .infinity, minHeight: RoutePlannerStopActionsView.height)
                .background(Color(.tertiarySystemFill), in: .capsule)
                .contentShape(.capsule)
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
        .opacity(isEnabled ? 1 : 0.4)
        .animation(.toggle, value: isEnabled)
    }
}

#Preview {
    VStack(spacing: 16) {
        RoutePlannerStopActionsView(
            strings: Strings(),
            isAddStopEnabled: true,
            isRoundTripEnabled: true,
            onAddStopClicked: {},
            onRoundTripClicked: {}
        )
        RoutePlannerStopActionsView(
            strings: Strings(),
            isAddStopEnabled: false,
            isRoundTripEnabled: false,
            onAddStopClicked: {},
            onRoundTripClicked: {}
        )
    }
    .padding()
}
