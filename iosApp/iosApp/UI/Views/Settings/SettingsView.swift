import Shared
import SwiftUI

enum SettingsRoute: Hashable {
    case settings
}

struct SettingsView: View {
    @State private var viewModel = KoinViewModelProvider.shared.getSettingsViewModel()
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    private let locationIqColor = Color(SharedRes.colors().locationIqRed.getUIColor())
    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            ScrollView {
                VStack(spacing: 0) {
                    hero(versionName: uiState.versionName)
                    contactSection
                    supportersSection
                }
                .padding(.bottom, 24)
            }
            .background(Color(.systemGray6))
            .accessibilityIdentifier(TestTags.shared.SETTINGS_SCREEN_ROOT)
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(
                        action: { viewModel.onEvent(event: SettingsUiEventsBackClicked.shared) },
                        label: {
                            Image(systemName: "chevron.backward")
                                .font(.system(size: 18, weight: .semibold))
                                .foregroundStyle(.primary)
                        }
                    )
                    .accessibilityIdentifier(TestTags.shared.SETTINGS_BACK_BUTTON)
                    .accessibilityLabel(strings.get(id: SharedRes.strings().settings_a11y_back))
                }
            }
            .task {
                for await effect in viewModel.settingsUiEffects {
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
                .accessibilityIdentifier(TestTags.shared.SETTINGS_APP_ICON)
            Text(strings.get(id: SharedRes.strings().settings_app_name))
                .font(.title.weight(.semibold))
                .padding(.top, 16)
            Text(strings.get(id: SharedRes.strings().settings_app_description))
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
            Text(strings.get(id: SharedRes.strings().settings_version_pattern, args: [versionName]))
                .font(.caption.weight(.semibold))
                .foregroundStyle(primary)
        }
        .padding(.horizontal, 13)
        .padding(.vertical, 6)
        .background(primary.opacity(0.15), in: .capsule)
        .accessibilityIdentifier(TestTags.shared.SETTINGS_VERSION)
    }

    private var contactSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            SettingsSectionHeaderView(text: strings.get(id: SharedRes.strings().settings_section_contact))
            VStack(spacing: 0) {
                SettingsItemView(
                    icon: tintedIcon(SharedRes.images().ic_email.toUIImage()!),
                    title: strings.get(id: SharedRes.strings().settings_item_email),
                    value: strings.get(id: SharedRes.strings().settings_contact_email),
                    accessibilityLabel: strings.get(id: SharedRes.strings().settings_a11y_open_email),
                    testTag: TestTags.shared.SETTINGS_ROW_EMAIL,
                    action: { viewModel.onEvent(event: SettingsUiEventsEmailClicked.shared) }
                )
                divider
                SettingsItemView(
                    icon: tintedIcon(SharedRes.images().ic_facebook.toUIImage()!),
                    title: strings.get(id: SharedRes.strings().settings_item_facebook),
                    description: strings.get(id: SharedRes.strings().settings_item_facebook_description),
                    descriptionColor: primary,
                    accessibilityLabel: strings.get(id: SharedRes.strings().settings_a11y_open_facebook),
                    testTag: TestTags.shared.SETTINGS_ROW_FACEBOOK,
                    action: { viewModel.onEvent(event: SettingsUiEventsFacebookClicked.shared) }
                )
                divider
                SettingsItemView(
                    icon: tintedIcon(SharedRes.images().ic_github.toUIImage()!),
                    title: strings.get(id: SharedRes.strings().settings_item_github),
                    description: strings.get(id: SharedRes.strings().settings_item_github_description),
                    descriptionColor: primary,
                    accessibilityLabel: strings.get(id: SharedRes.strings().settings_a11y_open_github),
                    testTag: TestTags.shared.SETTINGS_ROW_GITHUB,
                    action: { viewModel.onEvent(event: SettingsUiEventsGithubClicked.shared) }
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    private var supportersSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            SettingsSectionHeaderView(text: strings.get(id: SharedRes.strings().settings_section_supporters))
            VStack(spacing: 0) {
                SettingsItemView(
                    icon: Image(uiImage: SharedRes.images().ic_location_iq_circle.toUIImage()!)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 40, height: 40),
                    title: strings.get(id: SharedRes.strings().settings_item_location_iq),
                    description: strings.get(id: SharedRes.strings().settings_item_location_iq_description),
                    descriptionColor: locationIqColor,
                    showIconBackground: false,
                    accessibilityLabel: strings.get(id: SharedRes.strings().settings_a11y_open_location_iq),
                    testTag: TestTags.shared.SETTINGS_ROW_LOCATION_IQ,
                    action: { viewModel.onEvent(event: SettingsUiEventsLocationIqClicked.shared) }
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    private func tintedIcon(_ image: UIImage) -> some View {
        Image(uiImage: image)
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .frame(width: 22, height: 22)
            .foregroundStyle(Color(.label))
    }

    private var divider: some View {
        Divider()
            .padding(.leading, 16 + 40 + 16)
    }

    private func handleEffect(_ effect: SettingsUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
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
