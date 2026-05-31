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
                        .fill(backgroundColor)
                        .frame(width: 40, height: 40)
                    Image(uiImage: iconImage)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(.white)
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
                if let distance = place.distance, !distance.isEmpty {
                    Text(distance)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(TestTags.shared.SEARCH_RESULT_ITEM)
    }

    private var backgroundColor: SwiftUI.Color {
        if let category = place.placeCategory {
            return SwiftUI.Color(category.categoryColorRes.getUIColor())
        }
        return SwiftUI.Color(SharedRes.colors().colorPlaceCategoryFallback.getUIColor())
    }

    private var iconImage: UIImage {
        if let category = place.placeCategory {
            return category.iconRes.toUIImage()!
        }
        return osmIconImage(for: place.osmType)
    }

    private func osmIconImage(for osmType: OsmType?) -> UIImage {
        switch osmType {
        case .way:
            return SharedRes.images().ic_place_type_way.toUIImage()!
        case .relation:
            return SharedRes.images().ic_place_type_relation.toUIImage()!
        case .node, .none:
            return SharedRes.images().ic_place_type_node.toUIImage()!
        }
    }
}
