import Shared
import SwiftUI

struct OktResumeButton: View {
    let strings: Strings
    let section: OktSectionItem
    let onClick: () -> Void

    private let oktBlue = Color(SharedRes.colors().oktBlue.getUIColor())

    var body: some View {
        Button(action: onClick) {
            OktBadgeText(prefix: section.badgePrefix, number: section.badgeNumber, fontSize: 14)
                .foregroundStyle(oktBlue)
                .padding(.horizontal, 6)
                .frame(width: Dimens.oktStopFabSize, height: Dimens.oktStopFabSize)
                .contentShape(Circle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .glassBackground(.regular, in: Circle(), interactive: true)
        .accessibilityLabel(strings.get(id: SharedRes.strings().okt_a11y_resume, args: [section.name]))
        .accessibilityIdentifier(TestTags.shared.OKT_RESUME_FAB)
    }
}
