import Shared
import SwiftUI

struct SectionHeaderView: View {
    let title: String
    var actionText: String?
    var onActionClick: (() -> Void)?
    var actionAccessibilityId: String?

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(title)
                .font(.system(size: 24, weight: .bold))
                .foregroundStyle(.primary)
            Spacer()
            if let actionText, let onActionClick {
                Button(action: onActionClick) {
                    Text(actionText)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(SwiftUI.Color(SharedRes.colors().primary.getUIColor()))
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier(actionAccessibilityId ?? "")
            }
        }
        .padding(.horizontal, 16)
    }
}

#Preview {
    SectionHeaderView(
        title: "Recent GPX files",
        actionText: "See all",
        onActionClick: {},
        actionAccessibilityId: TestTags.shared.RECENT_GPX_SEE_ALL_BUTTON
    )
}
