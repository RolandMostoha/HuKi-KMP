import Shared
import SwiftUI

enum MenuRoute: Hashable {
    case menu
}

struct MenuView: View {
    let onSettingsClicked: () -> Void
    let onDestinationsClicked: () -> Void
    let onGpxCollectionClicked: () -> Void
    let onGpxGuideClicked: () -> Void
    let onTrailSymbolsGuideClicked: () -> Void
    let onPlaceHistoryClicked: () -> Void
    let onLocationIqClicked: () -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getMenuViewModel()
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let onPrimary = Color(SharedRes.colors().onPrimary.getUIColor())
    private let secondary = Color(SharedRes.colors().secondary.getUIColor())
    private let onSecondary = Color(SharedRes.colors().onSecondary.getUIColor())

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

    private var mainFeaturesSection: some View {
        VStack(spacing: 0) {
            MenuItemView(
                icon: tintedSymbol("gearshape.fill", color: onPrimary),
                title: strings.get(id: SharedRes.strings().menu_item_settings),
                description: strings.get(id: SharedRes.strings().menu_item_settings_description),
                iconBackgroundColor: primary,
                accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_settings),
                testTag: TestTags.shared.MENU_ROW_SETTINGS,
                action: { viewModel.onEvent(event: MenuUiEventsSettingsClicked.shared) }
            )
            divider
            MenuItemView(
                icon: tintedSymbol("backpack.fill", color: onPrimary),
                title: strings.get(id: SharedRes.strings().menu_item_destinations),
                description: strings.get(id: SharedRes.strings().menu_item_destinations_description),
                iconBackgroundColor: primary,
                accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_destinations),
                testTag: TestTags.shared.MENU_ROW_DESTINATIONS,
                action: { viewModel.onEvent(event: MenuUiEventsDestinationsClicked.shared) }
            )
            divider
            MenuItemView(
                icon: tintedSymbol("mappin.and.ellipse", color: onPrimary),
                title: strings.get(id: SharedRes.strings().menu_item_place_history),
                description: strings.get(id: SharedRes.strings().menu_item_place_history_description),
                iconBackgroundColor: primary,
                accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_place_history),
                testTag: TestTags.shared.MENU_ROW_PLACE_HISTORY,
                action: { viewModel.onEvent(event: MenuUiEventsPlaceHistoryClicked.shared) }
            )
            divider
            MenuItemView(
                icon: tintedIcon(SharedRes.images().ic_gpx.toUIImage()!, color: onPrimary),
                title: strings.get(id: SharedRes.strings().menu_item_gpx_collection),
                description: strings.get(id: SharedRes.strings().menu_item_gpx_collection_description),
                iconBackgroundColor: primary,
                accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_gpx_collection),
                testTag: TestTags.shared.MENU_ROW_GPX_COLLECTION,
                action: { viewModel.onEvent(event: MenuUiEventsGpxCollectionClicked.shared) }
            )
        }
        .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .padding(.horizontal, 16)
    }

    private var guidesSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            MenuSectionHeaderView(text: strings.get(id: SharedRes.strings().menu_section_guides))
            VStack(spacing: 0) {
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_gpx.toUIImage()!, color: onSecondary),
                    title: strings.get(id: SharedRes.strings().menu_item_gpx_guide),
                    description: strings.get(id: SharedRes.strings().menu_item_gpx_guide_description),
                    iconBackgroundColor: secondary,
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_gpx_guide),
                    testTag: TestTags.shared.MENU_ROW_GPX_GUIDE,
                    action: { viewModel.onEvent(event: MenuUiEventsGpxGuideClicked.shared) }
                )
                divider
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_place_category_guidepost.toUIImage()!, color: onSecondary),
                    title: strings.get(id: SharedRes.strings().menu_item_trail_symbols_guide),
                    description: strings.get(id: SharedRes.strings().menu_item_trail_symbols_guide_description),
                    iconBackgroundColor: secondary,
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_trail_symbols_guide),
                    testTag: TestTags.shared.MENU_ROW_TRAIL_SYMBOLS_GUIDE,
                    action: { viewModel.onEvent(event: MenuUiEventsTrailSymbolsGuideClicked.shared) }
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    private var contactSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            MenuSectionHeaderView(text: strings.get(id: SharedRes.strings().menu_section_contact))
            VStack(spacing: 0) {
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_email.toUIImage()!),
                    title: strings.get(id: SharedRes.strings().menu_item_email),
                    value: strings.get(id: SharedRes.strings().menu_contact_email),
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_email),
                    testTag: TestTags.shared.MENU_ROW_EMAIL,
                    action: { viewModel.onEvent(event: MenuUiEventsEmailClicked.shared) }
                )
                divider
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_facebook.toUIImage()!),
                    title: strings.get(id: SharedRes.strings().menu_item_facebook),
                    description: strings.get(id: SharedRes.strings().menu_item_facebook_description),
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_facebook),
                    testTag: TestTags.shared.MENU_ROW_FACEBOOK,
                    action: { viewModel.onEvent(event: MenuUiEventsFacebookClicked.shared) }
                )
                divider
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_github.toUIImage()!),
                    title: strings.get(id: SharedRes.strings().menu_item_github),
                    description: strings.get(id: SharedRes.strings().menu_item_github_description),
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_github),
                    testTag: TestTags.shared.MENU_ROW_GITHUB,
                    action: { viewModel.onEvent(event: MenuUiEventsGithubClicked.shared) }
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    private var legalSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            MenuSectionHeaderView(text: strings.get(id: SharedRes.strings().menu_section_legal))
            VStack(spacing: 0) {
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_link.toUIImage()!),
                    title: strings.get(id: SharedRes.strings().menu_item_privacy_policy),
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_privacy_policy),
                    testTag: TestTags.shared.MENU_ROW_PRIVACY_POLICY,
                    action: { viewModel.onEvent(event: MenuUiEventsPrivacyPolicyClicked.shared) }
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    private var supportersSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            MenuSectionHeaderView(text: strings.get(id: SharedRes.strings().menu_section_supporters))
            VStack(spacing: 0) {
                MenuItemView(
                    icon: Image(uiImage: SharedRes.images().ic_location_iq_circle.toUIImage()!)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 40, height: 40),
                    title: strings.get(id: SharedRes.strings().menu_item_location_iq),
                    description: strings.get(id: SharedRes.strings().menu_item_location_iq_description),
                    showIconBackground: false,
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_location_iq),
                    testTag: TestTags.shared.MENU_ROW_LOCATION_IQ,
                    action: { viewModel.onEvent(event: MenuUiEventsLocationIqClicked.shared) }
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    private func tintedIcon(_ image: UIImage, color: SwiftUI.Color = Color(.label)) -> some View {
        Image(uiImage: image)
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .frame(width: 22, height: 22)
            .foregroundStyle(color)
    }
}

    private func tintedSymbol(_ systemName: String, color: SwiftUI.Color = Color(.label)) -> some View {
        Image(systemName: systemName)
            .resizable()
            .scaledToFit()
            .frame(width: 22, height: 22)
            .foregroundStyle(color)
    }

    private var divider: some View { Divider().padding(.leading, 16 + 40 + 16) }

private extension MenuView {
    func handleEffect(_ effect: MenuUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
        case .navigateToSettings:
            onSettingsClicked()
        case .navigateToDestinations:
            onDestinationsClicked()
        case .navigateToPlaceHistory:
            onPlaceHistoryClicked()
        case .navigateToGpxCollection:
            onGpxCollectionClicked()
        case .navigateToGpxGuide:
            onGpxGuideClicked()
        case .navigateToTrailSymbolsGuide:
            onTrailSymbolsGuideClicked()
        case .navigateToLocationIq:
            onLocationIqClicked()
        case .openUrl(let openUrl):
            openExternalUrl(strings.get(id: openUrl.urlRes))
        case .sendEmail(let sendEmail):
            composeEmail(email: strings.get(id: sendEmail.emailRes), subject: strings.get(id: sendEmail.subjectRes))
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
