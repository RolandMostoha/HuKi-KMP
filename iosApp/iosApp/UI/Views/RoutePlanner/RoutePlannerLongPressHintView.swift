import Shared
import SwiftUI

struct RoutePlannerLongPressHintView: View {
    let strings: Strings
    let onDismiss: () -> Void

    private let cornerRadius: CGFloat = 24

    var body: some View {
        HStack(spacing: 10) {
            GestureDemoFabView(systemImage: "hand.tap.fill", gesture: .longClick)
            VStack(alignment: .leading, spacing: 2) {
                Text(strings.get(id: SharedRes.strings().route_planner_long_press_hint_title))
                    .font(.subheadline.weight(.semibold))
                Text(strings.get(id: SharedRes.strings().route_planner_long_press_hint_message))
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            closeButton
        }
        .padding(.leading, 6)
        .padding(.trailing, 12)
        .padding(.vertical, 6)
        .frame(maxWidth: 480)
        .glassBackground(.regular, in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_LONG_PRESS_HINT)
    }

    private var closeButton: some View {
        GlassIconButton(
            systemName: "xmark",
            accessibilityLabel: strings.get(id: SharedRes.strings().route_planner_a11y_dismiss_long_press_hint),
            accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_LONG_PRESS_HINT_CLOSE,
            fontSize: 14,
            padding: 8,
            action: onDismiss
        )
    }
}

#Preview {
    ZStack(alignment: .bottom) {
        Color.green.opacity(0.3).ignoresSafeArea()
        RoutePlannerLongPressHintView(strings: Strings(), onDismiss: {})
            .padding()
    }
}
