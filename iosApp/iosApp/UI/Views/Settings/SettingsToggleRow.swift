import SwiftUI

struct SettingsToggleRow: View {
    let title: String
    let description: String
    let isOn: Bool
    let testTag: String
    let onToggle: (Bool) -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                Text(description)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Toggle(
                title,
                isOn: Binding(
                    get: { isOn },
                    set: { onToggle($0) }
                )
            )
            .labelsHidden()
            .accessibilityIdentifier(testTag)
        }
    }
}

#Preview {
    Form {
        SettingsToggleRow(
            title: "Zoom controls",
            description: "Always Show + / − buttons on the map",
            isOn: true,
            testTag: "",
            onToggle: { _ in }
        )
        SettingsToggleRow(
            title: "Map rotation",
            description: "Rotate the map with two fingers",
            isOn: false,
            testTag: "",
            onToggle: { _ in }
        )
    }
}
