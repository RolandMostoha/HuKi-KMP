import Shared
import SwiftUI

struct DestinationsView: View {
    let onShowOnMap: (Destination) -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getDestinationsViewModel()
    @State private var selectedTab: DestinationsTab = .popular
    @State private var destinationCount: Int32 = 0
    @State private var sortedLandscapes: [Landscape] = []
    @State private var preview: DestinationPreviewItem?
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    var body: some View {
        ScrollView {
            Observing(viewModel.uiState) { uiState in
                LazyVStack(alignment: .leading, spacing: 0) {
                    if #unavailable(iOS 26) {
                        inlineSubtitle(destinationCount: uiState.destinationCount)
                    }
                    tabBar
                    content(uiState: uiState)
                }
                .padding(.bottom, 24)
                .accessibilityIdentifier(TestTags.shared.DESTINATIONS_LIST)
                .onAppear {
                    selectedTab = uiState.selectedTab
                    destinationCount = uiState.destinationCount
                    if sortedLandscapes.isEmpty {
                        sortedLandscapes = sortLandscapes(uiState.landscapes)
                    }
                }
                .onChange(of: uiState.selectedTab) { _, newTab in
                    if selectedTab != newTab { selectedTab = newTab }
                }
                .onChange(of: uiState.destinationCount) { _, newCount in
                    destinationCount = newCount
                }
                .onChange(of: uiState.landscapes.count) {
                    sortedLandscapes = sortLandscapes(uiState.landscapes)
                }
            }
        }
        .background(Color(.systemGroupedBackground))
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_SCREEN_ROOT)
        .navigationTitle(strings.get(id: SharedRes.strings().destinations_title))
        .navigationBarTitleDisplayMode(.large)
        .navigationSubtitleCompat(subtitle(destinationCount: destinationCount))
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) { backButton }
        }
        .task {
            for await effect in viewModel.uiEffects {
                handleEffect(effect)
            }
        }
        .destinationPreview(item: $preview, strings: strings, onShowOnMap: onShowOnMap)
    }

    private func inlineSubtitle(destinationCount: Int32) -> some View {
        Text(subtitle(destinationCount: destinationCount))
            .font(.subheadline)
            .foregroundStyle(.secondary)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 8)
    }

    private func subtitle(destinationCount: Int32) -> String {
        let res = destinationCount == 1
            ? SharedRes.strings().destinations_subtitle_single
            : SharedRes.strings().destinations_subtitle_pattern
        return strings.get(id: res, args: [destinationCount])
    }

    private var tabBar: some View {
        Picker("", selection: $selectedTab) {
            ForEach(DestinationsTab.allCases, id: \.self) { tab in
                Text(strings.get(id: tab.title)).tag(tab)
            }
        }
        .pickerStyle(.segmented)
        .controlSize(.large)
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .onChange(of: selectedTab) { _, newTab in
            viewModel.onEvent(event: DestinationsUiEventsTabSelected(tab: newTab))
        }
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_TAB_BAR)
    }

    @ViewBuilder
    private func content(uiState: DestinationsUiState) -> some View {
        switch selectedTab {
        case .popular:
            popularList(uiState.popularItems)
        case .nearby:
            nearbyContent(uiState.nearbyState)
        case .landscapes:
            landscapesList
        }
    }

    private var landscapesList: some View {
        ForEach(sortedLandscapes, id: \.osmId) { landscape in
            landscapeSection(landscape)
        }
    }

    private func sortLandscapes(_ landscapes: [Landscape]) -> [Landscape] {
        landscapes
            .map { (key: strings.get(id: $0.nameRes), value: $0) }
            .sorted { $0.key.localizedCaseInsensitiveCompare($1.key) == .orderedAscending }
            .map(\.value)
    }

    private func landscapeSection(_ landscape: Landscape) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(strings.get(id: landscape.nameRes))
                .font(.title3.weight(.bold))
                .foregroundStyle(.primary)
                .padding(.leading, 4)
                .padding(.bottom, 6)
                .padding(.top, 12)
            VStack(spacing: 0) {
                ForEach(Array(landscape.destinations.enumerated()), id: \.element.osmId) { index, destination in
                    DestinationRowView(
                        strings: strings,
                        destination: destination,
                        onClick: {
                            showPreview(destination, landscapeText: strings.get(id: landscape.nameRes))
                        }
                    )
                    if index != landscape.destinations.count - 1 {
                        Divider().padding(.leading, 70)
                    }
                }
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(.horizontal, 16)
    }

    private func popularList(_ items: [DestinationListItem]) -> some View {
        let rows = tabRows(items, prefix: "popular")
        return ForEach(rows) { row in
            cardRow(isFirst: row.index == 0, isLast: row.index == rows.count - 1) {
                DestinationRowView(
                    strings: strings,
                    destination: row.item.destination,
                    rank: row.index + 1,
                    landscapeText: row.item.landscape.map { strings.get(id: $0.nameRes) },
                    onClick: {
                        showPreview(
                            row.item.destination,
                            landscapeText: row.item.landscape.map { strings.get(id: $0.nameRes) }
                        )
                    }
                )
            }
        }
    }

    private func tabRows(_ items: [DestinationListItem], prefix: String) -> [TabRow] {
        items.enumerated().map { offset, item in
            TabRow(id: "\(prefix)-\(item.destination.osmId)", index: offset, item: item)
        }
    }

    @ViewBuilder
    private func nearbyContent(_ nearbyState: NearbyState) -> some View {
        switch onEnum(of: nearbyState) {
        case .loading:
            ProgressView()
                .frame(maxWidth: .infinity)
                .padding(.top, 48)
        case .permissionRequired:
            nearbyPermissionView
        case .locationUnavailable:
            nearbyErrorView
        case .loaded(let loaded):
            let rows = tabRows(loaded.items, prefix: "nearby")
            ForEach(rows) { row in
                cardRow(isFirst: row.index == 0, isLast: row.index == rows.count - 1) {
                    DestinationRowView(
                        strings: strings,
                        destination: row.item.destination,
                        distanceText: row.item.distance,
                        onClick: { showPreview(row.item.destination) }
                    )
                }
            }
        }
    }

}

