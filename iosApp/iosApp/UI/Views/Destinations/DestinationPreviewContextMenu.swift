import Shared
import SwiftUI

struct DestinationPreviewContextMenu: View {
    let strings: Strings
    let onShowOnMap: () -> Void

    private let shape = RoundedRectangle(cornerRadius: 26, style: .continuous)

    var body: some View {
        menuContent.glassBackground(.regular, in: shape)
    }

    private var menuContent: some View {
        VStack(spacing: 0) {
            actionRow(
                title: strings.get(id: SharedRes.strings().destinations_action_show_on_map),
                icon: "map.fill",
                action: onShowOnMap
            )
            .accessibilityIdentifier(TestTags.shared.DESTINATION_PREVIEW_SHOW_ON_MAP_BUTTON)
        }
        .padding(.vertical, 6)
        .contentShape(shape)
    }

    private func actionRow(
        title: String,
        icon: String,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(.primary)
                    .frame(width: 26)
                Text(title)
                    .font(.system(size: 17, weight: .regular))
                    .foregroundStyle(.primary)
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }
}
