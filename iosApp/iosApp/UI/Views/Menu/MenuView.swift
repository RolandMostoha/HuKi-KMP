import Shared
import SwiftUI

enum MenuRoute: Hashable {
    case menu
}

struct MenuView: View {
    let onGpxCollectionClicked: () -> Void
    let onLocationIqClicked: () -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getMenuViewModel()
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let onPrimary = Color(SharedRes.colors().onPrimary.getUIColor())

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            ScrollView {
                VStack(spacing: 0) {
                    hero(versionName: uiState.versionName)
                    Spacer().frame(height: 10)
                    gpxCollectionSection
                    contactSection
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

    private var gpxCollectionSection: some View {
        VStack(spacing: 0) {
            MenuItemView(
                icon: tintedIcon(SharedRes.images().ic_gpx.toUIImage()!, color: onPrimary),
                title: strings.get(id: SharedRes.strings().menu_item_gpx_collection),
                iconBackgroundColor: primary,
                accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_gpx_collection),
                testTag: TestTags.shared.MENU_ROW_GPX_COLLECTION,
                action: { viewModel.onEvent(event: MenuUiEventsGpxCollectionClicked.shared) }
            )
        }
        .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .padding(.horizontal, 16)
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

    private var divider: some View {
        Divider()
            .padding(.leading, 16 + 40 + 16)
    }

    private func handleEffect(_ effect: MenuUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
        case .navigateToGpxCollection:
            onGpxCollectionClicked()
        case .navigateToLocationIq:
            onLocationIqClicked()
        case .openUrl(let openUrl):
            if let url = URL(string: strings.get(id: openUrl.urlRes)) {
                UIApplication.shared.open(url)
            }
        case .sendEmail(let sendEmail):
            let email = strings.get(id: sendEmail.emailRes)
            let subject = strings.get(id: sendEmail.subjectRes)
            let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
            if let url = URL(string: "mailto:\(email)?subject=\(encodedSubject)") {
                UIApplication.shared.open(url)
            }
        }
    }
}
