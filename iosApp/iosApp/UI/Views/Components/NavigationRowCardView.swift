import Shared
import SwiftUI

struct NavigationRowCardView: View {
    let systemImage: String
    let title: String
    let subtitle: String
    let onClick: () -> Void

    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let primaryContainer = Color(SharedRes.colors().primaryContainer.getUIColor())

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 16) {
                Image(systemName: systemImage)
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(primary)
                    .frame(width: 40, height: 40)
                    .background(primaryContainer, in: Circle())
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.body.weight(.semibold))
                        .foregroundStyle(.primary)
                    Text(subtitle)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Image(systemName: "chevron.right")
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(.tertiary)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(Color(.tertiarySystemFill), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isButton)
    }
}

#Preview {
    NavigationRowCardView(
        systemImage: "mappin.and.ellipse",
        title: "Browse destinations",
        subtitle: "Nearly 400 hand-picked places",
        onClick: {}
    )
    .padding()
    .background(Color(.systemGray6))
}
