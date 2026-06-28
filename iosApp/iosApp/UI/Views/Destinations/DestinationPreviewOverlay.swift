import Shared
import SwiftUI

struct DestinationPreviewOverlay: View {
    let strings: Strings
    let destination: Destination
    var landscapeText: String?
    var distanceText: String?
    let onShowOnMap: () -> Void
    let onDismiss: () -> Void

    @State private var appeared = false

    var body: some View {
        ZStack {
            backdrop
            card
                .scaleEffect(appeared ? 1 : 0.85)
                .opacity(appeared ? 1 : 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .presentationBackground(.clear)
        .onAppear {
            withAnimation(.spring(response: 0.35, dampingFraction: 0.82)) { appeared = true }
        }
    }

    private var backdrop: some View {
        SwiftUI.Color.clear
            .glassBackground(in: Rectangle())
            .overlay(SwiftUI.Color.black.opacity(0.5))
            .ignoresSafeArea()
            .opacity(appeared ? 1 : 0)
            .contentShape(Rectangle())
            .onTapGesture { dismiss() }
            .accessibilityLabel(strings.get(id: SharedRes.strings().destinations_a11y_preview_dismiss))
            .accessibilityIdentifier(TestTags.shared.DESTINATION_PREVIEW_BACKDROP)
    }

    private var card: some View {
        VStack(spacing: 14) {
            DestinationPreviewCard(
                strings: strings,
                destination: destination,
                landscapeText: landscapeText,
                distanceText: distanceText
            )
            .shadow(color: .black.opacity(0.35), radius: 28, y: 14)
            DestinationPreviewContextMenu(
                strings: strings,
                onShowOnMap: { dismiss(then: onShowOnMap) }
            )
            .padding(.horizontal, 32)
            .shadow(color: .black.opacity(0.25), radius: 20, y: 10)
        }
        .padding(.horizontal, 24)
        .frame(maxWidth: 460)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.DESTINATION_PREVIEW)
    }

    private func dismiss(then action: (() -> Void)? = nil) {
        withAnimation(.easeOut(duration: 0.2)) { appeared = false }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            onDismiss()
            action?()
        }
    }
}
