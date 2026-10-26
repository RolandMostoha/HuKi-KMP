import Shared
import SwiftUI

struct MapLabelScaleSheetView: View {
    let strings: Strings
    let labelScale: MapLabelScale
    let onFactorChanged: (Float) -> Void
    let onFactorCommitted: (Float) -> Void
    let onDismissRequest: () -> Void

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var draggedFactor: Double?
    @State private var isEditing = false
    @State private var contentHeight = Dimens.mapLabelScaleDetentHeight

    private var displayedFactor: Double {
        draggedFactor ?? Double(labelScale.effectiveFactor(systemScale: dynamicTypeSize.mapboxSymbolScale))
    }

    private var percentText: String {
        let percent = MapLabelScale.companion.toPercent(factor: Float(displayedFactor))
        return strings.get(id: SharedRes.strings().map_label_scale_percent, args: [percent])
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text(strings.get(id: SharedRes.strings().map_label_scale_title))
                        .font(.title2)
                        .fontWeight(.bold)
                    Spacer()
                    CloseButton(
                        action: onDismissRequest,
                        accessibilityIdentifier: TestTags.shared.MAP_LABEL_SCALE_CLOSE_BUTTON
                    )
                }
                HStack(spacing: 12) {
                    Image(systemName: "textformat.size.smaller")
                        .accessibilityHidden(true)
                    Slider(
                        value: Binding(
                            get: { displayedFactor },
                            set: { updateFactor($0) }
                        ),
                        in: Double(MapLabelScale.companion.MIN_FACTOR)...Double(MapLabelScale.companion.MAX_FACTOR),
                        step: 1 / Double(MapLabelScale.companion.FACTOR_STEPS),
                        onEditingChanged: onEditingChanged
                    )
                    .accessibilityLabel(strings.get(id: SharedRes.strings().map_label_scale_title))
                    .accessibilityValue(percentText)
                    .accessibilityIdentifier(TestTags.shared.MAP_LABEL_SCALE_SLIDER)
                    Image(systemName: "textformat.size.larger")
                        .font(.title2)
                        .accessibilityHidden(true)
                    Text(percentText)
                        .font(.body.monospacedDigit())
                        .frame(minWidth: SharedDimens.shared.MAP_LABEL_SCALE_PERCENT_MIN_WIDTH, alignment: .trailing)
                        .accessibilityHidden(true)
                }
                Label(
                    strings.get(id: SharedRes.strings().map_label_scale_info),
                    systemImage: "info.circle"
                )
                .font(.footnote)
                .foregroundStyle(.secondary)
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)
            .padding(.bottom, 8)
            .onGeometryChange(for: CGFloat.self, of: { $0.size.height }, action: { contentHeight = $0 })
        }
        .scrollBounceBehavior(.basedOnSize)
        .accessibilityIdentifier(TestTags.shared.MAP_LABEL_SCALE_SHEET)
        .presentationDetents([.height(contentHeight)])
        .presentationBackgroundInteraction(.enabled(upThrough: .height(contentHeight)))
    }

    private func updateFactor(_ factor: Double) {
        draggedFactor = factor
        onFactorChanged(Float(factor))
        // VoiceOver adjustments change the value without an editing session
        if !isEditing {
            commit()
        }
    }

    private func onEditingChanged(_ editing: Bool) {
        isEditing = editing
        if !editing {
            commit()
        }
    }

    private func commit() {
        guard let factor = draggedFactor else { return }
        onFactorCommitted(Float(factor))
        draggedFactor = nil
    }
}

#Preview("Phone text size") {
    MapLabelScaleSheetView(
        strings: Strings(),
        labelScale: MapLabelScale.companion.DEFAULT,
        onFactorChanged: { _ in },
        onFactorCommitted: { _ in },
        onDismissRequest: {}
    )
}

#Preview("Custom") {
    MapLabelScaleSheetView(
        strings: Strings(),
        labelScale: MapLabelScale(followSystem: false, factor: 1.5),
        onFactorChanged: { _ in },
        onFactorCommitted: { _ in },
        onDismissRequest: {}
    )
    .preferredColorScheme(.dark)
}
