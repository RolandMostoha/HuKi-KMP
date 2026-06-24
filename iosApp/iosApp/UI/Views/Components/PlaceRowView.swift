import Shared
import SwiftUI

struct PlaceRowView: View {
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
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.white)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(place.name)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(.primary)
                        .lineLimit(1)
                    if let address = place.address, !address.isEmpty {
                        Text(address)
                            .font(.system(size: 13))
                            .foregroundStyle(Color(.secondaryLabel))
                            .lineLimit(1)
                    }
                }
                Spacer(minLength: 0)
            }
            .padding(.leading, 16)
            .padding(.trailing, 8)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
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
        return OsmTypeMapperKt.toPlaceIconRes(osmType: place.osmType).toUIImage()!
    }
}

extension Place {
    var listIdentity: String { "\(osmId)#\(String(describing: osmType))" }
}
