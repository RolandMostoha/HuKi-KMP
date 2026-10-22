import Shared
import SwiftUI

struct LandscapesResumeButton: View {
    let strings: Strings
    let grouping: LandscapesGrouping
    let onClick: () -> Void

    private static let imageWidth: CGFloat = 34

    private var image: Image {
        grouping == .category
            ? SharedRes.images().ic_destinations_by_category.swiftUIImage
            : SharedRes.images().ic_hungary_colored.swiftUIImage
    }

    var body: some View {
        Button(action: onClick) {
            image
                .renderingMode(.original)
                .resizable()
                .scaledToFit()
                .frame(width: Self.imageWidth)
                .frame(width: Dimens.oktStopFabSize, height: Dimens.oktStopFabSize)
                .contentShape(Circle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .glassBackground(.regular, in: Circle(), interactive: true)
        .accessibilityLabel(
            strings.get(
                id: grouping == .category
                    ? SharedRes.strings().categories_a11y_resume
                    : SharedRes.strings().landscapes_a11y_resume
            )
        )
        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_RESUME_FAB)
    }
}

#Preview {
    HStack {
        LandscapesResumeButton(strings: Strings(), grouping: .landscape, onClick: {})
        LandscapesResumeButton(strings: Strings(), grouping: .category, onClick: {})
    }
    .padding()
}
