import Shared
import SwiftUI

enum GpxTutorialRoute: Hashable {
    case gpxTutorial
}

struct GpxTutorialView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel = KoinViewModelProvider.shared.getGpxTutorialViewModel()

    private let strings = Strings()
    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let primaryContainer = Color(SharedRes.colors().primaryContainer.getUIColor())

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                header
                whatIsCard
                    .padding(.top, 4)
                stepsSection
            }
            .padding(.bottom, 24)
        }
        .background(Color(.systemGroupedBackground))
        .accessibilityIdentifier(TestTags.shared.GPX_TUTORIAL_SCREEN_ROOT)
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) { backButton }
        }
    }

    private var header: some View {
        Text(strings.get(id: SharedRes.strings().gpx_tutorial_title))
            .font(.largeTitle.weight(.bold))
            .foregroundStyle(.primary)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 12)
    }

    private var backButton: some View {
        Button(
            action: { dismiss() },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().gpx_tutorial_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.GPX_TUTORIAL_BACK_BUTTON)
    }

    private var whatIsCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 10) {
                iconChip
                Text(strings.get(id: SharedRes.strings().gpx_tutorial_what_title))
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
            }
            Text(strings.get(id: SharedRes.strings().gpx_tutorial_what_message))
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
        VStack(alignment: .leading, spacing: 8) {
            sectionHeader(strings.get(id: SharedRes.strings().gpx_tutorial_steps_section))
            VStack(spacing: 0) {
                browseStep
                stepDivider
                step(
                    number: 2,
                    title: strings.get(id: SharedRes.strings().gpx_tutorial_step_2_title),
                    message: strings.get(id: SharedRes.strings().gpx_tutorial_step_2_message)
                )
                stepDivider
                step(
                    number: 3,
                    title: strings.get(id: SharedRes.strings().gpx_tutorial_step_3_title),
                    message: strings.get(id: SharedRes.strings().gpx_tutorial_step_3_message)
                )
                stepDivider
                step(
                    number: 4,
                    title: strings.get(id: SharedRes.strings().gpx_tutorial_step_4_title),
                    message: strings.get(id: SharedRes.strings().gpx_tutorial_step_4_message)
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(.horizontal, 16)
        .padding(.top, 20)
    }

    private var stepDivider: some View {
        Divider().padding(.leading, 50)
    }

    private var browseStep: some View {
        HStack(alignment: .top, spacing: 12) {
            stepBadge(1)
            VStack(alignment: .leading, spacing: 3) {
                Text(strings.get(id: SharedRes.strings().gpx_tutorial_step_1_title))
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
                Text(strings.get(id: SharedRes.strings().gpx_tutorial_step_1_message))
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                recommendationsRow
                    .padding(.top, 8)
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 14)
        .padding(.leading, 14)
    }

    private var recommendationsRow: some View {
        HStack(spacing: 6) {
            recommendationCard(
                name: strings.get(id: SharedRes.strings().gpx_tutorial_recommended_aktivkalandor),
                image: SharedRes.images().ic_aktivkalandor.toUIImage()!,
                urlString: strings.get(id: SharedRes.strings().gpx_tutorial_recommended_aktivkalandor_url)
            )
            recommendationCard(
                name: strings.get(id: SharedRes.strings().gpx_tutorial_recommended_kirandulastippek),
                image: SharedRes.images().ic_kirandulastippek.toUIImage()!,
                urlString: strings.get(id: SharedRes.strings().gpx_tutorial_recommended_kirandulastippek_url)
            )
            recommendationCard(
                name: strings.get(id: SharedRes.strings().gpx_tutorial_recommended_termeszetjaro),
                image: SharedRes.images().ic_termeszetjaro.toUIImage()!,
                urlString: strings.get(id: SharedRes.strings().gpx_tutorial_recommended_termeszetjaro_url)
            )
        }
    }

    private func step(number: Int, title: String, message: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            stepBadge(number)
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
                Text(message)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding(14)
    }

    private func stepBadge(_ number: Int) -> some View {
        ZStack {
            Circle()
                .fill(primary)
                .frame(width: 24, height: 24)
            Text("\(number)")
                .font(.caption.weight(.bold))
                .foregroundStyle(.white)
        }
    }

    private func recommendationCard(name: String, image: UIImage, urlString: String) -> some View {
        Button(
            action: { open(urlString) },
            label: {
                VStack(spacing: 8) {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 40, height: 40)
                        .clipShape(Circle())
                    Text(name)
                        .font(.caption2.weight(.semibold))
                        .foregroundStyle(.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .padding(.horizontal, 2)
                .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
            }
        )
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().gpx_tutorial_a11y_open_collection, args: [name]))
    }

    private func sectionHeader(_ text: String) -> some View {
        Text(text.uppercased())
            .font(.caption.weight(.bold))
            .tracking(0.8)
            .foregroundStyle(.primary.opacity(0.4))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.leading, 4)
            .padding(.bottom, 2)
    }

    private func open(_ urlString: String) {
        if let url = URL(string: urlString) {
            UIApplication.shared.open(url)
        }
    }
}
