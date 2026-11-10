import Shared
import SwiftUI

struct SupportContactView: View {
    let cardColor: SwiftUI.Color

    private let strings = Strings()
    private let primary = SwiftUI.Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(strings.get(id: SharedRes.strings().support_contact_message))
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.horizontal, 6)
                .padding(.top, 26)
                .padding(.bottom, 10)
            Button(
                action: {
                    EmailNavigator.composeEmail(
                        to: strings.get(id: SharedRes.strings().menu_contact_email),
                        subject: strings.get(id: SharedRes.strings().menu_email_subject_ios)
                    )
                },
                label: {
                    HStack(spacing: 12) {
                        Image(systemName: "envelope.fill")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 30, height: 30)
                            .background(primary, in: RoundedRectangle(cornerRadius: 8, style: .continuous))
                        Text(strings.get(id: SharedRes.strings().menu_contact_email))
                            .font(.body)
                            .foregroundStyle(primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Image(systemName: "chevron.forward")
                            .font(.footnote.weight(.semibold))
                            .foregroundStyle(.tertiary)
                    }
                    .padding(.horizontal, 16)
                    .frame(minHeight: 54)
                    .background(cardColor, in: RoundedRectangle(cornerRadius: 26, style: .continuous))
                    .contentShape(RoundedRectangle(cornerRadius: 26, style: .continuous))
                }
            )
            .buttonStyle(.plain)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(strings.get(id: SharedRes.strings().support_a11y_send_email))
            .accessibilityValue(strings.get(id: SharedRes.strings().menu_contact_email))
            .accessibilityAddTraits(.isButton)
            .accessibilityIdentifier(TestTags.shared.SUPPORT_EMAIL_ROW)
        }
    }
}

#Preview {
    SupportContactView(cardColor: SwiftUI.Color(.secondarySystemGroupedBackground))
        .padding()
        .background(SwiftUI.Color(.systemGroupedBackground))
}
