import Shared
import SwiftUI

struct RoutePlannerHintView: View {
    let strings: Strings
    let hint: RoutePlannerHint
    let onDismiss: () -> Void

    private let cornerRadius: CGFloat = 24

    var body: some View {
        HStack(spacing: 10) {
            GestureDemoFabView(systemImage: systemImage, gesture: .longClick)
            VStack(alignment: .leading, spacing: 2) {
                Text(strings.get(id: title))
                    .font(.subheadline.weight(.semibold))
                Text(strings.get(id: message))
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
        .glassBackground(.regular, in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_LONG_PRESS_HINT)
    }

    private var systemImage: String {
        switch hint {
        case .longPress: "hand.tap.fill"
        case .moveStop: "hand.draw.fill"
        }
    }

    private var title: StringResource {
        switch hint {
        case .longPress: SharedRes.strings().route_planner_long_press_hint_title
        case .moveStop: SharedRes.strings().route_planner_move_hint_title
        }
    }

    private var message: StringResource {
        switch hint {
        case .longPress: SharedRes.strings().route_planner_long_press_hint_message
        case .moveStop: SharedRes.strings().route_planner_move_hint_message
        }
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
        RoutePlannerHintView(strings: Strings(), hint: .moveStop, onDismiss: {})
            .padding()
    }
}
