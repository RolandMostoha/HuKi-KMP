package hu.mostoha.mobile.kmp.huki.ui.features.support

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.support.SupportPricesState
import hu.mostoha.mobile.kmp.huki.features.support.SupportPurchaseMessage
import hu.mostoha.mobile.kmp.huki.features.support.SupportTier
import hu.mostoha.mobile.kmp.huki.features.support.SupportUiEvents
import hu.mostoha.mobile.kmp.huki.features.support.SupportUiState
import hu.mostoha.mobile.kmp.huki.features.support.SupportViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.model.domain.ProductPrice
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportSection
import hu.mostoha.mobile.kmp.huki.model.domain.SupportType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.InfoView
import hu.mostoha.mobile.kmp.huki.ui.components.ScreenViewEffect
import hu.mostoha.mobile.kmp.huki.ui.components.SectionHeader
import hu.mostoha.mobile.kmp.huki.ui.components.SupportAnimalBadges
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.openUrl
import hu.mostoha.mobile.kmp.huki.util.resolveMoko
import hu.mostoha.mobile.kmp.huki.util.sendEmail
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

private val CardShape = RoundedCornerShape(26.dp)

private val PreviewPrices = listOf(499, 999, 1499, 249, 499, 999).map { ProductPrice(it * 1_000_000L, "HUF") }

private data class SupporterHeader(
    val animal: SupportAnimal,
    val celebrationId: Int,
)

private const val PLAY_SUBSCRIPTIONS_URL = "https://play.google.com/store/account/subscriptions?package=%s"

private val PricesUnavailableInfo = InfoViewData(
    infoViewType = InfoViewType.ERROR,
    icon = SharedRes.images.ic_error,
    title = SharedRes.strings.support_prices_unavailable_title,
    message = SharedRes.strings.support_prices_unavailable,
)

