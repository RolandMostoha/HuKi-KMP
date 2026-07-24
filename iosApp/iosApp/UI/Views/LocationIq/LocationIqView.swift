import Shared
import SwiftUI

enum LocationIqRoute: Hashable {
    case locationIq
}

struct LocationIqView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel = KoinViewModelProvider.shared.getLocationIqViewModel()

    private let strings = Strings()

    private let accentColor = Color(SharedRes.colors().locationIqRed.getUIColor())
    private let backgroundColor = Color(SharedRes.colors().locationIqBackground.getUIColor())
    private let cardColor = Color(SharedRes.colors().locationIqCard.getUIColor())
    private let containerColor = Color(SharedRes.colors().locationIqContainer.getUIColor())
    private let buttonColor = Color(SharedRes.colors().locationIqButton.getUIColor())

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                hero
                sectionHeader(strings.get(id: SharedRes.strings().location_iq_section_how))
                usageCard(
                    systemImage: "magnifyingglass",
                    title: strings.get(id: SharedRes.strings().location_iq_card_autocomplete_title),
                    description: strings.get(id: SharedRes.strings().location_iq_card_autocomplete_description)
                )
                .padding(.bottom, 13)
                usageCard(
                    systemImage: "mappin.and.ellipse",
                    title: strings.get(id: SharedRes.strings().location_iq_card_geocoding_title),
                    description: strings.get(id: SharedRes.strings().location_iq_card_geocoding_description)
                )
                sectionHeader(strings.get(id: SharedRes.strings().location_iq_section_offers))
                offersRow
                visitWebsiteButton
                    .padding(.top, 24)
            }
            .padding(.bottom, 24)
            .readableWidth()
        }
        .background(backgroundColor.ignoresSafeArea())
        .accessibilityIdentifier(TestTags.shared.LOCATION_IQ_SCREEN_ROOT)
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(
                    action: { dismiss() },
                    label: {
                        Label(
                            strings.get(id: SharedRes.strings().location_iq_a11y_back),
                            systemImage: "chevron.backward"
                        )
                        .fontWeight(.semibold)
                        .foregroundStyle(.primary)
                    }
                )
                .labelStyle(.iconOnly)
                .accessibilityIdentifier(TestTags.shared.LOCATION_IQ_BACK_BUTTON)
            }
        }
    }

    private var hero: some View {
        VStack(spacing: 0) {
            Text(strings.get(id: SharedRes.strings().location_iq_headline))
                .font(.title.weight(.bold))
                .multilineTextAlignment(.center)
                .foregroundStyle(.primary)
            Image(uiImage: SharedRes.images().ic_location_iq_logo.toUIImage()!)
                .resizable()
                .scaledToFit()
                .frame(height: 30)
                .padding(.top, 6)
                .accessibilityLabel(strings.get(id: SharedRes.strings().location_iq_a11y_logo))
            Text(strings.get(id: SharedRes.strings().location_iq_intro))
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.top, 16)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 10)
    }

    private func sectionHeader(_ text: String) -> some View {
        Text(text.uppercased())
            .font(.subheadline.weight(.bold))
            .tracking(1.5)
            .foregroundStyle(.primary.opacity(0.4))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.top, 20)
            .padding(.bottom, 10)
    }

    private func iconChip(systemImage: String) -> some View {
        RoundedRectangle(cornerRadius: 13, style: .continuous)
            .fill(containerColor)
            .frame(width: 40, height: 40)
            .overlay {
                Image(systemName: systemImage)
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(accentColor)
            }
    }

    private func usageCard(systemImage: String, title: String, description: String) -> some View {
        HStack(alignment: .top, spacing: 16) {
            iconChip(systemImage: systemImage)
            VStack(alignment: .leading, spacing: 6) {
                Text(title)
                    .font(.headline)
                    .foregroundStyle(.primary)
                Text(description)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .background(cardColor, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .padding(.horizontal, 16)
    }

    private var offersRow: some View {
        HStack(spacing: 13) {
            offerCard(
                systemImage: "mappin.and.ellipse",
                label: strings.get(id: SharedRes.strings().location_iq_offer_geocoding)
            )
            offerCard(
                systemImage: "map",
                label: strings.get(id: SharedRes.strings().location_iq_offer_maps)
            )
            offerCard(
                systemImage: "point.topleft.down.to.point.bottomright.curvepath",
                label: strings.get(id: SharedRes.strings().location_iq_offer_routing)
            )
        }
        .padding(.horizontal, 16)
    }

    private func offerCard(systemImage: String, label: String) -> some View {
        VStack(spacing: 10) {
            iconChip(systemImage: systemImage)
            Text(label)
                .font(.subheadline.weight(.bold))
                .foregroundStyle(.primary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .background(cardColor, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    private var visitWebsiteButton: some View {
        Button(action: openWebsite) {
            Text(strings.get(id: SharedRes.strings().location_iq_visit_website))
                .font(.headline)
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(buttonColor, in: .capsule)
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(TestTags.shared.LOCATION_IQ_VISIT_WEBSITE)
        .padding(.horizontal, 16)
    }

    private func openWebsite() {
        if let url = URL(string: strings.get(id: SharedRes.strings().menu_location_iq_url)) {
            UIApplication.shared.open(url)
        }
    }
}
