import Shared
import SwiftUI

struct SearchSheetView: View {
    let strings: Strings
    let onDismiss: () -> Void
    let onPlaceSelected: (Place) -> Void
    let onLocationIqClicked: () -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getPlaceFinderViewModel()
    @FocusState private var isSearchFieldFocused: Bool

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            VStack(spacing: 0) {
                searchBar(uiState: uiState)
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
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .background(Color(.systemGray6))
            .onAppear {
                isSearchFieldFocused = true
            }
            .onDisappear {
                viewModel.clear()
            }
        }
    }

    @ViewBuilder
    private func searchBar(uiState: PlaceFinderUiState) -> some View {
        HStack {
            searchTextField(uiState: uiState)
            closeButton
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
    private var closeButton: some View {
        Button(action: {
            isSearchFieldFocused = false
            viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: ""))
            onDismiss()
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
    }

    @ViewBuilder
    private func attributionRow(isLoading: Bool) -> some View {
        HStack(spacing: 8) {
            Spacer()
            if isLoading {
                ProgressView()
                    .scaleEffect(0.8)
            }
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
            .accessibilityLabel(strings.get(id: SharedRes.strings().settings_a11y_open_location_iq))
        }
        .frame(height: 28)
        .padding(.horizontal, 24)
        .padding(.top, 10)
    }

    @ViewBuilder
    private func resultsList(places: [Place]) -> some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(Array(places.enumerated()), id: \.element.id) { index, place in
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
            .animation(.easeInOut(duration: 0.2), value: places.map(\.id))
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 24)
        }
        .scrollDismissesKeyboard(.interactively)
    }
}
