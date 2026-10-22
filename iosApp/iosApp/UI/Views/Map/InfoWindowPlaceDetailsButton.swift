import Shared
import SwiftUI

struct InfoWindowPlaceDetailsButton: View {
    let strings: Strings
    let onClick: () -> Void

    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        Button(action: onClick) {
            Image(systemName: "arrow.triangle.turn.up.right.diamond.fill")
                .font(.title3)
                .foregroundStyle(primary)
                .frame(width: 44, height: 44)
                .background(Circle().fill(primary.opacity(0.15)))
                .contentShape(Circle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityLabel(strings.get(id: SharedRes.strings().okt_info_window_navigate))
    }
}

#Preview {
    InfoWindowPlaceDetailsButton(strings: Strings(), onClick: {})
        .padding()
}
