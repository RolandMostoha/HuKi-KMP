import Shared
import SwiftUI

struct RoutePlannerGraphhopperAttributionView: View {
    let strings: Strings
    let action: () -> Void

    var body: some View {
        PoweredByAttributionView(
            strings: strings,
            logo: SharedRes.images().img_graphhopper_logo.toUIImage()!,
            logoHeight: 10,
            accessibilityLabel: strings.get(id: SharedRes.strings().route_planner_a11y_open_graphhopper),
            font: .system(size: 10),
            tintsLogo: true,
            action: action
        )
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_GRAPHHOPPER_ATTRIBUTION)
    }
}

#Preview {
    RoutePlannerGraphhopperAttributionView(strings: Strings(), action: {})
        .padding()
}
