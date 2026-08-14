import Shared
import SwiftUI

struct OnlineResultsSectionView: View {
    let strings: Strings
    let places: [Place]
    let error: InfoViewData?
    let isLoading: Bool
    let onPlaceSelected: (Place) -> Void
    let onRetryClicked: () -> Void
    let onLocationIqClicked: () -> Void

    var body: some View {
        if !places.isEmpty || isLoading || error != nil {
            VStack(alignment: .leading, spacing: 8) {
                header
                if isLoading {
                    HStack(spacing: 8) {
                        ProgressView().controlSize(.small)
                        Text(strings.get(id: SharedRes.strings().search_searching))
                            .font(.body)
                            .foregroundStyle(Color(.secondaryLabel))
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.top, 24)
                } else if !places.isEmpty {
                    VStack(spacing: 0) {
                        ForEach(Array(places.enumerated()), id: \.element.osmId) { index, place in
                            SearchResultItem(place: place, onClick: { onPlaceSelected(place) })
                            if index < places.count - 1 {
                                Divider().padding(.leading, 76)
                            }
                        }
                    }
                    .background(Color(.systemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    .padding(.horizontal, 16)
                } else if let error {
                    InfoView(
                        strings: strings,
                        infoViewData: error,
                        primaryActionText: strings.get(id: SharedRes.strings().search_error_retry),
                        onPrimaryActionClick: onRetryClicked
                    )
                    .padding(.top, 16)
                    .padding(.horizontal, 16)
                }
            }
            .padding(.top, 24)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(TestTags.shared.SEARCH_ONLINE_SECTION)
        }
    }

    @ViewBuilder
    private var header: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(strings.get(id: SharedRes.strings().search_online_title))
                .font(.system(size: 20, weight: .bold))
                .foregroundStyle(.primary)
            Spacer()
            Button(action: onLocationIqClicked) {
                HStack(spacing: 8) {
                    Text(strings.get(id: SharedRes.strings().search_powered_by))
                        .font(.caption)
                        .foregroundStyle(Color(.secondaryLabel))
                    Image(uiImage: SharedRes.images().ic_location_iq_logo.toUIImage()!)
                        .resizable()
                        .scaledToFit()
                        .frame(height: 16)
                }
            }
            .buttonStyle(.plain)
            .accessibilityLabel(strings.get(id: SharedRes.strings().menu_a11y_open_location_iq))
        }
        .padding(.horizontal, 16)
    }
}
