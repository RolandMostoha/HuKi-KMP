import Shared
import SwiftUI

struct LandscapesSheetView: View {
    let strings: Strings
    let landscapes: LandscapesUiState
    let onLandscapeClicked: (String) -> Void
    let onCategoryClicked: (String) -> Void
    let onShowAllClicked: () -> Void
    /// Nil when the sheet has a single height.
    let onExpandToggleClicked: (() -> Void)?
    let isExpanded: Bool
    let onDestinationClicked: (Destination) -> Void
    let onDismissRequest: () -> Void

    private static let res = SharedRes.strings()

    @State private var rows: [DestinationGroupRow] = []

    private var isCategories: Bool {
        landscapes.grouping == .category
    }

    private var selectedRowId: String? {
        isCategories ? landscapes.selectedCategoryId : landscapes.selectedLandscapeId
    }

    var body: some View {
        NavigationStack {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 4) {
                        ForEach(rows) { row in
                            let isSelected = row.id == selectedRowId
                            DestinationGroupRowView(
                                strings: strings,
                                icon: row.icon,
                                iconSize: row.iconSize,
                                color: row.color,
                                title: row.title,
                                destinationCount: row.destinationCount,
                                destinations: isSelected ? landscapes.destinations : [],
                                isSelected: isSelected,
                                selectedDestinationId: landscapes.selectedDestinationId,
                                onClick: row.onClick,
                                onDestinationClick: onDestinationClicked
                            )
                            .id(row.id)
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.bottom, 24)
                    .animation(.snappy, value: selectedRowId)
                }
                .onChange(of: selectedRowId) { _, rowId in
                    guard let rowId else { return }
                    withAnimation(.snappy) {
                        proxy.scrollTo(rowId, anchor: .top)
                    }
                }
                .onAppear {
                    if rows.isEmpty {
                        rows = isCategories ? categoryRows() : landscapeRows()
                    }
                    // The lazy rows are not laid out yet on appear
                    DispatchQueue.main.async {
                        proxy.scrollTo(selectedRowId, anchor: .top)
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    leadingButton
                }
                ToolbarItem(placement: .principal) {
                    title
                }
                ToolbarItem(placement: .topBarTrailing) {
                    closeButton
                }
            }
        }
        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_SHEET)
    }

    private func landscapeRows() -> [DestinationGroupRow] {
        sortedByTitle(
            landscapes.landscapes.map { landscape in
                DestinationGroupRow(
                    id: landscape.id,
                    icon: landscape.landscapeType.iconRes.toUIImage()!,
                    iconSize: 26,
                    color: SwiftUI.Color(landscape.colorRes.getUIColor()),
                    title: strings.get(id: landscape.nameRes),
                    destinationCount: landscape.destinations.count,
                    onClick: { onLandscapeClicked(landscape.id) }
                )
            }
        )
    }

    private func categoryRows() -> [DestinationGroupRow] {
        sortedByTitle(
            landscapes.categories.map { category in
                DestinationGroupRow(
                    id: category.id,
                    icon: category.iconRes.toUIImage()!,
                    iconSize: 24,
                    color: SwiftUI.Color(category.colorRes.getUIColor()),
                    title: strings.get(id: category.title),
                    destinationCount: category.destinations.count,
                    onClick: { onCategoryClicked(category.id) }
                )
            }
        )
    }

    private func sortedByTitle(_ rows: [DestinationGroupRow]) -> [DestinationGroupRow] {
        rows.sorted { $0.title.localizedCaseInsensitiveCompare($1.title) == .orderedAscending }
    }

    private var title: some View {
        Text(strings.get(id: isCategories ? Self.res.categories_title : Self.res.landscapes_title))
            .font(.headline)
            .accessibilityAddTraits(.isHeader)
    }

    @ViewBuilder
    private var leadingButton: some View {
        // Categories have no country-wide overview, so the action resizes the sheet instead
        if !isCategories {
            showAllButton
        } else if let onExpandToggleClicked {
            Button(action: onExpandToggleClicked) {
                Image(systemName: isExpanded ? "chevron.down" : "chevron.up")
            }
            .accessibilityLabel(
                strings.get(id: isExpanded ? Self.res.a11y_collapse_sheet : Self.res.a11y_expand_sheet)
            )
            .accessibilityIdentifier(TestTags.shared.LANDSCAPES_EXPAND_BUTTON)
        }
    }

    private var showAllButton: some View {
        Button(action: onShowAllClicked) {
            Image(systemName: "arrow.up.left.and.arrow.down.right")
        }
        .accessibilityLabel(strings.get(id: Self.res.landscapes_a11y_show_all))
        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_SHOW_ALL_BUTTON)
    }

    @ViewBuilder
    private var closeButton: some View {
        if #available(iOS 26, *) {
            Button(role: .close, action: onDismissRequest)
                .accessibilityIdentifier(TestTags.shared.LANDSCAPES_CLOSE_BUTTON)
        } else {
            Button(action: onDismissRequest) {
                Image(systemName: "xmark")
            }
            .accessibilityLabel(
                strings.get(id: isCategories ? Self.res.categories_a11y_close : Self.res.landscapes_a11y_close)
            )
            .accessibilityIdentifier(TestTags.shared.LANDSCAPES_CLOSE_BUTTON)
        }
    }
}

private struct DestinationGroupRow: Identifiable {
    let id: String
    let icon: UIImage
    let iconSize: CGFloat
    let color: SwiftUI.Color
    let title: String
    let destinationCount: Int
    let onClick: () -> Void
}