@Composable
fun SupportScreen(onBack: () -> Unit, viewModel: SupportViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenViewEffect { viewModel.onEvent(SupportUiEvents.ScreenViewed) }
    SupportContent(uiState = uiState, onEvent = viewModel::onEvent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportContent(uiState: SupportUiState, onEvent: (SupportUiEvents) -> Unit, onBack: () -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val scrollState = rememberScrollState()
    // The supporter card is revealed only once the top is in view, where it appears
    var header by remember {
        mutableStateOf(uiState.supporterAnimal?.let { SupporterHeader(it, uiState.celebrationId) })
    }
    var celebrate by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.supporterAnimal, uiState.celebrationId) {
        val animal = uiState.supporterAnimal
        if (animal == null) {
            header = null
            return@LaunchedEffect
        }
        // A celebration replaces the card once the top is in view, any other change animates within it
        celebrate = uiState.celebrationId != (header?.celebrationId ?: 0)
        if (celebrate) scrollState.animateScrollTo(0)
        header = SupporterHeader(animal, uiState.celebrationId)
    }
    val isSupporter = header != null
    var expandedReasons by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    val onReasonExpandedChange = { index: Int, isExpanded: Boolean ->
        if (isExpanded && index !in expandedReasons) {
            onEvent(SupportUiEvents.SectionOpened(SupportSection.entries[index]))
        }
        expandedReasons = if (isExpanded) expandedReasons + index else expandedReasons - index
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag(TestTags.SUPPORT_SCREEN_ROOT),
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(text = mokoString(SharedRes.strings.support_title)) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.SUPPORT_BACK_BUTTON),
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.support_a11y_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = Dimens.Large)
                .padding(top = Dimens.Large, bottom = 48.dp),
        ) {
            AnimatedContent(
                targetState = header,
                contentKey = { it?.celebrationId },
                transitionSpec = {
                    (fadeIn(tween(durationMillis = 300, delayMillis = 90)) + scaleIn(initialScale = 0.92f))
                        .togetherWith(fadeOut(tween(durationMillis = 90)))
                        .using(SizeTransform(clip = false))
                },
                label = "supportHeader",
            ) { supporterHeader ->
                if (supporterHeader == null) {
                    IntroCard(expandedReasons = expandedReasons, onReasonExpandedChange = onReasonExpandedChange)
                } else {
                    SupporterCard(
                        animal = supporterHeader.animal,
                        celebrate = celebrate,
                        choices = uiState.supporterChoices,
                        onChoose = { onEvent(SupportUiEvents.SupporterAnimalChosen(it)) },
                    )
                }
            }
            AnimatedContent(
                targetState = uiState.pricesState,
                label = "supportTiers",
            ) { pricesState ->
                when (pricesState) {
                    SupportPricesState.LOADING -> TiersLoading()
                    SupportPricesState.UNAVAILABLE -> InfoView(
                        infoViewData = PricesUnavailableInfo,
                        primaryActionText = mokoString(SharedRes.strings.support_retry),
                        onPrimaryActionClick = { onEvent(SupportUiEvents.RetryClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                            .testTag(TestTags.SUPPORT_PRICES_UNAVAILABLE),
                    )
                    SupportPricesState.LOADED -> Column {
                        PurchaseMessage(
                            purchaseMessage = uiState.purchaseMessage,
                            onDismiss = { onEvent(SupportUiEvents.PurchaseMessageDismissed) },
                        )
                        TierSection(
                            tiers = uiState.tiers.filter { it.animal.type == SupportType.ONE_TIME },
                            title = if (isSupporter) {
                                mokoString(SharedRes.strings.support_one_time_more_title)
                            } else {
                                mokoString(SharedRes.strings.support_one_time_title)
                            },
                            note = mokoString(SharedRes.strings.support_one_time_note_android),
                            purchasingAnimal = uiState.purchasingAnimal,
                            onTierClick = { onEvent(SupportUiEvents.TierClicked(it)) },
                        )
                        TierSection(
                            tiers = uiState.tiers.filter { it.animal.type == SupportType.RECURRING },
                            title = if (isSupporter) {
                                mokoString(SharedRes.strings.support_recurring_more_title)
                            } else {
                                mokoString(SharedRes.strings.support_recurring_title)
                            },
                            note = mokoString(SharedRes.strings.support_recurring_note_android),
                            purchasingAnimal = uiState.purchasingAnimal,
                            onTierClick = { onEvent(SupportUiEvents.TierClicked(it)) },
                        )
                        ManageSubscriptionsButton(onClick = { onEvent(SupportUiEvents.ManageSubscriptionsClicked) })
                    }
                }
            }
            AnimatedVisibility(visible = isSupporter, enter = fadeIn() + expandVertically()) {
                AboutSection(expandedReasons = expandedReasons, onReasonExpandedChange = onReasonExpandedChange)
            }
            ContactSection()
        }
    }
    uiState.alreadyPurchasedAnimal?.let { animal ->
        AlreadyPurchasedDialog(
            animal = animal,
            onDismiss = { onEvent(SupportUiEvents.AlreadyPurchasedDialogDismissed) },
        )
    }
    uiState.scheduledDialogStartTime?.let { startTime ->
        ScheduledDialog(
            startTime = startTime,
            onDismiss = { onEvent(SupportUiEvents.ScheduledDialogDismissed) },
        )
    }
}

@Composable
private fun ScheduledDialog(startTime: Instant, onDismiss: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = mokoString(SharedRes.strings.support_purchase_scheduled_title)) },
        text = {
            Text(
                text = mokoString(SharedRes.strings.support_scheduled_dialog_message, startTime.formatLongDate(locale)),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = mokoString(SharedRes.strings.support_purchase_message_dismiss))
            }
        },
    )
}

@Composable
private fun AlreadyPurchasedDialog(animal: SupportAnimal, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = mokoString(SharedRes.strings.support_already_purchased_title)) },
        text = {
            Text(text = mokoString(SharedRes.strings.support_already_purchased_message, mokoString(animal.nameRes)))
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = mokoString(SharedRes.strings.support_purchase_message_dismiss))
            }
        },
    )
}

