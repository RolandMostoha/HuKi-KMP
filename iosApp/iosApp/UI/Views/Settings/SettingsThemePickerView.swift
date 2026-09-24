import Shared
import SwiftUI

struct SettingsThemePickerView: View {
    let strings: Strings
    let selectedThemeMode: ThemeMode
    let onThemeModeSelected: (ThemeMode) -> Void

    private var selection: Binding<ThemeMode> {
        Binding(
            get: { selectedThemeMode },
            set: { onThemeModeSelected($0) }
        )
    }

    var body: some View {
        Picker(strings.get(id: SharedRes.strings().settings_a11y_theme_picker), selection: selection) {
            ForEach(ThemeMode.allCases, id: \.self) { themeMode in
                Text(strings.get(id: themeMode.title))
                    .tag(themeMode)
            }
        }
        .pickerStyle(.segmented)
        .labelsHidden()
        .accessibilityIdentifier(TestTags.shared.SETTINGS_THEME_PICKER)
    }
}

#Preview {
    VStack(spacing: 24) {
        SettingsThemePickerView(strings: Strings(), selectedThemeMode: .system, onThemeModeSelected: { _ in })
        SettingsThemePickerView(strings: Strings(), selectedThemeMode: .light, onThemeModeSelected: { _ in })
        SettingsThemePickerView(strings: Strings(), selectedThemeMode: .dark, onThemeModeSelected: { _ in })
    }
    .padding()
}
