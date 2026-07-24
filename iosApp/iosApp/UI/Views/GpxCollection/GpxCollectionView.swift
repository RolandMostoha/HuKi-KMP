import Shared
import SwiftUI

struct GpxCollectionView: View {
    let onOpenTutorial: () -> Void
    let onOpenGpx: (String) -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getGpxCollectionViewModel()
    @State private var showDeleteConfirm = false
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            Group {
                if !uiState.isLoading && uiState.sections.isEmpty {
                    emptyView(fileCount: uiState.fileCount)
                } else {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 0) {
                            header(fileCount: uiState.fileCount)
                            ForEach(uiState.sections, id: \.stableId) { section in
                                sectionView(section)
                            }
                        }
                        .padding(.bottom, 24)
                        .readableWidth()
                    .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_LIST)
                    }
                }
            }
            .background(Color(.systemGroupedBackground))
            .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_SCREEN_ROOT)
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) { backButton }
                ToolbarItem(placement: .topBarTrailing) { helpButton }
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
        }
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

    private func header(fileCount: Int32) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(strings.get(id: SharedRes.strings().gpx_collection_title))
                .font(.largeTitle.weight(.bold))
                .foregroundStyle(.primary)
            Text(subtitle(fileCount: fileCount))
                .font(.subheadline)
                .foregroundStyle(.secondary)
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

    private func sectionView(_ section: GpxFileSection) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            sectionHeader(section.header)
            VStack(spacing: 0) {
                ForEach(Array(section.files.enumerated()), id: \.element.fileName) { index, file in
                    GpxFileCardView(
                        strings: strings,
                        file: file,
                        onOpen: {
                            viewModel.onEvent(event: GpxCollectionUiEventsFileClicked(file: file))
                        },
                        onRename: {},
                        onShare: {},
                        onDelete: {
                            viewModel.onEvent(event: GpxCollectionUiEventsDeleteClicked(file: file))
                        }
                    )
                    if index != section.files.count - 1 {
                        Divider().padding(.leading, 16)
                    }
                }
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }

    private func sectionHeader(_ header: GpxFileHeader) -> some View {
        Text(headerText(header))
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(.secondary)
            .textCase(.uppercase)
            .padding(.leading, 4)
    }

    private func headerText(_ header: GpxFileHeader) -> String {
        switch onEnum(of: header) {
        case .today:
            return strings.get(id: SharedRes.strings().gpx_collection_date_today)
        case .yesterday:
            return strings.get(id: SharedRes.strings().gpx_collection_date_yesterday)
        case .date(let date):
            return date.label
        }
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
        }
    }
}

private extension GpxFileSection {
    var stableId: String {
        switch onEnum(of: header) {
        case .today: return "today"
        case .yesterday: return "yesterday"
        case .date(let date): return "date_\(date.label)"
        }
    }
}