@Composable
private fun IntroCard(expandedReasons: List<Int>, onReasonExpandedChange: (Int, Boolean) -> Unit) {
    val cardColor = MaterialTheme.colorScheme.surface
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = cardColor,
        shadowElevation = 0.5.dp,
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = Dimens.SmallMedium)) {
            SupportAnimalBadges(ringColor = cardColor)
            Text(
                text = mokoString(SharedRes.strings.support_intro_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                text = mokoString(SharedRes.strings.support_intro_message),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.SmallMedium),
            )
            Reasons(expandedReasons = expandedReasons, onReasonExpandedChange = onReasonExpandedChange)
        }
    }
}

@Composable
private fun AboutSection(expandedReasons: List<Int>, onReasonExpandedChange: (Int, Boolean) -> Unit) {
    Column {
        SectionHeader(
            title = mokoString(SharedRes.strings.support_about_title),
            modifier = Modifier.padding(top = 28.dp, bottom = Dimens.Medium),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.5.dp,
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = Dimens.SmallMedium)) {
                Reasons(
                    expandedReasons = expandedReasons,
                    onReasonExpandedChange = onReasonExpandedChange,
                    showFirstDivider = false,
                )
            }
        }
    }
}

@Composable
private fun Reasons(
    expandedReasons: List<Int>,
    onReasonExpandedChange: (Int, Boolean) -> Unit,
    showFirstDivider: Boolean = true,
) {
    SupportSection.entries.forEachIndexed { index, section ->
        SupportReason(
            title = mokoString(section.title),
            message = mokoString(section.message),
            isExpanded = index in expandedReasons,
            onExpandedChange = { isExpanded -> onReasonExpandedChange(index, isExpanded) },
            showDivider = showFirstDivider || index > 0,
            modifier = Modifier.testTag(TestTags.supportReason(index)),
        )
    }
}

@Composable
private fun TiersLoading() {
    val loadingDescription = mokoString(SharedRes.strings.support_a11y_loading)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .testTag(TestTags.SUPPORT_LOADING)
            .semantics { contentDescription = loadingDescription },
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun TierSection(
    tiers: List<SupportTier>,
    title: String,
    note: String,
    purchasingAnimal: SupportAnimal?,
    onTierClick: (SupportAnimal) -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column {
        SectionHeader(
            title = title,
            modifier = Modifier.padding(top = 28.dp, bottom = Dimens.Medium),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            tiers.forEach { tier ->
                SupportTierTile(
                    animal = tier.animal,
                    name = mokoString(tier.animal.nameRes),
                    price = tier.price?.format(locale),
                    perMonthSuffix = if (tier.animal.type == SupportType.RECURRING) {
                        mokoString(SharedRes.strings.support_price_per_month)
                    } else {
                        null
                    },
                    onClick = { onTierClick(tier.animal) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    isActive = tier.isActive,
                    scheduledDate = tier.scheduledStartTime?.formatShortDate(locale),
                    isPurchasing = tier.animal == purchasingAnimal,
                    isEnabled = purchasingAnimal == null,
                )
            }
        }
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(start = Dimens.Small, end = Dimens.Small, top = Dimens.Medium),
        )
    }
}

@Composable
private fun PurchaseMessage(purchaseMessage: SupportPurchaseMessage?, onDismiss: () -> Unit) {
    AnimatedContent(targetState = purchaseMessage, label = "supportPurchaseMessage") { message ->
        if (message != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.5.dp,
            ) {
                InfoView(
                    infoViewData = message.toInfoViewData(),
                    primaryActionText = mokoString(SharedRes.strings.support_purchase_message_dismiss),
                    onPrimaryActionClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.Large, vertical = 24.dp)
                        .testTag(TestTags.SUPPORT_PURCHASE_MESSAGE),
                )
            }
        }
    }
}

