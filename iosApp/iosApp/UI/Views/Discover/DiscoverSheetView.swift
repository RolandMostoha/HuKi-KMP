import Shared
import SwiftUI

struct DiscoverSheetView: View {
    let strings: Strings
    let onHikeRecommendationClicked: (HikeRecommendation) -> Void
    let onBrowseDestinationsClicked: () -> Void
    let onInfoClicked: () -> Void
    let onDismissRequest: () -> Void
    let onHeightChange: (CGFloat) -> Void

    @State private var contentHeight: CGFloat = 0
    @State private var toolbarHeight: CGFloat = 0
    @State private var isInfoPresented = false
    @State private var recommendationsRowWidth: CGFloat = 0
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private static let cardSpacing: CGFloat = 12
    private static let cardTitleStyles: [UIFont.TextStyle] = [.subheadline, .footnote, .caption1, .caption2]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    hikeRecommendationsSection
                    destinationsSection
                }
                .padding(.bottom, 24)
                .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                    contentHeight = height
                }
            }
            .scrollBounceBehavior(.basedOnSize)
            .onGeometryChange(for: CGFloat.self) { $0.safeAreaInsets.top } action: { inset in
                toolbarHeight = inset
            }
            .navigationTitle(strings.get(id: SharedRes.strings().discover_title))
            .toolbarTitleDisplayMode(.inlineLarge)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    closeButton
                }
            }
        }
        .accessibilityIdentifier(TestTags.shared.DISCOVER_SHEET)
        .onChange(of: contentHeight + toolbarHeight) { _, height in
            onHeightChange(height)
        }
    }

    @ViewBuilder
    private var closeButton: some View {
        if #available(iOS 26, *) {
            Button(role: .close, action: onDismissRequest)
                .accessibilityIdentifier(TestTags.shared.DISCOVER_CLOSE_BUTTON)
        } else {
            Button(action: onDismissRequest) {
                Image(systemName: "xmark")
            }
            .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
            .accessibilityIdentifier(TestTags.shared.DISCOVER_CLOSE_BUTTON)
        }
    }

    private var hikeRecommendationsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionHeaderView(title: strings.get(id: SharedRes.strings().discover_hike_recommendations_title))
                .overlay(alignment: .trailing) {
                    infoButton
                        .padding(.trailing, 5)
                }
            HStack(spacing: Self.cardSpacing) {
                ForEach(HikeRecommendation.allCases, id: \.self) { recommendation in
                    HikeRecommendationCardView(
                        strings: strings,
                        recommendation: recommendation,
                        backgroundColor: Color(.tertiarySystemFill),
                        titleFont: cardTitleFont,
                        onClick: { onHikeRecommendationClicked(recommendation) }
                    )
                    .accessibilityIdentifier(TestTags.shared.HIKE_RECOMMENDATION_CARD)
                }
            }
            .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { width in
                recommendationsRowWidth = width
            }
            .padding(.horizontal, 16)
        }
        .padding(.top, 8)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.DISCOVER_HIKE_RECOMMENDATIONS_SECTION)
    }

    private var cardTitleFont: Font {
        let count = CGFloat(HikeRecommendation.allCases.count)
        let titleWidth = (recommendationsRowWidth - Self.cardSpacing * (count - 1)) / count
            - HikeRecommendationCardView.horizontalPadding * 2
        let titles = HikeRecommendation.allCases.map { strings.get(id: $0.title) }
        let traits = UITraitCollection(preferredContentSizeCategory: UIContentSizeCategory(dynamicTypeSize))
        let fittingSize = Self.cardTitleStyles
            .map { UIFont.preferredFont(forTextStyle: $0, compatibleWith: traits).pointSize }
            .first { size in
                let font = UIFont.systemFont(ofSize: size, weight: .semibold)
                return titles.allSatisfy { ($0 as NSString).size(withAttributes: [.font: font]).width <= titleWidth }
            }
        return .system(size: fittingSize ?? UIFont.preferredFont(forTextStyle: .caption2).pointSize, weight: .semibold)
    }

    private var infoButton: some View {
        Button {
            onInfoClicked()
            isInfoPresented = true
        } label: {
            Image(systemName: "questionmark.circle")
                .font(.title2)
                .foregroundStyle(.secondary)
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().discover_hike_recommendations_a11y_info))
        .accessibilityIdentifier(TestTags.shared.DISCOVER_HIKE_RECOMMENDATIONS_INFO_BUTTON)
        .popover(isPresented: $isInfoPresented) {
            VStack(alignment: .leading, spacing: 8) {
                Text(strings.get(id: SharedRes.strings().discover_hike_recommendations_title))
                    .font(.headline)
                Text(strings.get(id: SharedRes.strings().discover_hike_recommendations_info))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            .fixedSize(horizontal: false, vertical: true)
            .frame(width: 300, alignment: .leading)
            .padding()
            .accessibilityIdentifier(TestTags.shared.DISCOVER_HIKE_RECOMMENDATIONS_INFO_TOOLTIP)
            .presentationCompactAdaptation(.popover)
        }
    }

    private var destinationsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionHeaderView(title: strings.get(id: SharedRes.strings().destinations_section_title))
            NavigationRowCardView(
                systemImage: "mappin.and.ellipse",
                title: strings.get(id: SharedRes.strings().discover_browse_destinations),
                subtitle: strings.get(id: SharedRes.strings().destinations_description),
                onClick: onBrowseDestinationsClicked
            )
            .padding(.horizontal, 16)
            .accessibilityIdentifier(TestTags.shared.DISCOVER_BROWSE_DESTINATIONS_BUTTON)
        }
        .padding(.top, Dimens.sectionSpacing)
    }
}

#Preview {
    DiscoverSheetView(
        strings: Strings(),
        onHikeRecommendationClicked: { _ in },
        onBrowseDestinationsClicked: {},
        onInfoClicked: {},
        onDismissRequest: {},
        onHeightChange: { _ in }
    )
}
