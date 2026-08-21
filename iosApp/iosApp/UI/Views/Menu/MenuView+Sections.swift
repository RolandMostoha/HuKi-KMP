import Shared
import SwiftUI

extension MenuView {
    var mainFeaturesSection: some View {
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
                icon: tintedSymbol(
                    "point.topright.arrow.triangle.backward.to.point.bottomleft.scurvepath.fill",
                    color: onPrimary
                ),
                title: strings.get(id: SharedRes.strings().menu_item_route_planner),
                description: strings.get(id: SharedRes.strings().menu_item_route_planner_description),
                iconBackgroundColor: primary,
                accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_route_planner),
                testTag: TestTags.shared.MENU_ROW_ROUTE_PLANNER,
                action: { viewModel.onEvent(event: MenuUiEventsRoutePlannerClicked.shared) }
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

    var guidesSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            MenuSectionHeaderView(text: strings.get(id: SharedRes.strings().menu_section_guides))
            VStack(spacing: 0) {
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_place_category_guidepost.toUIImage()!, color: onSecondary),
                    title: strings.get(id: SharedRes.strings().menu_item_trail_symbols_guide),
                    description: strings.get(id: SharedRes.strings().menu_item_trail_symbols_guide_description),
                    iconBackgroundColor: secondary,
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_trail_symbols_guide),
                    testTag: TestTags.shared.MENU_ROW_TRAIL_SYMBOLS_GUIDE,
                    action: { viewModel.onEvent(event: MenuUiEventsTrailSymbolsGuideClicked.shared) }
                )
                divider
                MenuItemView(
                    icon: tintedIcon(SharedRes.images().ic_gpx.toUIImage()!, color: onSecondary),
                    title: strings.get(id: SharedRes.strings().menu_item_gpx_guide),
                    description: strings.get(id: SharedRes.strings().menu_item_gpx_guide_description),
                    iconBackgroundColor: secondary,
                    accessibilityLabel: strings.get(id: SharedRes.strings().menu_a11y_open_gpx_guide),
                    testTag: TestTags.shared.MENU_ROW_GPX_GUIDE,
                    action: { viewModel.onEvent(event: MenuUiEventsGpxGuideClicked.shared) }
                )
            }
            .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    var contactSection: some View {
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

    var legalSection: some View {
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

    var supportersSection: some View {
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

    func tintedIcon(_ image: UIImage, color: SwiftUI.Color = Color(.label)) -> some View {
        Image(uiImage: image)
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .frame(width: 22, height: 22)
            .foregroundStyle(color)
    }

    func tintedSymbol(_ systemName: String, color: SwiftUI.Color = Color(.label)) -> some View {
        Image(systemName: systemName)
            .resizable()
            .scaledToFit()
            .frame(width: 22, height: 22)
            .foregroundStyle(color)
    }

    var divider: some View { Divider().padding(.leading, 16 + 40 + 16) }
}
