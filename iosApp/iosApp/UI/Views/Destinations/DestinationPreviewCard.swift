import Shared
import SwiftUI

struct DestinationPreviewCard: View {
    let strings: Strings
    let destination: Destination
    var landscapeText: String?
    var distanceText: String?

    var body: some View {
        header
            .background(SwiftUI.Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top, spacing: 8) {
                typeChip
                Spacer(minLength: 8)
                if let distanceText {
                    distanceChip(distanceText)
                }
            }
            Spacer(minLength: 0)
                .frame(height: 56)
            textBlock
            description
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .topLeading)
        .background {
            ZStack {
                typeColor
                gradientOverlay
            }
        }
    }

    private var typeChip: some View {
        HStack(spacing: 6) {
            Image(uiImage: destination.type.iconRes.toUIImage()!)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 16, height: 16)
                .foregroundStyle(chipTint)
            Text(strings.get(id: destination.type.title))
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(chipTint)
                .lineLimit(1)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 7)
        .background(SwiftUI.Color(.systemBackground), in: .capsule)
    }

    private func distanceChip(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 13, weight: .bold))
            .foregroundStyle(chipTint)
            .lineLimit(1)
            .padding(.horizontal, 12)
            .padding(.vertical, 7)
            .background(SwiftUI.Color(.systemBackground), in: .capsule)
    }

    private var textBlock: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(destination.name)
                .font(.system(size: 24, weight: .bold))
                .foregroundStyle(.white)
                .lineLimit(2)
                .minimumScaleFactor(0.7)
            HStack(alignment: .center, spacing: 4) {
                Image(systemName: "mappin.and.ellipse")
                    .font(.system(size: 13, weight: .medium))
                Text(subtitle)
                    .font(.system(size: 13, weight: .medium))
                    .lineLimit(1)
                    .padding(.leading, 2)
            }
            .foregroundStyle(.white.opacity(0.9))
        }
    }

    private var subtitle: String {
        if let landscapeText {
            return "\(destination.town) · \(landscapeText)"
        }
        return destination.town
    }

    private var description: some View {
        Text(strings.get(id: destination.description_))
            .font(.system(size: 14))
            .foregroundStyle(.white.opacity(0.9))
            .fixedSize(horizontal: false, vertical: true)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 14)
    }

    private var typeColor: SwiftUI.Color {
        SwiftUI.Color(destination.type.colorRes.getUIColor())
    }

    private var chipTint: SwiftUI.Color {
        SwiftUI.Color(destination.type.colorRes.getUIColor().categoryTint())
    }

    private var gradientOverlay: some View {
        LinearGradient(
            colors: [.clear, .black],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}
