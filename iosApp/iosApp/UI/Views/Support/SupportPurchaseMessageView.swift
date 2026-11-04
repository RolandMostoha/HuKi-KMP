import Shared
import SwiftUI

struct SupportPurchaseMessageView: View {
    let purchaseMessage: SupportPurchaseMessage
    let onDismiss: () -> Void

    private let strings = Strings()

    var body: some View {
        InfoView(
            strings: strings,
            infoViewData: infoViewData,
            primaryActionText: strings.get(id: SharedRes.strings().support_purchase_message_dismiss),
            onPrimaryActionClick: onDismiss
        )
        .accessibilityIdentifier(TestTags.shared.SUPPORT_PURCHASE_MESSAGE)
    }
}

private extension SupportPurchaseMessageView {
    var infoViewData: InfoViewData {
        switch purchaseMessage {
        case .pending:
            InfoViewData(
                infoViewType: .info,
                icon: SharedRes.images().ic_schedule,
                title: SharedRes.strings().support_purchase_pending_title,
                message: SharedRes.strings().support_purchase_pending_message
            )
        case .scheduled:
            InfoViewData(
                infoViewType: .info,
                icon: SharedRes.images().ic_schedule,
                title: SharedRes.strings().support_purchase_scheduled_title,
                message: SharedRes.strings().support_purchase_scheduled_message
            )
        case .failed:
            InfoViewData(
                infoViewType: .error,
                icon: SharedRes.images().ic_error,
                title: SharedRes.strings().support_purchase_failed_title,
                message: SharedRes.strings().support_purchase_failed_message
            )
        }
    }
}

#Preview {
    SupportPurchaseMessageView(purchaseMessage: .pending, onDismiss: {})
        .padding()
}
