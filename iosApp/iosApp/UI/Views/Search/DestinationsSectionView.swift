import Shared
import SwiftUI

struct DestinationsSectionView: View {
    let strings: Strings
    let destinations: [Destination]
    let onDestinationSelected: (Destination) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            header
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 12) {
                    ForEach(Array(destinations.enumerated()), id: \.offset) { _, destination in
                        DestinationCardView(
                            strings: strings,
                            destination: destination,
                            onClick: { onDestinationSelected(destination) }
                        )
                    }
                }
                .padding(.horizontal, 16)
            }
            .scrollClipDisabled()
        }
        .padding(.top, 16)
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_SECTION)
    }

    private var header: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(strings.get(id: SharedRes.strings().destinations_section_title))
                .font(.system(size: 24, weight: .bold))
                .foregroundStyle(.primary)
            Spacer()
            // TODO Feature:DestinationsScreen - wire "See all" navigation in a later task.
            Button(action: {}, label: {
                Text(strings.get(id: SharedRes.strings().destinations_see_all))
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(SwiftUI.Color(SharedRes.colors().primary.getUIColor()))
            })
            .buttonStyle(.plain)
            .accessibilityIdentifier(TestTags.shared.DESTINATIONS_SEE_ALL_BUTTON)
        }
        .padding(.horizontal, 16)
    }
}
