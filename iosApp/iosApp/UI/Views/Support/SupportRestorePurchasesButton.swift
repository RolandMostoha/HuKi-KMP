import Shared
import SwiftUI

struct SupportRestorePurchasesButton: View {
    let isRestoring: Bool
    let onClick: () -> Void

    private let strings = Strings()

    var body: some View {
        HStack(spacing: 8) {
            Button(strings.get(id: SharedRes.strings().support_restore_purchases), action: onClick)
                .font(.subheadline.weight(.semibold))
                .disabled(isRestoring)
                .accessibilityIdentifier(TestTags.shared.SUPPORT_RESTORE_PURCHASES)
            if isRestoring {
                ProgressView()
                    .controlSize(.small)
                    .accessibilityLabel(strings.get(id: SharedRes.strings().support_a11y_restoring))
                    .transition(.opacity)
            }
        }
        .animation(.default, value: isRestoring)
    }
}

#Preview {
    VStack(alignment: .leading, spacing: 16) {
        SupportRestorePurchasesButton(isRestoring: false, onClick: {})
        SupportRestorePurchasesButton(isRestoring: true, onClick: {})
    }
}
