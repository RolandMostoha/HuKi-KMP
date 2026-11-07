import Shared
import SwiftUI

struct SupportLegalLinksView: View {
    @Environment(\.openURL) private var openURL

    private let strings = Strings()

    var body: some View {
        HStack(spacing: 24) {
            link(
                title: SharedRes.strings().support_terms_of_use,
                url: SharedRes.strings().support_terms_url_ios,
                a11yLabel: SharedRes.strings().support_a11y_open_terms_of_use,
                testTag: TestTags.shared.SUPPORT_TERMS_OF_USE
            )
            link(
                title: SharedRes.strings().menu_item_privacy_policy,
                url: SharedRes.strings().menu_privacy_policy_url,
                a11yLabel: SharedRes.strings().menu_a11y_open_privacy_policy,
                testTag: TestTags.shared.SUPPORT_PRIVACY_POLICY
            )
        }
        .font(.footnote.weight(.semibold))
        .frame(maxWidth: .infinity)
        .padding(.top, 20)
    }

    private func link(
        title: StringResource,
        url: StringResource,
        a11yLabel: StringResource,
        testTag: String
    ) -> some View {
        Button(strings.get(id: title)) {
            if let destination = URL(string: strings.get(id: url)) {
                openURL(destination)
            }
        }
        .accessibilityLabel(strings.get(id: a11yLabel))
        .accessibilityIdentifier(testTag)
    }
}

#Preview {
    SupportLegalLinksView()
        .padding()
        .background(SwiftUI.Color(.systemGroupedBackground))
}
