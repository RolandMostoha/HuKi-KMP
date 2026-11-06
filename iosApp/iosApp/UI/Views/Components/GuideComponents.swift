import Shared
import SwiftUI

struct GuideSectionTitleView: View {
    let text: String

    var body: some View {
        Text(text.uppercased())
            .font(.caption.weight(.bold))
            .tracking(0.8)
            .foregroundStyle(.primary.opacity(0.4))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.leading, 4)
            .padding(.bottom, 2)
    }
}

struct GuideSectionView<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            GuideSectionTitleView(text: title)
            VStack(spacing: 0, content: content)
                .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(.horizontal, 16)
        .padding(.top, 20)
    }
}

struct GuideStepView<Leading: View, Content: View>: View {
    let title: String
    let message: String
    @ViewBuilder let leading: () -> Leading
    @ViewBuilder var content: () -> Content

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            leading()
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
                Text(message)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                content()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(14)
    }
}

extension GuideStepView where Content == EmptyView {
    init(title: String, message: String, @ViewBuilder leading: @escaping () -> Leading) {
        self.init(title: title, message: message, leading: leading, content: { EmptyView() })
    }
}

struct GuideStepBadgeView: View {
    let number: Int

    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        ZStack {
            Circle()
                .fill(primary)
                .frame(width: 24, height: 24)
            Text("\(number)")
                .font(.caption.weight(.bold))
                .foregroundStyle(.white)
        }
        .accessibilityHidden(true)
    }
}

struct GuideStepDividerView: View {
    var leadingInset: CGFloat = 50

    var body: some View {
        Divider().padding(.leading, leadingInset)
    }
}

#Preview {
    ScrollView {
        GuideSectionView(title: "How to import GPX into HuKi") {
            GuideStepView(
                title: "Browse a hike collection website",
                message: "Look for a GPX export option near the route map.",
                leading: { GuideStepBadgeView(number: 1) }
            )
            GuideStepDividerView()
            GuideStepView(
                title: "Open the file with HuKi",
                message: "Tap the GPX file, choose Share, then pick HuKi.",
                leading: { GuideStepBadgeView(number: 2) }
            )
        }
    }
    .background(Color(.systemGroupedBackground))
}
