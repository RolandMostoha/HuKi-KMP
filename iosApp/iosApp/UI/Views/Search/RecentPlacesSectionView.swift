import Shared
import SwiftUI

struct RecentPlacesSectionView: View {
    let strings: Strings
    let places: [Place]
    let onPlaceSelected: (Place) -> Void
    var onSeeAllClicked: (() -> Void)?

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeaderView(
                title: strings.get(id: SharedRes.strings().search_recent_places_title),
                actionText: onSeeAllClicked == nil ? nil : strings.get(id: SharedRes.strings().see_all),
                onActionClick: onSeeAllClicked,
                actionAccessibilityId: TestTags.shared.RECENT_PLACES_SEE_ALL_BUTTON
            )
            VStack(spacing: 0) {
                ForEach(Array(places.enumerated()), id: \.element.listIdentity) { index, place in
                    PlaceRowView(
                        place: place,
                        onClick: { onPlaceSelected(place) }
                    )
                    .accessibilityIdentifier(TestTags.shared.RECENT_PLACES_ITEM)
                    if index < places.count - 1 {
                        Divider()
                            .padding(.leading, 70)
                    }
                }
            }
            .padding(.vertical, 2)
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
        .padding(.top, Dimens.sectionSpacing)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.RECENT_PLACES_SECTION)
    }
}
