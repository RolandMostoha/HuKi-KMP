package hu.mostoha.mobile.kmp.huki.features.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.BillingProduct
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportRestoreResult
import hu.mostoha.mobile.kmp.huki.model.domain.SupportSection
import hu.mostoha.mobile.kmp.huki.model.domain.SupportType
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import hu.mostoha.mobile.kmp.huki.repository.SupporterRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.BillingException
import hu.mostoha.mobile.kmp.huki.service.BillingService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.fetchProducts
import hu.mostoha.mobile.kmp.huki.service.logScreenView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupportViewModel(
    private val billingService: BillingService,
    private val supporterRepository: SupporterRepository,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupportUiState.Default)
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    /**
     * The purchase waiting for approval, completed once the store reports it as owned.
     */
    private var pendingPurchase: PendingPurchase? = null

    /**
     * The scheduled subscription seen last, celebrated once the store reports it as active.
     */
    private var scheduledAnimal: SupportAnimal? = null

    init {
        initLogging()
        loadPrices()
        observeSupporterState()
        viewModelScope.launch { supporterRepository.refresh() }
    }

    fun onEvent(event: SupportUiEvents) {
        Logger.d { "SupportEvent: $event" }
        when (event) {
            SupportUiEvents.ScreenViewed -> analyticsService.logScreenView(Screen.SUPPORT)
            is SupportUiEvents.SectionOpened -> onSectionOpened(event.section)
            SupportUiEvents.RetryClicked -> loadPrices()
            is SupportUiEvents.TierClicked -> onTierClicked(event.animal)
            SupportUiEvents.PurchaseMessageDismissed -> _uiState.update { it.copy(purchaseMessage = null) }
            SupportUiEvents.AlreadyPurchasedDialogDismissed -> _uiState.update {
                it.copy(alreadyPurchasedAnimal = null)
            }
            SupportUiEvents.ScheduledDialogDismissed -> _uiState.update { it.copy(scheduledDialogStartTime = null) }
            is SupportUiEvents.SupporterAnimalChosen -> onSupporterAnimalChosen(event.animal)
            SupportUiEvents.ManageSubscriptionsClicked -> onManageSubscriptionsClicked()
            SupportUiEvents.RestorePurchasesClicked -> onRestorePurchasesClicked()
            SupportUiEvents.RestoreMessageDismissed -> _uiState.update { it.copy(restoreMessage = null) }
        }
    }

    private fun onSectionOpened(section: SupportSection) {
        analyticsService.logEvent(AnalyticsEvent.SupportSectionOpened(section))
    }

    private fun loadPrices() {
        _uiState.update { it.copy(pricesState = SupportPricesState.LOADING) }
        viewModelScope.launch {
            try {
                val products = billingService.fetchProducts(SupportAnimal.entries.map { it.productId })
                _uiState.update { it.withPrices(products) }
            } catch (exception: BillingException) {
                Logger.w(exception) { "Billing: Failed to fetch support products." }
                _uiState.update { it.copy(pricesState = SupportPricesState.UNAVAILABLE) }
            }
            if (_uiState.value.pricesState == SupportPricesState.UNAVAILABLE) {
                analyticsService.logEvent(AnalyticsEvent.SupportPricesUnavailable)
            }
        }
    }

    private fun observeSupporterState() {
        supporterRepository.supporterState
            .onEach { supporterState ->
                _uiState.update { it.withSupporterState(supporterState) }
                completePendingPurchase(supporterState)
                completeScheduledSubscription(supporterState)
            }
            .launchIn(viewModelScope)
    }

    private fun onTierClicked(animal: SupportAnimal) {
        val uiState = _uiState.value
        if (uiState.purchasingAnimal != null || uiState.isRestoring) {
            return
        }
        val tier = uiState.tiers.firstOrNull { it.animal == animal }
        if (tier?.isActive == true) {
            _uiState.update { it.copy(alreadyPurchasedAnimal = animal) }
            return
        }
        if (tier?.scheduledStartTime != null) {
            _uiState.update { it.copy(scheduledDialogStartTime = tier.scheduledStartTime) }
            return
        }
        _uiState.update { it.copy(purchasingAnimal = animal, purchaseMessage = null) }
        analyticsService.logEvent(AnalyticsEvent.SupportPurchaseStarted(animal))
        viewModelScope.launch {
            onPurchaseResult(animal, supporterRepository.purchase(animal))
        }
    }

    private fun onPurchaseResult(animal: SupportAnimal, result: BillingPurchaseResult) {
        when (result) {
            is BillingPurchaseResult.Success -> {
                analyticsService.logEvent(AnalyticsEvent.SupportPurchaseCompleted(animal))
                scheduledAnimal = scheduledAnimal.takeUnless { it == animal }
                _uiState.update {
                    it.copy(purchasingAnimal = null)
                        .withSupporterState(supporterRepository.supporterState.value)
                        .copy(supporterAnimal = animal, celebrationId = it.celebrationId + 1)
                }
            }
            is BillingPurchaseResult.Scheduled -> {
                analyticsService.logEvent(AnalyticsEvent.SupportPurchaseScheduled(animal))
                _uiState.update {
                    it.copy(purchasingAnimal = null, purchaseMessage = SupportPurchaseMessage.SCHEDULED)
                        .withSupporterState(supporterRepository.supporterState.value)
                }
            }
            BillingPurchaseResult.Cancelled -> {
                analyticsService.logEvent(AnalyticsEvent.SupportPurchaseCancelled(animal))
                _uiState.update {
                    it.copy(purchasingAnimal = null).withSupporterState(supporterRepository.supporterState.value)
                }
            }
            BillingPurchaseResult.Pending -> {
                val purchaseCount = supporterRepository.supporterState.value.oneTimePurchaseCounts[animal] ?: 0
                pendingPurchase = PendingPurchase(animal, purchaseCount)
                analyticsService.logEvent(AnalyticsEvent.SupportPurchasePending)
                _uiState.update {
                    it.copy(purchasingAnimal = null, purchaseMessage = SupportPurchaseMessage.PENDING)
                        .withSupporterState(supporterRepository.supporterState.value)
                }
            }
            is BillingPurchaseResult.Failed -> {
                Logger.w { "Billing: Purchase failed: ${result.message}" }
                crashlyticsService.recordException(BillingException(result.message))
                analyticsService.logEvent(AnalyticsEvent.SupportPurchaseFailed)
                _uiState.update {
                    it.copy(purchasingAnimal = null, purchaseMessage = SupportPurchaseMessage.FAILED)
                        .withSupporterState(supporterRepository.supporterState.value)
                }
            }
        }
    }

    private fun completePendingPurchase(supporterState: SupporterState) {
        val pending = pendingPurchase ?: return
        val isCompleted = when (pending.animal.type) {
            SupportType.ONE_TIME -> (supporterState.oneTimePurchaseCounts[pending.animal] ?: 0) > pending.purchaseCount
            SupportType.RECURRING -> pending.animal in supporterState.activeSubscriptions
        }
        if (!isCompleted) {
            return
        }
        pendingPurchase = null
        analyticsService.logEvent(AnalyticsEvent.SupportPurchaseCompleted(pending.animal))
        _uiState.update { uiState ->
            uiState.copy(
                purchaseMessage = uiState.purchaseMessage.takeUnless { it == SupportPurchaseMessage.PENDING },
                supporterAnimal = pending.animal,
                celebrationId = uiState.celebrationId + 1,
            )
        }
        viewModelScope.launch { supporterRepository.chooseAnimal(pending.animal) }
    }

    private fun completeScheduledSubscription(supporterState: SupporterState) {
        val animal = scheduledAnimal
        scheduledAnimal =
            supporterState.scheduledSubscription?.animal?.takeUnless { it in supporterState.activeSubscriptions }
        // A purchase in progress celebrates through its own result
        if (animal == null ||
            animal !in supporterState.activeSubscriptions ||
            _uiState.value.purchasingAnimal != null
        ) {
            return
        }
        _uiState.update { uiState ->
            uiState.copy(
                purchaseMessage = uiState.purchaseMessage.takeUnless { it == SupportPurchaseMessage.SCHEDULED },
                supporterAnimal = animal,
                celebrationId = uiState.celebrationId + 1,
            )
        }
        viewModelScope.launch { supporterRepository.chooseAnimal(animal) }
    }

    private fun onManageSubscriptionsClicked() {
        analyticsService.logEvent(AnalyticsEvent.SupportManageSubscriptionsClicked)
    }

    private fun onRestorePurchasesClicked() {
        val uiState = _uiState.value
        if (uiState.isRestoring || uiState.purchasingAnimal != null) {
            return
        }
        _uiState.update { it.copy(isRestoring = true) }
        analyticsService.logEvent(AnalyticsEvent.SupportRestoreClicked)
        viewModelScope.launch {
            onRestoreResult(supporterRepository.restore())
        }
    }

    private fun onRestoreResult(result: SupportRestoreResult) {
        val restoreMessage = when (result) {
            SupportRestoreResult.Restored -> {
                analyticsService.logEvent(AnalyticsEvent.SupportRestoreCompleted)
                SupportRestoreMessage.RESTORED
            }
            SupportRestoreResult.NothingToRestore -> {
                analyticsService.logEvent(AnalyticsEvent.SupportRestoreNothingToRestore)
                SupportRestoreMessage.NOTHING_TO_RESTORE
            }
            SupportRestoreResult.Cancelled -> null
            is SupportRestoreResult.Failed -> {
                Logger.w { "Billing: Restore failed: ${result.message}" }
                crashlyticsService.recordException(BillingException(result.message))
                analyticsService.logEvent(AnalyticsEvent.SupportRestoreFailed)
                SupportRestoreMessage.FAILED
            }
        }
        _uiState.update { it.copy(isRestoring = false, restoreMessage = restoreMessage) }
    }

    private fun onSupporterAnimalChosen(animal: SupportAnimal) {
        if (animal == _uiState.value.supporterAnimal) {
            return
        }
        viewModelScope.launch { supporterRepository.chooseAnimal(animal) }
    }

    private fun SupportUiState.withPrices(products: List<BillingProduct>): SupportUiState {
        val prices = products.associate { it.productId to it.price }
        return copy(
            tiers = tiers.map { it.copy(price = prices[it.animal.productId]) },
            pricesState = if (prices.isEmpty()) SupportPricesState.UNAVAILABLE else SupportPricesState.LOADED,
        )
    }

    private fun SupportUiState.withSupporterState(supporterState: SupporterState): SupportUiState {
        val tiers = tiers.map { tier ->
            tier.copy(
                isActive = tier.animal in supporterState.activeSubscriptions,
                scheduledStartTime = supporterState.scheduledSubscription?.takeIf {
                    it.animal == tier.animal
                }?.startTime,
            )
        }
        // Held during a purchase, its result shows the new animal and choices together with the celebration
        if (purchasingAnimal != null) {
            return copy(tiers = tiers)
        }
        return copy(
            tiers = tiers,
            supporterAnimal = supporterState.supporterAnimal,
            supporterChoices = supporterState.ownedAnimals.sortedByDescending {
                supporterState.lastPurchaseTimes[it]
            }.map { animal ->
                SupporterChoice(animal = animal, purchaseCount = supporterState.oneTimePurchaseCounts[animal] ?: 1)
            },
        )
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "SupportState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }

    /**
     * [purchaseCount] is the one-time count before the purchase, a completed one raises it.
     */
    private data class PendingPurchase(
        val animal: SupportAnimal,
        val purchaseCount: Int,
    )
}
