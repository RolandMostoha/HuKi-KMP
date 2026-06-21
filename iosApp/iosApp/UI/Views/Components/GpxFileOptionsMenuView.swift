import Shared
import SwiftUI

struct GpxFileOptionsMenuView: View {
    let strings: Strings
    let onRename: () -> Void
    let onShare: () -> Void
    let onDelete: () -> Void

    var body: some View {
        Menu {
            if FeatureFlags.shared.IS_GPX_RENAME_ENABLED {
                Button(action: onRename) {
                    Label(strings.get(id: SharedRes.strings().gpx_collection_action_rename), systemImage: "pencil")
                }
            }
            if FeatureFlags.shared.IS_GPX_SHARE_ENABLED {
                Button(action: onShare) {
                    Label(
                        strings.get(id: SharedRes.strings().gpx_collection_action_share),
                        systemImage: "square.and.arrow.up"
                    )
                }
            }
            Button(role: .destructive, action: onDelete) {
                Label(strings.get(id: SharedRes.strings().gpx_collection_action_delete), systemImage: "trash")
            }
        } label: {
            Image(systemName: "ellipsis")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(.primary)
                .frame(width: 36, height: 36)
                .contentShape(Rectangle())
        }
        .menuStyle(.button)
        .buttonStyle(.plain)
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM_OPTIONS_BUTTON)
        .accessibilityLabel(strings.get(id: SharedRes.strings().gpx_collection_a11y_options))
    }
}
