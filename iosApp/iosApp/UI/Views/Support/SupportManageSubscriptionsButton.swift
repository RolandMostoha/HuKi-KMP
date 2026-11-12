import Shared
import StoreKit
import SwiftUI

struct SupportManageSubscriptionsButton: View {
    let onClick: () -> Void
    let onDismiss: () -> Void

    @State private var isSheetPresented = false

    private let strings = Strings()

    var body: some View {
        Button(strings.get(id: SharedRes.strings().support_manage_subscriptions)) {
            onClick()
            isSheetPresented = true
        }
        .font(.subheadline.weight(.semibold))
        .manageSubscriptionsSheet(isPresented: $isSheetPresented)
        .onChange(of: isSheetPresented) { _, isPresented in
            if !isPresented {
                onDismiss()
            }
        }
        .accessibilityIdentifier(TestTags.shared.SUPPORT_MANAGE_SUBSCRIPTIONS)
    }
}

#Preview {
    SupportManageSubscriptionsButton(onClick: {}, onDismiss: {})
}
