package hu.mostoha.mobile.kmp.huki.ui.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.settings.SettingsUiEffects
import hu.mostoha.mobile.kmp.huki.features.settings.SettingsUiEvents
import hu.mostoha.mobile.kmp.huki.features.settings.SettingsUiState
import hu.mostoha.mobile.kmp.huki.features.settings.SettingsViewModel
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        uiEffects = viewModel.uiEffects,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    uiEffects: Flow<SettingsUiEffects>,
    onEvent: (SettingsUiEvents) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(uiEffects) {
        uiEffects.collect { effect ->
            when (effect) {
                SettingsUiEffects.NavigateBack -> onBack()
            }
        }
    }
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.SETTINGS_SCREEN_ROOT),
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mokoString(SharedRes.strings.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.SETTINGS_BACK_BUTTON),
                        onClick = { onEvent(SettingsUiEvents.BackClicked) },
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.settings_a11y_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = Dimens.ExtraLarge),
        ) {
            SettingsSectionHeader(text = mokoString(SharedRes.strings.settings_section_map))
            SettingsCard {
                SettingsSwitchItem(
                    title = mokoString(SharedRes.strings.settings_zoom_controls_title),
                    description = mokoString(SharedRes.strings.settings_zoom_controls_description),
                    checked = uiState.mapZoomControlsVisible,
                    testTag = TestTags.SETTINGS_ZOOM_CONTROLS_TOGGLE,
                    onCheckedChange = { onEvent(SettingsUiEvents.MapZoomControlsToggled(it)) },
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.ExtraLarge,
                end = Dimens.ExtraLarge,
                top = Dimens.Large,
                bottom = Dimens.Medium,
            ),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large),
        shape = RoundedCornerShape(Dimens.Large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large, vertical = Dimens.MediumLarge)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.ExtraSmall),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .padding(start = Dimens.Large)
                .testTag(testTag),
        )
    }
}

@Preview
@Composable
private fun SettingsContentPreview() {
    HuKiTheme {
        SettingsContent(
            uiState = SettingsUiState.Default,
            uiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
        )
    }
}
