import SwiftUI

struct OktBadgeText: View {
    let prefix: String
    let number: String?
    let fontSize: CGFloat

    var body: some View {
        Text(number.map { "\(prefix)\n\($0)" } ?? prefix)
            .font(.system(size: fontSize, weight: .heavy))
            .multilineTextAlignment(.center)
            .lineLimit(2)
            .minimumScaleFactor(0.6)
    }
}

#Preview {
    HStack(spacing: 16) {
        OktBadgeText(prefix: "RPDDK", number: "09", fontSize: 12)
        OktBadgeText(prefix: "OKT", number: "04", fontSize: 12)
        OktBadgeText(prefix: "AKT", number: nil, fontSize: 14)
    }
    .padding()
}
