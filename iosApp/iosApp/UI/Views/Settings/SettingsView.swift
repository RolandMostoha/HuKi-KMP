import Shared
import SwiftUI

enum SettingsRoute: Hashable {
    case settings
}

struct SettingsView: View {
    @State private var viewModel = KoinViewModelProvider.shared.getSettingsViewModel()
    @Environment(\.dismiss) private var dismiss

    private let strings = Strings()

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            Form {
                Section(strings.get(id: SharedRes.strings().settings_section_map)) {
                    Toggle(
                        isOn: Binding(
                            get: { uiState.mapZoomControlsVisible },
                            set: { newValue in
                                let event = SettingsUiEventsMapZoomControlsToggled(visible: newValue)
                                viewModel.onEvent(event: event)
                            }
                        )
                    ) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(strings.get(id: SharedRes.strings().settings_zoom_controls_title))
                            Text(strings.get(id: SharedRes.strings().settings_zoom_controls_description))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .accessibilityIdentifier(TestTags.shared.SETTINGS_ZOOM_CONTROLS_TOGGLE)
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
                for await effect in viewModel.uiEffects {
                    handleEffect(effect)
                }
            }
        }
    }

    private func handleEffect(_ effect: SettingsUiEffects) {
        switch onEnum(of: effect) {
        case .navigateBack:
            dismiss()
        }
    }
}
