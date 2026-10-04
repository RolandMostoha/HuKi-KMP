import Shared
import SwiftUI

struct OktSheetView: View {
    let strings: Strings
    let okt: OktUiState
    let onSectionClicked: (String) -> Void
    let onSectionStartClicked: (String) -> Void
    let onSectionWebsiteClicked: (String) -> Void
    let onSectionReverseClicked: (String) -> Void
    let onStampClicked: (OktMarker) -> Void
    let onDismissRequest: () -> Void

    private let oktBlue = Color(SharedRes.colors().oktBlue.getUIColor())

    var body: some View {
        NavigationStack {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 4) {
                        ForEach(okt.sections, id: \.id) { section in
                            OktSectionRowView(
                                strings: strings,
                                section: section,
                                isSelected: section.id == okt.selectedSectionId,
                                isReversed: okt.isReversed(sectionId: section.id),
                                selectedMarkerId: okt.selectedMarkerId,
                                onClick: { onSectionClicked(section.id) },
                                onStartClick: { onSectionStartClicked(section.id) },
                                onWebsiteClick: { onSectionWebsiteClicked(section.id) },
                                onReverseClick: { onSectionReverseClicked(section.id) },
                                onStampClick: onStampClicked
                            )
                            .id(section.id)
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.bottom, 24)
                    .animation(.snappy, value: okt.selectedSectionId)
                }
                .onChange(of: okt.selectedSectionId) { _, sectionId in
                    withAnimation(.snappy) {
                        proxy.scrollTo(sectionId, anchor: .top)
                    }
                }
                .onAppear {
                    // The lazy rows are not laid out yet on appear
                    DispatchQueue.main.async {
                        proxy.scrollTo(okt.selectedSectionId, anchor: .top)
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    startButton
                }
                ToolbarItem(placement: .principal) {
                    title
                }
                ToolbarItem(placement: .topBarTrailing) {
                    closeButton
                }
            }
        }
        .accessibilityIdentifier(TestTags.shared.OKT_SHEET)
    }

    private var title: some View {
        VStack(spacing: 0) {
            Text(strings.get(id: okt.type.title))
                .font(.headline)
            Text(strings.get(id: okt.type.longSubtitle))
                .font(.footnote)
                .foregroundStyle(.secondary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isHeader)
    }

    @ViewBuilder
    private var startButton: some View {
        let button = Button {
            onSectionStartClicked(okt.selectedSectionId)
        } label: {
            Image(systemName: "play.fill")
        }
        .tint(oktBlue)
        .accessibilityLabel(strings.get(id: SharedRes.strings().okt_a11y_start))
        .accessibilityIdentifier(TestTags.shared.OKT_START_BUTTON)
        if #available(iOS 26, *) {
            button.buttonStyle(.glassProminent)
        } else {
            button.buttonStyle(.borderedProminent)
        }
    }

    @ViewBuilder
    private var closeButton: some View {
        if #available(iOS 26, *) {
            Button(role: .close, action: onDismissRequest)
                .accessibilityIdentifier(TestTags.shared.OKT_CLOSE_BUTTON)
        } else {
            Button(action: onDismissRequest) {
                Image(systemName: "xmark")
            }
            .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
            .accessibilityIdentifier(TestTags.shared.OKT_CLOSE_BUTTON)
        }
    }
}
