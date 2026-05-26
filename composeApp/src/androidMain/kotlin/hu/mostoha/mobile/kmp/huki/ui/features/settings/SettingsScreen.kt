package hu.mostoha.mobile.kmp.huki.ui.features.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.openUrl
import hu.mostoha.mobile.kmp.huki.util.resolveMoko
import hu.mostoha.mobile.kmp.huki.util.sendEmail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel(), onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        settingsUiEffects = viewModel.settingsUiEffects,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    settingsUiEffects: Flow<SettingsUiEffects>,
    onEvent: (SettingsUiEvents) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(settingsUiEffects) {
        settingsUiEffects.collect { effect ->
            when (effect) {
                SettingsUiEffects.NavigateBack -> onBack()
                is SettingsUiEffects.OpenUrl -> context.openUrl(context.resolveMoko(effect.urlRes))
                is SettingsUiEffects.SendEmail -> context.sendEmail(
                    email = context.resolveMoko(effect.emailRes),
                    subject = context.resolveMoko(effect.subjectRes),
                )
            }
        }
    }
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.SETTINGS_SCREEN_ROOT),
        containerColor = backgroundColor,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = Dimens.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = Dimens.Small, top = Dimens.Small)
                    .testTag(TestTags.SETTINGS_BACK_BUTTON),
                onClick = { onEvent(SettingsUiEvents.BackClicked) },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = mokoString(SharedRes.strings.settings_a11y_back),
                )
            }
            SettingsHero(versionName = uiState.versionName)
            SettingsSectionHeader(text = mokoString(SharedRes.strings.settings_section_contact))
            SettingsCard {
                SettingsRow(
                    title = mokoString(SharedRes.strings.settings_item_email),
                    valueText = mokoString(SharedRes.strings.settings_contact_email),
                    contentDescription = mokoString(SharedRes.strings.settings_a11y_open_email),
                    testTag = TestTags.SETTINGS_ROW_EMAIL,
                    onClick = { onEvent(SettingsUiEvents.EmailClicked) },
                ) {
                    TintedRowIcon(SharedRes.images.ic_email.drawableResId)
                }
                SettingsRowDivider()
                SettingsRow(
                    title = mokoString(SharedRes.strings.settings_item_facebook),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.settings_a11y_open_facebook),
                    testTag = TestTags.SETTINGS_ROW_FACEBOOK,
                    onClick = { onEvent(SettingsUiEvents.FacebookClicked) },
                    description = mokoString(SharedRes.strings.settings_item_facebook_description),
                ) {
                    TintedRowIcon(SharedRes.images.ic_facebook.drawableResId)
                }
                SettingsRowDivider()
                SettingsRow(
                    title = mokoString(SharedRes.strings.settings_item_github),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.settings_a11y_open_github),
                    testTag = TestTags.SETTINGS_ROW_GITHUB,
                    onClick = { onEvent(SettingsUiEvents.GithubClicked) },
                    description = mokoString(SharedRes.strings.settings_item_github_description),
                ) {
                    TintedRowIcon(SharedRes.images.ic_github.drawableResId)
                }
            }
            SettingsSectionHeader(text = mokoString(SharedRes.strings.settings_section_supporters))
            SettingsCard {
                SettingsRow(
                    title = mokoString(SharedRes.strings.settings_item_location_iq),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.settings_a11y_open_location_iq),
                    testTag = TestTags.SETTINGS_ROW_LOCATION_IQ,
                    onClick = { onEvent(SettingsUiEvents.LocationIqClicked) },
                    showIconBackground = false,
                    description = mokoString(SharedRes.strings.settings_item_location_iq_description),
                    descriptionColor = mokoColor(SharedRes.colors.locationIqRed),
                ) {
                    Image(
                        painter = painterResource(SharedRes.images.ic_location_iq_circle.drawableResId),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.IconContainer),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsHero(versionName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(SharedRes.images.ic_app_icon.drawableResId),
            contentDescription = mokoString(SharedRes.strings.settings_a11y_app_icon),
            modifier = Modifier
                .size(Dimens.IconHero)
                .shadow(
                    elevation = Dimens.Small,
                    shape = RoundedCornerShape(Dimens.ExtraLarge),
                    clip = true,
                )
                .testTag(TestTags.SETTINGS_APP_ICON),
        )
        Text(
            text = mokoString(SharedRes.strings.settings_app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.Large),
        )
        Text(
            text = mokoString(SharedRes.strings.settings_app_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.ExtraSmall),
        )
        VersionPill(
            text = mokoString(SharedRes.strings.settings_version_pattern, versionName),
            modifier = Modifier
                .padding(top = Dimens.Medium)
                .testTag(TestTags.SETTINGS_VERSION),
        )
    }
}

@Composable
private fun VersionPill(text: String, modifier: Modifier = Modifier) {
    val container = mokoColor(SharedRes.colors.primaryContainer)
    val content = mokoColor(SharedRes.colors.primary)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.MediumLarge, vertical = Dimens.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.Small)
                    .background(color = content, shape = CircleShape),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = content,
                modifier = Modifier.padding(start = Dimens.Small),
            )
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
private fun SettingsRow(
    title: String,
    valueText: String?,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    showIconBackground: Boolean = true,
    description: String? = null,
    descriptionColor: Color? = null,
    icon: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
            .semantics(mergeDescendants = true) { this.contentDescription = contentDescription }
            .padding(horizontal = Dimens.Large, vertical = Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.IconContainer)
                .then(
                    if (showIconBackground) {
                        Modifier.background(
                            color = mokoColor(SharedRes.colors.primaryContainer),
                            shape = CircleShape,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(
            modifier = Modifier
                .padding(start = Dimens.Large)
                .weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light,
                    color = descriptionColor ?: mokoColor(SharedRes.colors.primary),
                    modifier = Modifier.padding(top = Dimens.ExtraSmall),
                )
            }
        }
        if (valueText != null) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Dimens.Small),
            )
        }
    }
}

@Composable
private fun TintedRowIcon(drawableResId: Int) {
    Icon(
        imageVector = ImageVector.vectorResource(drawableResId),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(Dimens.IconSmall),
    )
}

@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = Dimens.Large + Dimens.IconContainer + Dimens.Large),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    )
}

@Preview
@Composable
private fun SettingsContentPreview() {
    HuKiTheme {
        SettingsContent(
            uiState = SettingsUiState.Default,
            settingsUiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
        )
    }
}
