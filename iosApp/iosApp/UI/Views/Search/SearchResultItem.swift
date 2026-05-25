import Shared
import SwiftUI

struct SearchResultItem: View {
    let place: Place
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(Color(SharedRes.colors().primaryContainer.getUIColor()))
                        .frame(width: 40, height: 40)
                    Image(systemName: "mappin")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(place.title)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(.primary)
                        .lineLimit(1)
                    if let subtitle = place.subtitle, !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.system(size: 14))
                            .foregroundStyle(Color(.secondaryLabel))
                            .lineLimit(2)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Text("2 km")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(TestTags.shared.SEARCH_RESULT_ITEM)
    }
}
