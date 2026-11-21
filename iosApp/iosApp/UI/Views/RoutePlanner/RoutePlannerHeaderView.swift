import Shared
import SwiftUI

struct RoutePlannerHeaderView: View {
    let strings: Strings
    let title: String
    let expandAccessibilityLabel: String
    let minimizeAccessibilityLabel: String
    let detent: RoutePlannerDetent
    let onDetentChange: (RoutePlannerDetent) -> Void
    let onAttributionClicked: () -> Void
    let onCloseClicked: () -> Void

    private static let iconSize: CGFloat = 24

    var body: some View {
        HStack(spacing: 8) {
            GlassIconButton(
                systemName: detent == .minimized
                    ? "arrow.up.left.and.arrow.down.right"
                    : "arrow.down.right.and.arrow.up.left",
                accessibilityLabel: detent == .minimized ? expandAccessibilityLabel : minimizeAccessibilityLabel,
                accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_DETENT_BUTTON,
                iconSize: Self.iconSize,
                action: { onDetentChange(detent.toggled) }
            )
            VStack(spacing: 4) {
                Text(title)
                    .font(.system(size: 24, weight: .bold))
                    .lineLimit(1)
                    .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_TITLE)
                RoutePlannerGraphhopperAttributionView(strings: strings, action: onAttributionClicked)
            }
            .frame(maxWidth: .infinity)
            CloseButton(
                action: onCloseClicked,
                accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_CLOSE_BUTTON,
                iconSize: Self.iconSize
            )
        }
        .padding(.horizontal, 16)
    }
}

#Preview {
    RoutePlannerHeaderView(
        strings: Strings(),
        title: "Route planner",
        expandAccessibilityLabel: "Expand",
        minimizeAccessibilityLabel: "Minimize",
        detent: .expanded,
        onDetentChange: { _ in },
        onAttributionClicked: {},
        onCloseClicked: {}
    )
}
