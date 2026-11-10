import Shared
import SwiftUI

struct SupportAnimalBadgesView: View {
    let ringColor: SwiftUI.Color
    var size: CGFloat = 42

    private let animals = SupportAnimal.allCases

    var body: some View {
        HStack(spacing: -size * 0.19) {
            ForEach(Array(animals.enumerated()), id: \.element) { index, animal in
                animal.iconImage
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(animal.color)
                    .frame(width: size * 0.62, height: size * 0.62)
                    .frame(width: size, height: size)
                    .background(animal.containerColor, in: .circle)
                    .background(ringColor, in: Circle().inset(by: -3))
                    .zIndex(Double(animals.count - index))
            }
        }
        .padding(.leading, 3)
        .accessibilityHidden(true)
    }
}

#Preview {
    SupportAnimalBadgesView(ringColor: SwiftUI.Color(.secondarySystemGroupedBackground))
        .padding()
        .background(SwiftUI.Color(.secondarySystemGroupedBackground))
}