private extension DestinationsView {
    func cardRow<Content: View>(
        isFirst: Bool,
        isLast: Bool,
        @ViewBuilder _ content: () -> Content
    ) -> some View {
        VStack(spacing: 0) {
            content()
            if !isLast {
                Divider().padding(.leading, 70)
            }
        }
        .background(Color(.systemBackground))
        .clipShape(
            UnevenRoundedRectangle(
                topLeadingRadius: isFirst ? 16 : 0,
                bottomLeadingRadius: isLast ? 16 : 0,
                bottomTrailingRadius: isLast ? 16 : 0,
                topTrailingRadius: isFirst ? 16 : 0,
                style: .continuous
            )
        )
        .padding(.horizontal, 16)
        .padding(.top, isFirst ? 16 : 0)
    }

    private func showPreview(
        _ destination: Destination,
        landscapeText: String? = nil
    ) {
        preview = DestinationPreviewItem(
            destination: destination,
            landscapeText: landscapeText,
            distanceText: viewModel.distanceText(destination: destination)
        )
    }

    var nearbyPermissionView: some View {
        InfoView(
            strings: strings,
            infoViewData: InfoViewData(
                infoViewType: .warning,
                icon: .system("location.slash"),
                title: SharedRes.strings().destinations_nearby_permission_title,
                message: SharedRes.strings().destinations_nearby_permission_message
            ),
            primaryActionText: strings.get(id: SharedRes.strings().destinations_nearby_permission_action),
            onPrimaryActionClick: {
                viewModel.onEvent(event: DestinationsUiEventsGrantLocationClicked.shared)
            }
        )
        .padding(.horizontal, 16)
        .padding(.top, 40)
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_NEARBY_PERMISSION_VIEW)
    }

    var nearbyErrorView: some View {
        InfoView(
            strings: strings,
            infoViewData: InfoViewData(
                infoViewType: .error,
                icon: .system("location.slash"),
                title: SharedRes.strings().destinations_nearby_error_title,
                message: SharedRes.strings().destinations_nearby_error_message
            ),
            primaryActionText: strings.get(id: SharedRes.strings().destinations_nearby_error_action),
            onPrimaryActionClick: {
                viewModel.onEvent(event: DestinationsUiEventsRetryNearbyClicked.shared)
            }
        )
        .padding(.horizontal, 16)
        .padding(.top, 40)
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_NEARBY_ERROR_VIEW)
    }

    var backButton: some View {
        Button(
            action: { viewModel.onEvent(event: DestinationsUiEventsBackClicked.shared) },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().destinations_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_BACK_BUTTON)
    }

    func handleEffect(_ effect: DestinationsUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
        case .navigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        }
    }
}

private extension View {
    func destinationPreview(
        item: Binding<DestinationPreviewItem?>,
        strings: Strings,
        onShowOnMap: @escaping (Destination) -> Void
    ) -> some View {
        fullScreenCover(item: item) { previewItem in
            DestinationPreviewOverlay(
                strings: strings,
                destination: previewItem.destination,
                landscapeText: previewItem.landscapeText,
                distanceText: previewItem.distanceText,
                onShowOnMap: { onShowOnMap(previewItem.destination) },
                onDismiss: { item.wrappedValue = nil }
            )
        }
        .transaction { $0.disablesAnimations = true }
    }
}

private struct TabRow: Identifiable {
    let id: String
    let index: Int
    let item: DestinationListItem
}

private struct DestinationPreviewItem: Identifiable {
    let destination: Destination
    let landscapeText: String?
    let distanceText: String?

    var id: String { destination.osmId }
}

private extension View {
    @ViewBuilder
    func navigationSubtitleCompat(_ subtitle: String) -> some View {
        if #available(iOS 26, *) {
            self.navigationSubtitle(subtitle)
        } else {
            self
        }
    }
}
