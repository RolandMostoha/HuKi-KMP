import Shared
import SwiftUI

enum MenuRoute: Hashable {
    case menu
}

struct MenuView: View {
    let onSettingsClicked: () -> Void
    let onRoutePlannerClicked: () -> Void
    let onDestinationsClicked: () -> Void
    let onGpxCollectionClicked: () -> Void
    let onGpxGuideClicked: () -> Void
    let onTrailSymbolsGuideClicked: () -> Void
    let onPlaceHistoryClicked: () -> Void
    let onLocationIqClicked: () -> Void

    @State var viewModel = KoinViewModelProvider.shared.getMenuViewModel()
    @Environment(\.dismiss) private var dismiss

    let strings = Strings()

    let primary = Color(SharedRes.colors().primary.getUIColor())
    let onPrimary = Color(SharedRes.colors().onPrimary.getUIColor())
    let secondary = Color(SharedRes.colors().secondary.getUIColor())
    let onSecondary = Color(SharedRes.colors().onSecondary.getUIColor())

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            ScrollView {
                VStack(spacing: 0) {
                    hero(versionName: uiState.versionName)
                    Spacer().frame(height: 10)
                    mainFeaturesSection
                    guidesSection
                    contactSection
                    legalSection
                    supportersSection
                }
                .padding(.bottom, 24)
                .readableWidth()
            }
            .background(Color(.systemGray6))
            .accessibilityIdentifier(TestTags.shared.MENU_SCREEN_ROOT)
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(
                        action: { viewModel.onEvent(event: MenuUiEventsBackClicked.shared) },
                        label: {
                            Label(
                                strings.get(id: SharedRes.strings().menu_a11y_back),
                                systemImage: "chevron.backward"
                            )
                            .fontWeight(.semibold)
                            .foregroundStyle(.primary)
                        }
                    )
                    .labelStyle(.iconOnly)
                    .accessibilityIdentifier(TestTags.shared.MENU_BACK_BUTTON)
                }
            }
            .task {
                for await effect in viewModel.menuUiEffects {
                    handleEffect(effect)
                }
            }
        }
    }

    private func hero(versionName: String) -> some View {
        VStack(spacing: 0) {
            Image(uiImage: SharedRes.images().ic_app_icon.toUIImage()!)
                .resizable()
                .scaledToFit()
                .frame(width: 96, height: 96)
                .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                .shadow(color: .black.opacity(0.15), radius: 6, x: 0, y: 3)
                .accessibilityIdentifier(TestTags.shared.MENU_APP_ICON)
            Text(strings.get(id: SharedRes.strings().menu_app_name))
                .font(.title.weight(.semibold))
                .padding(.top, 16)
            Text(strings.get(id: SharedRes.strings().menu_app_description))
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .padding(.top, 2)
            versionPill(versionName: versionName)
                .padding(.top, 10)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 8)
        .padding(.bottom, 16)
    }

    private func versionPill(versionName: String) -> some View {
        HStack(spacing: 6) {
            Circle()
                .fill(primary)
                .frame(width: 6, height: 6)
            Text(strings.get(id: SharedRes.strings().menu_version_pattern, args: [versionName]))
                .font(.caption.weight(.semibold))
                .foregroundStyle(primary)
        }
        .padding(.horizontal, 13)
        .padding(.vertical, 6)
        .background(primary.opacity(0.15), in: .capsule)
        .accessibilityIdentifier(TestTags.shared.MENU_VERSION)
    }
}

private extension MenuView {
    func handleEffect(_ effect: MenuUiEffects) {
        switch onEnum(of: effect) {
        case .openUrl(let openUrl):
            openExternalUrl(strings.get(id: openUrl.urlRes))
        case .sendEmail(let sendEmail):
            composeEmail(email: strings.get(id: sendEmail.emailRes), subject: strings.get(id: sendEmail.subjectRes))
        default:
            navigationAction(for: effect)?()
        }
    }

    func navigationAction(for effect: MenuUiEffects) -> (() -> Void)? {
        switch onEnum(of: effect) {
        case .navigateBack: return { dismiss() }
        case .navigateToSettings: return onSettingsClicked
        case .navigateToRoutePlanner: return onRoutePlannerClicked
        case .navigateToDestinations: return onDestinationsClicked
        case .navigateToPlaceHistory: return onPlaceHistoryClicked
        case .navigateToGpxCollection: return onGpxCollectionClicked
        case .navigateToGpxGuide: return onGpxGuideClicked
        case .navigateToTrailSymbolsGuide: return onTrailSymbolsGuideClicked
        case .navigateToLocationIq: return onLocationIqClicked
        case .openUrl, .sendEmail: return nil
        }
    }

    func openExternalUrl(_ urlString: String) {
        if let url = URL(string: urlString) {
            UIApplication.shared.open(url)
        }
    }

    func composeEmail(email: String, subject: String) {
        let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if let url = URL(string: "mailto:\(email)?subject=\(encodedSubject)") {
            UIApplication.shared.open(url)
        }
    }
}
