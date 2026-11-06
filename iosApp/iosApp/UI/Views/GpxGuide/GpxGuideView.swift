import Shared
import SwiftUI

enum GpxGuideRoute: Hashable {
    case gpxGuide
}

struct GpxGuideView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var holder = ViewModelHolder(KoinViewModelProvider.shared.getGpxGuideViewModel)
    private var viewModel: GpxGuideViewModel { holder.viewModel }

    private let strings = Strings()
    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let primaryContainer = Color(SharedRes.colors().primaryContainer.getUIColor())

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                whatIsCard
                stepsSection
            }
            .padding(.top, 8)
            .padding(.bottom, 24)
            .readableWidth()
        }
        .background(Color(.systemGroupedBackground))
        .task {
            viewModel.onEvent(event: GpxGuideUiEventsScreenViewed.shared)
        }
        .accessibilityIdentifier(TestTags.shared.GPX_GUIDE_SCREEN_ROOT)
        .navigationTitle(strings.get(id: SharedRes.strings().gpx_guide_title))
        .navigationBarTitleDisplayMode(.large)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) { backButton }
        }
    }

    private var backButton: some View {
        Button(
            action: { dismiss() },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().gpx_guide_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.GPX_GUIDE_BACK_BUTTON)
    }

    private var whatIsCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 10) {
                iconChip
                Text(strings.get(id: SharedRes.strings().gpx_guide_what_title))
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
            }
            Text(strings.get(id: SharedRes.strings().gpx_guide_what_message))
                .font(.footnote)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(14)
        .background(
            in: RoundedRectangle(cornerRadius: 16, style: .continuous)
        )
        .padding(.horizontal, 16)
    }

    private var iconChip: some View {
        RoundedRectangle(cornerRadius: 11, style: .continuous)
            .fill(primaryContainer)
            .frame(width: 38, height: 38)
            .overlay {
                Image(SharedRes.images().ic_gpx.assetImageName, bundle: SharedRes.images().ic_gpx.bundle)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 20, height: 20)
                    .foregroundStyle(primary)
            }
    }

    private var stepsSection: some View {
        GuideSectionView(title: strings.get(id: SharedRes.strings().gpx_guide_steps_section)) {
            GuideStepView(
                title: strings.get(id: SharedRes.strings().gpx_guide_step_1_title),
                message: strings.get(id: SharedRes.strings().gpx_guide_step_1_message),
                leading: { GuideStepBadgeView(number: 1) },
                content: {
                    recommendationsRow
                        .padding(.top, 8)
                }
            )
            GuideStepDividerView()
            GuideStepView(
                title: strings.get(id: SharedRes.strings().gpx_guide_step_2_title),
                message: strings.get(id: SharedRes.strings().gpx_guide_step_2_message),
                leading: { GuideStepBadgeView(number: 2) }
            )
            GuideStepDividerView()
            GuideStepView(
                title: strings.get(id: SharedRes.strings().gpx_guide_step_3_title),
                message: strings.get(id: SharedRes.strings().gpx_guide_step_3_message),
                leading: { GuideStepBadgeView(number: 3) }
            )
            GuideStepDividerView()
            GuideStepView(
                title: strings.get(id: SharedRes.strings().gpx_guide_step_4_title),
                message: strings.get(id: SharedRes.strings().gpx_guide_step_4_message),
                leading: { GuideStepBadgeView(number: 4) }
            )
        }
    }

    private var recommendationsRow: some View {
        HStack(spacing: 6) {
            ForEach(HikeRecommendation.allCases, id: \.self) { recommendation in
                HikeRecommendationCardView(
                    strings: strings,
                    recommendation: recommendation,
                    onClick: { open(recommendation.baseUrl) }
                )
            }
        }
    }

    private func open(_ urlString: String) {
        if let url = URL(string: urlString) {
            UIApplication.shared.open(url)
        }
    }
}