@Composable
private fun ManageSubscriptionsButton(onClick: () -> Unit) {
    val context = LocalContext.current
    TextButton(
        onClick = {
            onClick()
            context.openUrl(PLAY_SUBSCRIPTIONS_URL.format(context.packageName))
        },
        modifier = Modifier.testTag(TestTags.SUPPORT_MANAGE_SUBSCRIPTIONS),
    ) {
        Text(text = mokoString(SharedRes.strings.support_manage_subscriptions))
    }
}

private fun SupportPurchaseMessage.toInfoViewData(): InfoViewData =
    when (this) {
        SupportPurchaseMessage.PENDING -> InfoViewData(
            infoViewType = InfoViewType.INFO,
            icon = SharedRes.images.ic_schedule,
            title = SharedRes.strings.support_purchase_pending_title,
            message = SharedRes.strings.support_purchase_pending_message,
        )
        SupportPurchaseMessage.SCHEDULED -> InfoViewData(
            infoViewType = InfoViewType.INFO,
            icon = SharedRes.images.ic_schedule,
            title = SharedRes.strings.support_purchase_scheduled_title,
            message = SharedRes.strings.support_purchase_scheduled_message,
        )
        SupportPurchaseMessage.FAILED -> InfoViewData(
            infoViewType = InfoViewType.ERROR,
            icon = SharedRes.images.ic_error,
            title = SharedRes.strings.support_purchase_failed_title,
            message = SharedRes.strings.support_purchase_failed_message,
        )
    }

@Composable
private fun ContactSection() {
    val context = LocalContext.current
    val primary = mokoColor(SharedRes.colors.primary)
    val sendEmailLabel = mokoString(SharedRes.strings.support_a11y_send_email)
    Column {
        Text(
            text = mokoString(SharedRes.strings.support_contact_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(start = Dimens.Small, end = Dimens.Small, top = 26.dp, bottom = Dimens.Medium),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.SUPPORT_EMAIL_ROW)
                .semantics(mergeDescendants = true) { contentDescription = sendEmailLabel },
            shape = CardShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.5.dp,
            onClick = {
                context.sendEmail(
                    email = context.resolveMoko(SharedRes.strings.menu_contact_email),
                    subject = context.resolveMoko(SharedRes.strings.menu_email_subject_android),
                )
            },
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 54.dp)
                    .padding(horizontal = Dimens.Large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(primary, RoundedCornerShape(Dimens.SmallMedium)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(SharedRes.images.ic_email.drawableResId),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(Dimens.IconExtraSmall),
                    )
                }
                Text(
                    text = mokoString(SharedRes.strings.menu_contact_email),
                    style = MaterialTheme.typography.bodyLarge,
                    color = primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Dimens.MediumLarge),
                )
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconExtraSmall),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SupportContentPreview() {
    HuKiTheme {
        SupportContent(
            uiState = SupportUiState(
                tiers = SupportAnimal.entries.zip(PreviewPrices) { animal, price -> SupportTier(animal, price) },
                pricesState = SupportPricesState.LOADED,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SupportContentSupporterPreview() {
    HuKiTheme {
        SupportContent(
            uiState = SupportUiState(
                tiers = SupportAnimal.entries.zip(PreviewPrices) { animal, price -> SupportTier(animal, price) },
                pricesState = SupportPricesState.LOADED,
                supporterAnimal = SupportAnimal.OWL,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SupportContentLoadingPreview() {
    HuKiTheme {
        SupportContent(uiState = SupportUiState.Default, onEvent = {}, onBack = {})
    }
}

@Preview
@Composable
private fun SupportContentUnavailablePreview() {
    HuKiTheme {
        SupportContent(
            uiState = SupportUiState(pricesState = SupportPricesState.UNAVAILABLE),
            onEvent = {},
            onBack = {},
        )
    }
}
