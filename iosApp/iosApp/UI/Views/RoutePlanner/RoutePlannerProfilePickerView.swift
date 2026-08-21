import Shared
import SwiftUI

struct RoutePlannerProfilePickerView: View {
    let strings: Strings
    let selectedProfile: RoutePlannerProfile
    let onProfileSelected: (RoutePlannerProfile) -> Void

    private var selection: Binding<RoutePlannerProfile> {
        Binding(
            get: { selectedProfile },
            set: { onProfileSelected($0) }
        )
    }

    var body: some View {
        Picker(strings.get(id: SharedRes.strings().route_planner_a11y_profiles), selection: selection) {
            ForEach(RoutePlannerProfile.allCases, id: \.self) { profile in
                Text(strings.get(id: profile.title))
                    .tag(profile)
            }
        }
        .pickerStyle(.segmented)
        .labelsHidden()
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_PROFILE_PICKER)
    }
}

#Preview {
    VStack(spacing: 24) {
        RoutePlannerProfilePickerView(strings: Strings(), selectedProfile: .onTrails, onProfileSelected: { _ in })
        RoutePlannerProfilePickerView(strings: Strings(), selectedProfile: .shortestRoute, onProfileSelected: { _ in })
        RoutePlannerProfilePickerView(strings: Strings(), selectedProfile: .bike, onProfileSelected: { _ in })
    }
    .padding()
}
