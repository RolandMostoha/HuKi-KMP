import Shared
import SwiftUI

struct MenuItemView<Icon: View>: View {
    let icon: Icon
    let title: String
    var value: String?
    var description: String?
    var showIconBackground: Bool = true
    var iconBackgroundColor: SwiftUI.Color?
    let accessibilityLabel: String
    let testTag: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                ZStack {
                    if showIconBackground {
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(iconBackgroundColor ?? Color(.systemGray5))
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
                            .foregroundStyle(.secondary)
                    }
                }
                Spacer(minLength: 8)
                if let value {
                    Text(value)
                        .font(.caption.weight(.light))
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
