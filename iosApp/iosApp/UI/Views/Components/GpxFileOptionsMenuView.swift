import Shared
import SwiftUI

struct GpxFileOptionsMenuView: View {
    let strings: Strings
    let isReplanVisible: Bool
    let onReplan: () -> Void
    let onRename: () -> Void
    let onShare: () -> Void
    let onDelete: () -> Void

    var body: some View {
        Menu {
            if isReplanVisible {
                Button(action: onReplan) {
                    Label(
                        strings.get(id: SharedRes.strings().gpx_collection_action_replan),
                        systemImage: "arrow.triangle.turn.up.right.diamond"
                    )
                }
                .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM_REPLAN_BUTTON)
            }
            if FeatureFlags.shared.IS_GPX_RENAME_ENABLED {
                Button(action: onRename) {
                    Label(strings.get(id: SharedRes.strings().gpx_collection_action_rename), systemImage: "pencil")
                }
            }
            Button(action: onShare) {
                Label(
                    strings.get(id: SharedRes.strings().gpx_collection_action_share),
                    systemImage: "square.and.arrow.up"
                )
            }
            Button(role: .destructive, action: onDelete) {
                Label(strings.get(id: SharedRes.strings().gpx_collection_action_delete), systemImage: "trash")
            }
        } label: {
            Image(systemName: "ellipsis")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(.primary)
                .frame(width: 36, height: 36)
                .background(Circle().fill(Color(.tertiarySystemFill)))
                .contentShape(Circle())
        }
        .menuStyle(.button)
        .buttonStyle(.plain)
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM_OPTIONS_BUTTON)
        .accessibilityLabel(strings.get(id: SharedRes.strings().gpx_collection_a11y_options))
    }
}
