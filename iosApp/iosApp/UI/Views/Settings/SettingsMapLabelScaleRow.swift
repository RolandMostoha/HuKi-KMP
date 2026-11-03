import Shared
import SwiftUI

struct SettingsMapLabelScaleRow: View {
    let title: String
    let description: String
    let value: String
    let onClick: () -> Void

    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        Button(
            action: onClick,
            label: {
                HStack(spacing: 12) {
                    Image(systemName: "textformat.size")
                        .font(.body.weight(.semibold))
                        .foregroundStyle(.white)
                        .frame(width: 30, height: 30)
                        .background(primary, in: RoundedRectangle(cornerRadius: 7, style: .continuous))
                        .accessibilityHidden(true)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(title)
                            .fontWeight(.medium)
                        Text(description)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Spacer()
                    Text(value)
                        .font(.headline)
                        .foregroundStyle(primary)
                    Image(systemName: "chevron.forward")
                        .font(.footnote.weight(.semibold))
                        .foregroundStyle(.tertiary)
                        .accessibilityHidden(true)
                }
                .contentShape(Rectangle())
            }
        )
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(TestTags.shared.SETTINGS_MAP_LABEL_SCALE_ROW)
    }
}

#Preview {
    Form {
        Section("Map labels") {
            SettingsMapLabelScaleRow(
                title: "Map text size",
                description: "Size of place names, peaks and trail labels",
                value: "120%",
                onClick: {}
            )
        }
    }
}
