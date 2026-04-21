import Shared
import SwiftUI

struct SearchSheetView: View {
    let strings: Strings
    let height: CGFloat
    @Binding var selectedDetent: PresentationDetent
    let onMenuClick: () -> Void
    let onPlaceSelected: (Place) -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getPlaceFinderViewModel()
    @FocusState private var isSearchFieldFocused: Bool

    private var collapsedDetent: PresentationDetent {
        .height(height)
    }

    private var isExpanded: Bool {
        selectedDetent == .large
    }

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            VStack(spacing: 0) {
                searchBar(uiState: uiState)
                if isExpanded {
                    attributionRow(isLoading: uiState.isLoading)
                    if !uiState.places.isEmpty {
                        resultsList(places: uiState.places)
                    } else if let error = uiState.error {
                        InfoView(
                            strings: strings,
                            infoViewData: error,
                            primaryActionText: strings.get(id: SharedRes.strings().search_error_retry),
                            onPrimaryActionClick: {
                                viewModel.onEvent(event: PlaceFinderUiEventsRetryClicked.shared)
                            }
                        )
                        .padding(.top, 32)
                        .padding(.horizontal, 16)
                        Spacer(minLength: 0)
                    } else {
                        Spacer(minLength: 0)
                    }
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .background(isExpanded ? Color(.systemGray6) : Color.clear)
            .animation(.interactiveSpring(duration: 0.3), value: isSearchFieldFocused)
            .onChange(of: selectedDetent) { _, newValue in
                if newValue != .large {
                    clearSearch()
                }
            }
            .onChange(of: isSearchFieldFocused) { _, newValue in
                if newValue {
                    expandSheet()
                }
            }
        }
    }

    @ViewBuilder
    private func searchBar(uiState: PlaceFinderUiState) -> some View {
        HStack {
            searchTextField(uiState: uiState)
            trailingButton
        }
        .padding(.top, 15)
        .padding(.horizontal, 18)
    }

    @ViewBuilder
    private func searchTextField(uiState: PlaceFinderUiState) -> some View {
        let queryBinding = Binding<String>(
            get: { uiState.searchText },
            set: { viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: $0)) }
        )

        TextField(
            "",
            text: queryBinding,
            prompt: Text(strings.get(id: SharedRes.strings().search_input_placeholder))
                .foregroundStyle(Color(.secondaryLabel))
        )
        .font(.system(size: 18, weight: .regular))
        .foregroundStyle(.secondary)
        .submitLabel(.search)
        .textContentType(.fullStreetAddress)
        .focused($isSearchFieldFocused)
        .tint(Color(SharedRes.colors().primary.getUIColor()))
        .padding(.leading, 48)
        .padding(.trailing, uiState.searchText.isEmpty ? 16 : 44)
        .padding(.vertical, 14)
        .background(Color(.systemBackground), in: .capsule)
        .overlay(alignment: .leading) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(.primary)
                .padding(.leading, 18)
        }
        .overlay(alignment: .trailing) {
            if !uiState.searchText.isEmpty {
                clearButton
            }
        }
        .contentShape(Capsule())
        .onTapGesture {
            isSearchFieldFocused = true
        }
    }

    @ViewBuilder
    private var clearButton: some View {
        Button(action: {
            viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: ""))
        }, label: {
            Image(systemName: "xmark.circle.fill")
                .font(.system(size: 20))
                .foregroundStyle(.primary)
                .padding(.trailing, 14)
        })
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_clear_text))
    }

    @ViewBuilder
    private var trailingButton: some View {
        if isExpanded {
            Button(action: {
                clearSearch()
                collapseSheet()
            }, label: {
                Image(systemName: "xmark")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.primary)
                    .padding(12)
                    .background(
                        Circle().fill(Color(.systemBackground))
                    )
            })
            .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
        } else {
            Button(action: onMenuClick) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundColor(.primary)
                    .padding(.horizontal, 12)
            }
            .buttonStyle(.plain)
            .contentShape(Circle())
            .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_menu))
        }
    }

    @ViewBuilder
    private func attributionRow(isLoading: Bool) -> some View {
        HStack(spacing: 8) {
            Spacer()
            if isLoading {
                ProgressView()
                    .scaleEffect(0.8)
            }
            Text(strings.get(id: SharedRes.strings().search_powered_by))
                .font(.caption)
                .foregroundStyle(Color(.secondaryLabel))
            Image(resource: \.ic_location_iq_logo)
                .resizable()
                .scaledToFit()
                .frame(height: 16)
        }
        .frame(height: 28)
        .padding(.horizontal, 24)
        .padding(.top, 10)
    }

    @ViewBuilder
    private func resultsList(places: [Place]) -> some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(Array(places.enumerated()), id: \.offset) { index, place in
                    SearchResultItem(place: place, onClick: {
                        isSearchFieldFocused = false
                        onPlaceSelected(place)
                    })
                    if index < places.count - 1 {
                        Divider()
                            .padding(.leading, 76)
                    }
                }
            }
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 24)
        }
        .scrollDismissesKeyboard(.interactively)
    }

    private func expandSheet() {
        withAnimation(.easeInOut(duration: 0.2)) {
            selectedDetent = .large
        }
    }

    private func collapseSheet() {
        withAnimation(.easeInOut(duration: 0.2)) {
            selectedDetent = collapsedDetent
        }
    }

    private func clearSearch() {
        isSearchFieldFocused = false
        viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: ""))
    }
}
