import Shared
import SwiftUI

struct DestinationsSectionView: View {
    let strings: Strings
    let destinations: [Destination]
    let onDestinationSelected: (Destination) -> Void
    var title: String?
    var topPadding: CGFloat = Dimens.sectionSpacing
    var onSeeAllClicked: (() -> Void)?

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionHeaderView(
                title: title ?? strings.get(id: SharedRes.strings().destinations_section_title),
                actionText: onSeeAllClicked == nil ? nil : strings.get(id: SharedRes.strings().see_all),
                onActionClick: onSeeAllClicked,
                actionAccessibilityId: TestTags.shared.DESTINATIONS_SEE_ALL_BUTTON
            )
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
        .padding(.top, topPadding)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_SECTION)
    }
}
