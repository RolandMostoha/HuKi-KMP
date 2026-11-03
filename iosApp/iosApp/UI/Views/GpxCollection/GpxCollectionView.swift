import Shared
import SwiftUI

struct GpxCollectionView: View {
    let onOpenTutorial: () -> Void
    let onOpenGpx: (String) -> Void

    @StateObject private var holder = ViewModelHolder(KoinViewModelProvider.shared.getGpxCollectionViewModel)
    private var viewModel: GpxCollectionViewModel { holder.viewModel }
    @State private var showDeleteConfirm = false
    @State private var shareItem: ShareItem?
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            Group {
                if !uiState.isLoading && uiState.sections.isEmpty {
                    emptyView(fileCount: uiState.fileCount)
                        .transition(.opacity)
                } else {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 0) {
                            header(fileCount: uiState.fileCount, isLoading: uiState.isLoading)
                            sortModePicker(selected: uiState.sortMode)
                            if uiState.isLoading {
                                loadingView
                                    .transition(.opacity)
                            }
                            ForEach(displayedSections(uiState), id: \.header.key) { section in
                                GpxFileSectionView(
                                    strings: strings,
                                    section: section,
                                    onEvent: { viewModel.onEvent(event: $0) }
                                )
                                .transition(.opacity)
                            }
                        }
                        .padding(.bottom, 24)
                        .readableWidth()
                    .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_LIST)
                    }
                    .transition(.opacity)
                }
            }
            .animation(.easeInOut, value: uiState.isLoading)
            .background(Color(.systemGroupedBackground))
            .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_SCREEN_ROOT)
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) { backButton }
                ToolbarItem(placement: .topBarTrailing) { helpButton }
            }
            .task {
                viewModel.onEvent(event: GpxCollectionUiEventsScreenViewed.shared)
            }
            .task {
                for await effect in viewModel.uiEffects {
                    handleEffect(effect)
                }
            }
            .alert(
                strings.get(id: SharedRes.strings().gpx_collection_delete_dialog_title),
                isPresented: $showDeleteConfirm,
                presenting: uiState.pendingDelete
            ) { _ in
                Button(strings.get(id: SharedRes.strings().gpx_collection_action_delete), role: .destructive) {
                    viewModel.onEvent(event: GpxCollectionUiEventsDeleteConfirmed.shared)
                }
                Button(strings.get(id: SharedRes.strings().gpx_collection_delete_dialog_cancel), role: .cancel) {
                    viewModel.onEvent(event: GpxCollectionUiEventsDeleteDismissed.shared)
                }
            } message: { file in
                Text(strings.get(id: SharedRes.strings().gpx_collection_delete_dialog_message, args: [file.fileName]))
            }
            .onChange(of: uiState.pendingDelete != nil) { _, newValue in
                showDeleteConfirm = newValue
            }
            .sheet(item: $shareItem) { item in
                ShareSheetView(url: item.url)
            }
        }
    }

    private var loadingView: some View {
        ProgressView()
            .controlSize(.large)
            .frame(maxWidth: .infinity, minHeight: 320)
            .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_LOADING)
    }

    private func emptyView(fileCount: Int32) -> some View {
        VStack(spacing: 0) {
            header(fileCount: fileCount)
            Spacer()
            InfoView(
                strings: strings,
                infoViewData: InfoViewData(
                    infoViewType: .success,
                    icon: SharedRes.images().ic_gpx,
                    title: SharedRes.strings().gpx_collection_empty_title,
                    message: SharedRes.strings().gpx_collection_empty_message
                ),
                primaryActionText: strings.get(id: SharedRes.strings().gpx_collection_empty_action),
                onPrimaryActionClick: {
                    viewModel.onEvent(event: GpxCollectionUiEventsHelpClicked.shared)
                }
            )
            .padding(.horizontal, 16)
            .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_EMPTY_VIEW)
            Spacer()
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }

    private func header(fileCount: Int32, isLoading: Bool = false) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(strings.get(id: SharedRes.strings().gpx_collection_title))
                .font(.largeTitle.weight(.bold))
                .foregroundStyle(.primary)
            Text(subtitle(fileCount: fileCount))
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .opacity(isLoading ? 0 : 1)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 16)
        .padding(.top, 4)
        .padding(.bottom, 12)
    }

    private func subtitle(fileCount: Int32) -> String {
        if fileCount == 0 {
            return strings.get(id: SharedRes.strings().gpx_collection_subtitle_empty)
        }
        let res = fileCount == 1
            ? SharedRes.strings().gpx_collection_subtitle_single
            : SharedRes.strings().gpx_collection_subtitle_pattern
        return strings.get(id: res, args: [fileCount])
    }

    private func displayedSections(_ uiState: GpxCollectionUiState) -> [GpxFileSection] {
        uiState.sortMode == .landscape ? uiState.sections.sortedByLandscapeName(strings: strings) : uiState.sections
    }

    private func sortModePicker(selected: GpxCollectionSortMode) -> some View {
        Picker(
            strings.get(id: SharedRes.strings().gpx_collection_sort_a11y),
            selection: Binding(
                get: { selected },
                set: { viewModel.onEvent(event: GpxCollectionUiEventsSortModeSelected(sortMode: $0)) }
            )
        ) {
            ForEach(sortModes, id: \.self) { sortMode in
                Text(strings.get(id: sortMode.label))
                    .tag(sortMode)
                    .accessibilityIdentifier(sortMode.testTag)
            }
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }

    private var sortModes: [GpxCollectionSortMode] {
        [.date, .distance, .landscape]
    }

    private var backButton: some View {
        Button(
            action: { viewModel.onEvent(event: GpxCollectionUiEventsBackClicked.shared) },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().gpx_collection_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_BACK_BUTTON)
    }

    private var helpButton: some View {
        Button(
            action: { viewModel.onEvent(event: GpxCollectionUiEventsHelpClicked.shared) },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().gpx_collection_a11y_help),
                    systemImage: "questionmark"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_HELP_BUTTON)
    }

    private func handleEffect(_ effect: GpxCollectionUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
        case .navigateToTutorial:
            onOpenTutorial()
        case .openGpx(let effect):
            onOpenGpx(effect.fileUri)
        case .shareGpx(let effect):
            shareItem = ShareItem(url: URL(fileURLWithPath: effect.fileUri))
        }
    }
}

private extension GpxCollectionSortMode {
    var label: StringResource {
        switch self {
        case .date: return SharedRes.strings().gpx_collection_sort_date
        case .distance: return SharedRes.strings().gpx_collection_sort_distance
        case .landscape: return SharedRes.strings().gpx_collection_sort_landscape
        }
    }

    var testTag: String {
        switch self {
        case .date: return TestTags.shared.GPX_COLLECTION_SORT_DATE
        case .distance: return TestTags.shared.GPX_COLLECTION_SORT_DISTANCE
        case .landscape: return TestTags.shared.GPX_COLLECTION_SORT_LANDSCAPE
        }
    }
}
