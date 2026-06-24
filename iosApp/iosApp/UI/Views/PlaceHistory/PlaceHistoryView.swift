import Shared
import SwiftUI

struct PlaceHistoryView: View {
    let onOpenPlace: (OsmType, String) -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getPlaceHistoryViewModel()
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            Group {
                if !uiState.isLoading && uiState.sections.isEmpty {
                    emptyView(placeCount: uiState.placeCount)
                } else {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 0) {
                            header(placeCount: uiState.placeCount)
                            ForEach(uiState.sections, id: \.stableId) { section in
                                sectionView(section)
                            }
                        }
                        .padding(.bottom, 24)
                        .accessibilityIdentifier(TestTags.shared.PLACE_HISTORY_LIST)
                    }
                }
            }
            .background(Color(.systemGroupedBackground))
            .accessibilityIdentifier(TestTags.shared.PLACE_HISTORY_SCREEN_ROOT)
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) { backButton }
            }
            .task {
                for await effect in viewModel.uiEffects {
                    handleEffect(effect)
                }
            }
        }
    }

    private func emptyView(placeCount: Int32) -> some View {
        VStack(spacing: 0) {
            header(placeCount: placeCount)
            Spacer()
            InfoView(
                strings: strings,
                infoViewData: InfoViewData(
                    infoViewType: .info,
                    icon: .system("mappin.and.ellipse"),
                    title: SharedRes.strings().place_history_empty_title,
                    message: SharedRes.strings().place_history_empty_message
                )
            )
            .padding(.horizontal, 16)
            .accessibilityIdentifier(TestTags.shared.PLACE_HISTORY_EMPTY_VIEW)
            Spacer()
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }

    private func header(placeCount: Int32) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(strings.get(id: SharedRes.strings().place_history_title))
                .font(.largeTitle.weight(.bold))
                .foregroundStyle(.primary)
            Text(subtitle(placeCount: placeCount))
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 16)
        .padding(.top, 4)
        .padding(.bottom, 12)
    }

    private func subtitle(placeCount: Int32) -> String {
        if placeCount == 0 {
            return strings.get(id: SharedRes.strings().place_history_subtitle_empty)
        }
        let res = placeCount == 1
            ? SharedRes.strings().place_history_subtitle_single
            : SharedRes.strings().place_history_subtitle_pattern
        return strings.get(id: res, args: [placeCount])
    }

    private func sectionView(_ section: PlaceHistorySection) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            sectionHeader(section.header)
            VStack(spacing: 0) {
                ForEach(Array(section.items.enumerated()), id: \.element.place.listIdentity) { index, item in
                    PlaceRowView(
                        place: item.place,
                        onClick: {
                            viewModel.onEvent(event: PlaceHistoryUiEventsPlaceClicked(place: item.place))
                        }
                    )
                    .accessibilityIdentifier(TestTags.shared.PLACE_HISTORY_ITEM)
                    if index != section.items.count - 1 {
                        Divider().padding(.leading, 70)
                    }
                }
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }

    private func sectionHeader(_ header: PlaceHistoryHeader) -> some View {
        Text(headerText(header))
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(.secondary)
            .textCase(.uppercase)
            .padding(.leading, 4)
    }

    private func headerText(_ header: PlaceHistoryHeader) -> String {
        switch onEnum(of: header) {
        case .today:
            return strings.get(id: SharedRes.strings().place_history_date_today)
        case .yesterday:
            return strings.get(id: SharedRes.strings().place_history_date_yesterday)
        case .date(let date):
            return date.label
        }
    }

    private var backButton: some View {
        Button(
            action: { viewModel.onEvent(event: PlaceHistoryUiEventsBackClicked.shared) },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().place_history_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.PLACE_HISTORY_BACK_BUTTON)
    }

    private func handleEffect(_ effect: PlaceHistoryUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
        case .openPlace(let effect):
            onOpenPlace(effect.osmType, effect.osmId)
        }
    }
}

private extension PlaceHistorySection {
    var stableId: String {
        switch onEnum(of: header) {
        case .today: return "today"
        case .yesterday: return "yesterday"
        case .date(let date): return "date_\(date.label)"
        }
    }
}
