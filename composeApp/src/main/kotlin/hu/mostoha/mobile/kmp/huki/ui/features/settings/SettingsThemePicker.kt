package hu.mostoha.mobile.kmp.huki.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsThemePicker(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeModes = ThemeMode.entries
    val themeModesLabel = mokoString(SharedRes.strings.settings_a11y_theme_picker)
    val colors = ToggleButtonDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.SETTINGS_THEME_PICKER)
            .semantics { contentDescription = themeModesLabel },
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        themeModes.forEachIndexed { index, themeMode ->
            val shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                themeModes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }
            ToggleButton(
                checked = selectedThemeMode == themeMode,
                onCheckedChange = { onThemeModeSelected(themeMode) },
                shapes = shapes,
                colors = colors,
                contentPadding = PaddingValues(horizontal = Dimens.Small, vertical = Dimens.Medium),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = mokoString(themeMode.title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsThemePickerSystemPreview() {
    HuKiTheme {
        SettingsThemePicker(selectedThemeMode = ThemeMode.SYSTEM, onThemeModeSelected = {})
    }
}

@Preview
@Composable
private fun SettingsThemePickerDarkPreview() {
    HuKiTheme(themeMode = ThemeMode.DARK) {
        SettingsThemePicker(selectedThemeMode = ThemeMode.DARK, onThemeModeSelected = {})
    }
}
