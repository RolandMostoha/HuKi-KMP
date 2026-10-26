import Shared
import SwiftUI

enum SettingsRoute: Hashable {
    case settings
}

struct SettingsView: View {
    let onMapLabelScaleClicked: () -> Void

    @StateObject private var holder = ViewModelHolder(KoinViewModelProvider.shared.getSettingsViewModel)
    private var viewModel: SettingsViewModel { holder.viewModel }
    @Environment(\.dismiss) private var dismiss
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private let strings = Strings()

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            Form {
                Section(strings.get(id: SharedRes.strings().settings_section_appearance)) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(strings.get(id: SharedRes.strings().settings_theme_title))
                        SettingsThemePickerView(
                            strings: strings,
                            selectedThemeMode: uiState.themeMode,
                            onThemeModeSelected: { themeMode in
                                let event = SettingsUiEventsThemeModeSelected(themeMode: themeMode)
                                viewModel.onEvent(event: event)
                            }
                        )
                    }
                }
                Section(strings.get(id: SharedRes.strings().settings_section_map)) {
                    Button(
                        action: { viewModel.onEvent(event: SettingsUiEventsMapLabelScaleClicked.shared) },
                        label: {
                            HStack {
                                LabeledContent(
                                    strings.get(id: SharedRes.strings().map_label_scale_title),
                                    value: mapLabelScaleValue(uiState.mapLabelScale)
                                )
                                Image(systemName: "chevron.forward")
                                    .font(.footnote.weight(.semibold))
                                    .foregroundStyle(.tertiary)
                                    .accessibilityHidden(true)
                            }
                            .contentShape(Rectangle())
                        }
                    )
                    .buttonStyle(.plain)
                    .accessibilityIdentifier(TestTags.shared.SETTINGS_MAP_LABEL_SCALE_ROW)
                    SettingsToggleRow(
                        title: strings.get(id: SharedRes.strings().settings_zoom_controls_title),
                        description: strings.get(id: SharedRes.strings().settings_zoom_controls_description),
                        isOn: uiState.mapZoomControlsVisible,
                        testTag: TestTags.shared.SETTINGS_ZOOM_CONTROLS_TOGGLE,
                        onToggle: { viewModel.onEvent(event: SettingsUiEventsMapZoomControlsToggled(visible: $0)) }
                    )
                    SettingsToggleRow(
                        title: strings.get(id: SharedRes.strings().settings_map_rotation_title),
                        description: strings.get(id: SharedRes.strings().settings_map_rotation_description),
                        isOn: uiState.mapRotationEnabled,
                        testTag: TestTags.shared.SETTINGS_MAP_ROTATION_TOGGLE,
                        onToggle: { viewModel.onEvent(event: SettingsUiEventsMapRotationToggled(enabled: $0)) }
                    )
                }
                Section(strings.get(id: SharedRes.strings().settings_section_gpx)) {
                    SettingsToggleRow(
                        title: strings.get(id: SharedRes.strings().settings_gpx_distances_title),
                        description: strings.get(id: SharedRes.strings().settings_gpx_distances_description),
                        isOn: uiState.gpxDistancesVisible,
                        testTag: TestTags.shared.SETTINGS_GPX_DISTANCES_TOGGLE,
                        onToggle: { viewModel.onEvent(event: SettingsUiEventsGpxDistancesToggled(visible: $0)) }
                    )
                    SettingsToggleRow(
                        title: strings.get(id: SharedRes.strings().settings_gpx_slope_colors_title),
                        description: strings.get(id: SharedRes.strings().settings_gpx_slope_colors_description),
                        isOn: uiState.gpxSlopeColorsVisible,
                        testTag: TestTags.shared.SETTINGS_GPX_SLOPE_COLORS_TOGGLE,
                        onToggle: { viewModel.onEvent(event: SettingsUiEventsGpxSlopeColorsToggled(visible: $0)) }
                    )
                }
            }
            .accessibilityIdentifier(TestTags.shared.SETTINGS_SCREEN_ROOT)
            .navigationTitle(strings.get(id: SharedRes.strings().settings_title))
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(
                        action: { viewModel.onEvent(event: SettingsUiEventsBackClicked.shared) },
                        label: {
                            Label(
                                strings.get(id: SharedRes.strings().settings_a11y_back),
                                systemImage: "chevron.backward"
                            )
                            .fontWeight(.semibold)
                            .foregroundStyle(.primary)
                        }
                    )
                    .labelStyle(.iconOnly)
                    .accessibilityIdentifier(TestTags.shared.SETTINGS_BACK_BUTTON)
                }
            }
            .task {
                viewModel.onEvent(event: SettingsUiEventsScreenViewed.shared)
            }
            .task {
                for await effect in viewModel.uiEffects {
                    handleEffect(effect)
                }
            }
        }
    }

    private func mapLabelScaleValue(_ labelScale: MapLabelScale) -> String {
        let percent = labelScale.effectivePercent(systemScale: dynamicTypeSize.mapboxSymbolScale)
        return strings.get(id: SharedRes.strings().map_label_scale_percent, args: [percent])
    }

    private func handleEffect(_ effect: SettingsUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
        case .navigateToMapLabelScale:
            onMapLabelScaleClicked()
        }
    }
}
