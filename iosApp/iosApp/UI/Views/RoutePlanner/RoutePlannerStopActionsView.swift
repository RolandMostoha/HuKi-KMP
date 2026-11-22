import Shared
import SwiftUI

struct RoutePlannerStopActionsView: View {
    let strings: Strings
    let isAddStopEnabled: Bool
    let isRoundTrip: Bool
    let isRoundTripEnabled: Bool
    let onAddStopClicked: () -> Void
    let onRoundTripClicked: () -> Void

    /// Fits the full-size system switch with the same inset all around, the add stop button matches it
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
            RoutePlannerRoundTripToggle(
                title: strings.get(id: SharedRes.strings().route_planner_round_trip),
                isOn: isRoundTrip,
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

private struct RoutePlannerRoundTripToggle: View {
    let title: String
    let isOn: Bool
    let isEnabled: Bool
    let action: () -> Void

    // The system switch's fixed height, centered in the capsule so its end inset matches the top and bottom ones
    private static let switchHeight: CGFloat = 31

    var body: some View {
        HStack(spacing: 8) {
            Text(title)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .frame(maxWidth: .infinity, alignment: .leading)
                // The switch below carries the same title for VoiceOver
                .accessibilityHidden(true)
            // The state lives in the ViewModel, so the switch reports a flip instead of owning the value. Touches go to
            // the capsule only, so a tap on the knob flips it once
            Toggle(title, isOn: Binding(get: { isOn }, set: { _ in action() }))
                .labelsHidden()
                .toggleStyle(.switch)
                .tint(Color(SharedRes.colors().primary.getUIColor()))
                .allowsHitTesting(false)
        }
        .padding(.leading, 16)
        .padding(.trailing, (RoutePlannerStopActionsView.height - Self.switchHeight) / 2)
        .frame(maxWidth: .infinity, minHeight: RoutePlannerStopActionsView.height)
        .accessibilityElement(children: .combine)
        .background(Color(.tertiarySystemFill), in: .capsule)
        .contentShape(.capsule)
        .onTapGesture { if isEnabled { action() } }
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
            isRoundTrip: false,
            isRoundTripEnabled: true,
            onAddStopClicked: {},
            onRoundTripClicked: {}
        )
        RoutePlannerStopActionsView(
            strings: Strings(),
            isAddStopEnabled: false,
            isRoundTrip: true,
            isRoundTripEnabled: true,
            onAddStopClicked: {},
            onRoundTripClicked: {}
        )
    }
    .padding()
}
