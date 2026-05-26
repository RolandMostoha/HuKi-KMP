import Shared
import SwiftUI

struct SettingsItemView<Icon: View>: View {
    let icon: Icon
    let title: String
    var value: String?
    var description: String?
    var descriptionColor: SwiftUI.Color?
    var showIconBackground: Bool = true
    let accessibilityLabel: String
    let testTag: String
    let action: () -> Void

    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                ZStack {
                    if showIconBackground {
                        Circle()
                            .fill(Color(.systemGray5))
                            .frame(width: 40, height: 40)
                    }
                    icon
                }
                .frame(width: 40, height: 40)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.body)
                        .foregroundStyle(.primary)
                    if let description {
                        Text(description)
                            .font(.caption)
                            .foregroundStyle(descriptionColor ?? primary)
                    }
                }
                Spacer(minLength: 8)
                if let value {
                    Text(value)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 13)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityIdentifier(testTag)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel)
    }
}
