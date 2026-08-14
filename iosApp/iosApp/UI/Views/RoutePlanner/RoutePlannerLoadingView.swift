import Shared
import SwiftUI

struct RoutePlannerLoadingView: View {
    let strings: Strings
    var fontSize: CGFloat = 15

    var body: some View {
        HStack(spacing: 10) {
            ProgressView()
                .controlSize(.regular)
            Text(strings.get(id: SharedRes.strings().route_planner_loading))
                .font(.system(size: fontSize, weight: .medium))
                .foregroundStyle(Color(.secondaryLabel))
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_LOADING)
    }
}

#Preview("Default") {
    RoutePlannerLoadingView(strings: Strings())
        .padding()
}

#Preview("Large") {
    RoutePlannerLoadingView(strings: Strings(), fontSize: 20)
        .padding()
}
