import Shared
import SwiftUI

enum TrailSymbolsGuideRoute: Hashable {
    case trailSymbolsGuide
}

struct TrailSymbolsGuideView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel = KoinViewModelProvider.shared.getTrailSymbolsGuideViewModel()

    private let strings = Strings()

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                symbolSection(
                    title: strings.get(id: SharedRes.strings().trail_symbols_main_section_title),
                    symbols: TrailSymbol.allCases.filter { $0.section == .route }
                )
                symbolSection(
                    title: strings.get(id: SharedRes.strings().trail_symbols_branch_section_title),
                    symbols: TrailSymbol.allCases.filter { $0.section == .target }
                )
            }
            .padding(.top, 14)
            .padding(.bottom, 24)
        }
        .background(Color(.systemGroupedBackground))
        .accessibilityIdentifier(TestTags.shared.TRAIL_SYMBOLS_GUIDE_SCREEN_ROOT)
        .navigationTitle(strings.get(id: SharedRes.strings().trail_symbols_guide_title))
        .navigationBarTitleDisplayMode(.large)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) { backButton }
        }
    }

    private var backButton: some View {
        Button(
            action: { dismiss() },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().trail_symbols_guide_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.TRAIL_SYMBOLS_GUIDE_BACK_BUTTON)
    }

    private func symbolSection(title: String, symbols: [TrailSymbol]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            sectionHeader(title)
            VStack(spacing: 0) {
                ForEach(Array(symbols.enumerated()), id: \.offset) { index, symbol in
                    if index > 0 {
                        Divider().padding(.leading, 78)
                    }
                    symbolRow(symbol)
                }
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(.horizontal, 16)
    }

    private func symbolRow(_ symbol: TrailSymbol) -> some View {
        HStack(spacing: 12) {
            Image(uiImage: symbol.iconRes.toUIImage()!)
                .resizable()
                .scaledToFit()
                .frame(width: 52, height: 44)
            VStack(alignment: .leading, spacing: 3) {
                Text(strings.get(id: symbol.title))
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
                Text(strings.get(id: symbol.descriptionRes))
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding(14)
    }

    private func sectionHeader(_ text: String) -> some View {
        Text(text.uppercased())
            .font(.footnote.weight(.bold))
            .tracking(0.8)
            .foregroundStyle(.primary.opacity(0.4))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.leading, 4)
    }
}
