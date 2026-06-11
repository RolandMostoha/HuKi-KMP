import Shared
import SwiftUI

struct SearchBarView: View {
    let strings: Strings
    let onSearchTap: () -> Void
    let onSettingsClick: () -> Void

    var body: some View {
        HStack {
            searchField
            menuButton
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 14)
        .glassBackground()
        .accessibilityIdentifier(TestTags.shared.MAIN_SEARCH_BAR)
    }

    @ViewBuilder
    private var searchField: some View {
        Button(action: onSearchTap) {
            Text(strings.get(id: SharedRes.strings().search_input_placeholder))
                .font(.system(size: 16, weight: .regular))
                .foregroundStyle(Color(.secondaryLabel))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.leading, 48)
                .padding(.trailing, 16)
                .padding(.vertical, 12)
                .background(Color(.systemBackground).opacity(0.8), in: .capsule)
                .overlay(alignment: .leading) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(.primary)
                        .padding(.leading, 18)
                }
                .contentShape(Capsule())
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }

    @ViewBuilder
    private var menuButton: some View {
        Button(action: onSettingsClick) {
            Image(systemName: "line.3.horizontal")
                .font(.system(size: 22, weight: .semibold))
                .foregroundColor(.primary)
                .padding(.horizontal, 12)
                .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_settings))
    }
}
