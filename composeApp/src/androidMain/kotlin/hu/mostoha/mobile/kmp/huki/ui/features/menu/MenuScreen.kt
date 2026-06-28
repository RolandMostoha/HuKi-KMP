package hu.mostoha.mobile.kmp.huki.ui.features.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import hu.mostoha.mobile.kmp.huki.features.menu.MenuUiEffects
import hu.mostoha.mobile.kmp.huki.features.menu.MenuUiEvents
import hu.mostoha.mobile.kmp.huki.features.menu.MenuUiState
import hu.mostoha.mobile.kmp.huki.features.menu.MenuViewModel
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
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
fun MenuScreen(
    onBack: () -> Unit,
    onDestinationsClicked: () -> Unit,
    onGpxCollectionClicked: () -> Unit,
    onPlaceHistoryClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
    viewModel: MenuViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MenuContent(
        uiState = uiState,
        menuUiEffects = viewModel.menuUiEffects,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onDestinationsClicked = onDestinationsClicked,
        onGpxCollectionClicked = onGpxCollectionClicked,
        onPlaceHistoryClicked = onPlaceHistoryClicked,
        onLocationIqClicked = onLocationIqClicked,
    )
}

@Composable
private fun MenuContent(
    uiState: MenuUiState,
    menuUiEffects: Flow<MenuUiEffects>,
    onEvent: (MenuUiEvents) -> Unit,
    onBack: () -> Unit,
    onDestinationsClicked: () -> Unit,
    onGpxCollectionClicked: () -> Unit,
    onPlaceHistoryClicked: () -> Unit,
    onLocationIqClicked: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(menuUiEffects) {
        menuUiEffects.collect { effect ->
            when (effect) {
                MenuUiEffects.NavigateBack -> onBack()
                MenuUiEffects.NavigateToPlaceHistory -> onPlaceHistoryClicked()
                MenuUiEffects.NavigateToGpxCollection -> onGpxCollectionClicked()
                MenuUiEffects.NavigateToLocationIq -> onLocationIqClicked()
                is MenuUiEffects.OpenUrl -> context.openUrl(context.resolveMoko(effect.urlRes))
                is MenuUiEffects.SendEmail -> context.sendEmail(
                    email = context.resolveMoko(effect.emailRes),
                    subject = context.resolveMoko(effect.subjectRes),
                )
                MenuUiEffects.NavigateToDestinations -> onDestinationsClicked()
            }
        }
    }
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.MENU_SCREEN_ROOT),
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
                    .testTag(TestTags.MENU_BACK_BUTTON),
                onClick = { onEvent(MenuUiEvents.BackClicked) },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_back),
                )
            }
            MenuHero(versionName = uiState.versionName)
            Spacer(modifier = Modifier.height(Dimens.Medium))
            MenuCard {
                MenuRow(
                    title = mokoString(SharedRes.strings.menu_item_destinations),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_open_destinations),
                    testTag = TestTags.MENU_ROW_DESTINATIONS,
                    onClick = { onEvent(MenuUiEvents.DestinationsClicked) },
                    description = mokoString(SharedRes.strings.menu_item_destinations_description),
                    iconBackgroundColor = mokoColor(SharedRes.colors.primary),
                ) {
                    TintedRowIcon(
                        drawableResId = R.drawable.ic_backpack,
                        tint = mokoColor(SharedRes.colors.onPrimary),
                    )
                }
                MenuRowDivider()
                MenuRow(
                    title = mokoString(SharedRes.strings.menu_item_place_history),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_open_place_history),
                    testTag = TestTags.MENU_ROW_PLACE_HISTORY,
                    onClick = { onEvent(MenuUiEvents.PlaceHistoryClicked) },
                    description = mokoString(SharedRes.strings.menu_item_place_history_description),
                    iconBackgroundColor = mokoColor(SharedRes.colors.primary),
                ) {
                    TintedRowIcon(
                        drawableResId = R.drawable.ic_place_circle,
                        tint = mokoColor(SharedRes.colors.onPrimary),
                    )
                }
                MenuRowDivider()
                MenuRow(
                    title = mokoString(SharedRes.strings.menu_item_gpx_collection),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_open_gpx_collection),
                    testTag = TestTags.MENU_ROW_GPX_COLLECTION,
                    onClick = { onEvent(MenuUiEvents.GpxCollectionClicked) },
                    description = mokoString(SharedRes.strings.menu_item_gpx_collection_description),
                    iconBackgroundColor = mokoColor(SharedRes.colors.primary),
                ) {
                    TintedRowIcon(
                        drawableResId = SharedRes.images.ic_gpx.drawableResId,
                        tint = mokoColor(SharedRes.colors.onPrimary),
                    )
                }
            }
            MenuSectionHeader(text = mokoString(SharedRes.strings.menu_section_contact))
            MenuCard {
                MenuRow(
                    title = mokoString(SharedRes.strings.menu_item_email),
                    valueText = mokoString(SharedRes.strings.menu_contact_email),
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_open_email),
                    testTag = TestTags.MENU_ROW_EMAIL,
                    onClick = { onEvent(MenuUiEvents.EmailClicked) },
                ) {
                    TintedRowIcon(SharedRes.images.ic_email.drawableResId)
                }
                MenuRowDivider()
                MenuRow(
                    title = mokoString(SharedRes.strings.menu_item_facebook),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_open_facebook),
                    testTag = TestTags.MENU_ROW_FACEBOOK,
                    onClick = { onEvent(MenuUiEvents.FacebookClicked) },
                    description = mokoString(SharedRes.strings.menu_item_facebook_description),
                ) {
                    TintedRowIcon(SharedRes.images.ic_facebook.drawableResId)
                }
                MenuRowDivider()
                MenuRow(
                    title = mokoString(SharedRes.strings.menu_item_github),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_open_github),
                    testTag = TestTags.MENU_ROW_GITHUB,
                    onClick = { onEvent(MenuUiEvents.GithubClicked) },
                    description = mokoString(SharedRes.strings.menu_item_github_description),
                ) {
                    TintedRowIcon(SharedRes.images.ic_github.drawableResId)
                }
            }
            MenuSectionHeader(text = mokoString(SharedRes.strings.menu_section_supporters))
            MenuCard {
                MenuRow(
                    title = mokoString(SharedRes.strings.menu_item_location_iq),
                    valueText = null,
                    contentDescription = mokoString(SharedRes.strings.menu_a11y_open_location_iq),
                    testTag = TestTags.MENU_ROW_LOCATION_IQ,
                    onClick = { onEvent(MenuUiEvents.LocationIqClicked) },
                    showIconBackground = false,
                    description = mokoString(SharedRes.strings.menu_item_location_iq_description),
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
private fun MenuHero(versionName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(SharedRes.images.ic_app_icon.drawableResId),
            contentDescription = mokoString(SharedRes.strings.menu_a11y_app_icon),
            modifier = Modifier
                .size(Dimens.IconHero)
                .shadow(
                    elevation = Dimens.Small,
                    shape = RoundedCornerShape(Dimens.ExtraLarge),
                    clip = true,
                )
                .testTag(TestTags.MENU_APP_ICON),
        )
        Text(
            text = mokoString(SharedRes.strings.menu_app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.Large),
        )
        Text(
            text = mokoString(SharedRes.strings.menu_app_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.ExtraSmall),
        )
        VersionPill(
            text = mokoString(SharedRes.strings.menu_version_pattern, versionName),
            modifier = Modifier
                .padding(top = Dimens.Medium)
                .testTag(TestTags.MENU_VERSION),
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
private fun MenuSectionHeader(text: String) {
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
private fun MenuCard(content: @Composable () -> Unit) {
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
private fun MenuRow(
    title: String,
    valueText: String?,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    showIconBackground: Boolean = true,
    iconBackgroundColor: Color? = null,
    description: String? = null,
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
                            color = iconBackgroundColor ?: mokoColor(SharedRes.colors.primaryContainer),
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
                fontWeight = FontWeight.Normal,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.ExtraSmall),
                )
            }
        }
        if (valueText != null) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = Dimens.Small),
            )
        }
    }
}

@Composable
private fun TintedRowIcon(drawableResId: Int, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Icon(
        imageVector = ImageVector.vectorResource(drawableResId),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(Dimens.IconSmall),
    )
}

@Composable
private fun MenuRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = Dimens.Large + Dimens.IconContainer + Dimens.Large),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.dividerColor,
    )
}

@Preview
@Composable
private fun MenuContentPreview() {
    HuKiTheme {
        MenuContent(
            uiState = MenuUiState.Default,
            menuUiEffects = emptyFlow(),
            onEvent = {},
            onBack = {},
            onDestinationsClicked = {},
            onGpxCollectionClicked = {},
            onPlaceHistoryClicked = {},
            onLocationIqClicked = {},
        )
    }
}
