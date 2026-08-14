import SwiftUI
import UIKit

struct RoutePlannerWaypointIconColumn<Icon: View>: View {
    let hasConnectorAbove: Bool
    let hasConnectorBelow: Bool
    @ViewBuilder let icon: () -> Icon

    private let columnWidth: CGFloat = 20
    private let iconPadding: CGFloat = 2
    private let connectorWidth: CGFloat = 2

    var body: some View {
        VStack(spacing: iconPadding) {
            connector
                .opacity(hasConnectorAbove ? 1 : 0)
            icon()
            connector
                .opacity(hasConnectorBelow ? 1 : 0)
        }
        .frame(width: columnWidth)
    }

    private var connector: some View {
        Capsule()
            .fill(Color(.tertiaryLabel))
            .frame(width: connectorWidth)
            .frame(maxHeight: .infinity)
    }
}

#Preview {
    VStack(spacing: 0) {
        RoutePlannerWaypointIconColumn(hasConnectorAbove: false, hasConnectorBelow: true) {
            Image(systemName: "location.fill")
                .frame(width: 20, height: 20)
        }
        .frame(height: 44)
        RoutePlannerWaypointIconColumn(hasConnectorAbove: true, hasConnectorBelow: true) {
            Image(systemName: "flag.fill")
                .frame(width: 20, height: 20)
        }
        .frame(height: 44)
        RoutePlannerWaypointIconColumn(hasConnectorAbove: true, hasConnectorBelow: false) {
            Image(systemName: "plus.circle.fill")
                .frame(width: 20, height: 20)
        }
        .frame(height: 44)
    }
    .padding()
}
