package hu.mostoha.mobile.kmp.huki.features.support

import app.cash.turbine.test
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.BillingProduct
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchase
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.ProductPrice
import hu.mostoha.mobile.kmp.huki.model.domain.ScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportSection
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import hu.mostoha.mobile.kmp.huki.repository.FakeSupporterRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeBillingService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SupportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val billingService = FakeBillingService()
    private val supporterRepository = FakeSupporterRepository()
    private val analyticsService = FakeAnalyticsService()
    private val crashlyticsService = FakeCrashlyticsService()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SupportViewModel {
        val viewModel = SupportViewModel(billingService, supporterRepository, analyticsService, crashlyticsService)
        testDispatcher.scheduler.runCurrent()
        return viewModel
    }

    @Test
    fun `Given view model - When created - Then every support product is requested`() {
        createViewModel()

        billingService.requestedProductIds shouldBe listOf(SupportAnimal.entries.map { it.productId })
    }

    @Test
    fun `Given store products - When created - Then tiers show the store prices`() =
        runTest {
            billingService.products = SupportAnimal.entries.map { BillingProduct(it.productId, ProductPrice(it.ordinal * 1_000_000L, "HUF")) }

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() shouldBe SupportUiState(
                    tiers = SupportAnimal.entries.map { SupportTier(animal = it, price = ProductPrice(it.ordinal * 1_000_000L, "HUF")) },
                    pricesState = SupportPricesState.LOADED,
                )
            }
        }

    @Test
    fun `Given a product missing from the store - When created - Then its tier has no price`() =
        runTest {
            billingService.products = listOf(BillingProduct(SupportAnimal.OWL.productId, OWL_PRICE))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val tiers = awaitItem().tiers
                tiers.single { it.animal == SupportAnimal.OWL }.price shouldBe OWL_PRICE
                tiers.single { it.animal == SupportAnimal.BEAR }.price shouldBe null
            }
        }

    @Test
    fun `Given no store products - When created - Then prices are unavailable`() =
        runTest {
            billingService.products = emptyList()

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem().pricesState shouldBe SupportPricesState.UNAVAILABLE
            }
        }

    @Test
    fun `Given no store products - When created - Then unavailable prices are logged`() {
        billingService.products = emptyList()

        createViewModel()

        analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SupportPricesUnavailable)
    }

    @Test
    fun `Given a store error - When created - Then unavailable prices are logged`() {
        billingService.errorMessage = "The Internet connection appears to be offline."

        createViewModel()

        analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SupportPricesUnavailable)
    }

    @Test
    fun `Given store products - When created - Then nothing is logged`() {
        billingService.products = listOf(BillingProduct(SupportAnimal.OWL.productId, OWL_PRICE))

        createViewModel()

        analyticsService.loggedEvents shouldBe emptyList()
    }

    @Test
    fun `Given a store error - When created - Then prices are unavailable`() =
        runTest {
            billingService.errorMessage = "The Internet connection appears to be offline."

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() shouldBe SupportUiState.Default.copy(pricesState = SupportPricesState.UNAVAILABLE)
            }
        }

    @Test
    fun `Given unavailable prices - When retry clicked - Then prices load again`() =
        runTest {
            billingService.errorMessage = "The Internet connection appears to be offline."
            val viewModel = createViewModel()
            billingService.errorMessage = null
            billingService.products = listOf(BillingProduct(SupportAnimal.OWL.productId, OWL_PRICE))

            viewModel.uiState.test {
                awaitItem().pricesState shouldBe SupportPricesState.UNAVAILABLE

                viewModel.onEvent(SupportUiEvents.RetryClicked)

                awaitItem().pricesState shouldBe SupportPricesState.LOADING
                testDispatcher.scheduler.runCurrent()
                awaitItem().tiers.first { it.animal == SupportAnimal.OWL }.price shouldBe OWL_PRICE
            }
        }

    @Test
    fun `Given view model - When created - Then supporter state is refreshed`() {
        createViewModel()

        supporterRepository.refreshCount shouldBe 1
    }

    @Test
    fun `Given supporter state - When created - Then active subscriptions and supporter animal show`() {
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BOAR),
            oneTimePurchaseCounts = mapOf(SupportAnimal.HEDGEHOG to 1),
        )

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value
        uiState.supporterAnimal shouldBe SupportAnimal.BOAR
        uiState.tiers.filter { it.isActive }.map { it.animal } shouldBe listOf(SupportAnimal.BOAR)
    }

    @Test
    fun `Given successful purchase - When tier clicked - Then the purchased animal becomes the supporter animal`() {
        billingService.products = listOf(BillingProduct(SupportAnimal.OWL.productId, OWL_PRICE))
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BEAR))
        supporterRepository.purchaseResult = BillingPurchaseResult.Success(OWL_PURCHASE)
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.value
        uiState.supporterAnimal shouldBe SupportAnimal.OWL
        uiState.celebrationId shouldBe 1
        uiState.purchasingAnimal shouldBe null
        uiState.purchaseMessage shouldBe null
        analyticsService.loggedEvents shouldBe listOf(
            AnalyticsEvent.SupportPurchaseStarted(SupportAnimal.OWL),
            AnalyticsEvent.SupportPurchaseCompleted(SupportAnimal.OWL),
        )
    }

    @Test
    fun `Given cancelled purchase - When tier clicked - Then nothing changes`() {
        supporterRepository.purchaseResult = BillingPurchaseResult.Cancelled
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.value
        uiState.supporterAnimal shouldBe null
        uiState.purchasingAnimal shouldBe null
        uiState.purchaseMessage shouldBe null
        analyticsService.loggedEvents.last() shouldBe AnalyticsEvent.SupportPurchaseCancelled(SupportAnimal.OWL)
    }

    @Test
    fun `Given pending purchase - When tier clicked - Then the pending message shows`() {
        supporterRepository.purchaseResult = BillingPurchaseResult.Pending
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.purchaseMessage shouldBe SupportPurchaseMessage.PENDING
        analyticsService.loggedEvents.last() shouldBe AnalyticsEvent.SupportPurchasePending
    }

    @Test
    fun `Given pending one-time purchase - When it completes - Then the message hides and the animal is chosen`() {
        supporterRepository.supporterState.value = SupporterState(oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 1))
        supporterRepository.purchaseResult = BillingPurchaseResult.Pending
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        supporterRepository.supporterState.value = SupporterState(oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 2))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.purchaseMessage shouldBe null
        supporterRepository.chosenAnimals shouldBe listOf(SupportAnimal.OWL)
        analyticsService.loggedEvents.last() shouldBe AnalyticsEvent.SupportPurchaseCompleted(SupportAnimal.OWL)
    }

    @Test
    fun `Given pending subscription - When it becomes active - Then the message hides`() {
        supporterRepository.purchaseResult = BillingPurchaseResult.Pending
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.DEER))
        testDispatcher.scheduler.runCurrent()

        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.DEER))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.purchaseMessage shouldBe null
        viewModel.uiState.value.supporterAnimal shouldBe SupportAnimal.DEER
        viewModel.uiState.value.celebrationId shouldBe 1
    }

    @Test
    fun `Given ongoing purchase - When supporter state changes - Then the supporter animal waits for the result`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BEAR))
        supporterRepository.purchaseGate = CompletableDeferred()
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BEAR),
            oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 1),
            chosenAnimal = SupportAnimal.OWL,
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.supporterAnimal shouldBe SupportAnimal.BEAR
        viewModel.uiState.value.celebrationId shouldBe 0
    }

    @Test
    fun `Given scheduled subscription purchase - When tier clicked - Then the switch is announced without celebration`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BEAR))
        supporterRepository.purchaseResult = BillingPurchaseResult.Scheduled(SWITCH_TIME)
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.BOAR))
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.value
        uiState.supporterAnimal shouldBe SupportAnimal.BEAR
        uiState.celebrationId shouldBe 0
        uiState.purchaseMessage shouldBe SupportPurchaseMessage.SCHEDULED
        uiState.tiers.first { it.animal == SupportAnimal.BOAR }.scheduledStartTime shouldBe SWITCH_TIME
        analyticsService.loggedEvents.last() shouldBe AnalyticsEvent.SupportPurchaseScheduled(SupportAnimal.BOAR)
    }

    @Test
    fun `Given scheduled subscription - When its tier clicked - Then no purchase starts and the start time shows`() {
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BEAR),
            scheduledSubscription = ScheduledSubscription(SupportAnimal.BOAR, SWITCH_TIME),
        )
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.BOAR))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.scheduledDialogStartTime shouldBe SWITCH_TIME
        supporterRepository.purchasedAnimals shouldBe emptyList()
    }

    @Test
    fun `Given scheduled dialog - When dismissed - Then the dialog hides`() {
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BEAR),
            scheduledSubscription = ScheduledSubscription(SupportAnimal.BOAR, SWITCH_TIME),
        )
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.BOAR))

        viewModel.onEvent(SupportUiEvents.ScheduledDialogDismissed)

        viewModel.uiState.value.scheduledDialogStartTime shouldBe null
    }

    @Test
    fun `Given scheduled subscription - When it starts - Then it is celebrated and chosen`() {
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BEAR),
            scheduledSubscription = ScheduledSubscription(SupportAnimal.BOAR, SWITCH_TIME),
        )
        val viewModel = createViewModel()
        testDispatcher.scheduler.runCurrent()

        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BOAR))
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.value
        uiState.supporterAnimal shouldBe SupportAnimal.BOAR
        uiState.celebrationId shouldBe 1
        uiState.tiers.first { it.animal == SupportAnimal.BOAR }.scheduledStartTime shouldBe null
        supporterRepository.chosenAnimals shouldBe listOf(SupportAnimal.BOAR)
    }

    @Test
    fun `Given upgrade reported as scheduled - When it completes - Then it is celebrated once`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BOAR))
        supporterRepository.purchaseResult = BillingPurchaseResult.Success(BEAR_PURCHASE)
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.BEAR))
        testDispatcher.scheduler.runCurrent()
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BOAR, SupportAnimal.BEAR),
            scheduledSubscription = ScheduledSubscription(SupportAnimal.BEAR, SWITCH_TIME),
        )
        testDispatcher.scheduler.runCurrent()
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BEAR))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.celebrationId shouldBe 1
    }

    @Test
    fun `Given ongoing purchase - When supporter state changes - Then the supporter choices wait for the result`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BEAR))
        supporterRepository.purchaseGate = CompletableDeferred()
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BEAR),
            oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 1),
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.supporterChoices shouldBe listOf(SupporterChoice(SupportAnimal.BEAR, purchaseCount = 1))
    }

    @Test
    fun `Given successful purchase of another animal - When it completes - Then the choices show it with the celebration`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BEAR))
        supporterRepository.purchaseResult = BillingPurchaseResult.Success(OWL_PURCHASE)
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.value
        uiState.supporterAnimal shouldBe SupportAnimal.OWL
        uiState.celebrationId shouldBe 1
        uiState.supporterChoices shouldBe listOf(
            SupporterChoice(SupportAnimal.OWL, purchaseCount = 1),
            SupporterChoice(SupportAnimal.BEAR, purchaseCount = 1),
        )
    }

    @Test
    fun `Given pending purchase - When another animal changes - Then the pending message stays`() {
        supporterRepository.purchaseResult = BillingPurchaseResult.Pending
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.DEER))
        testDispatcher.scheduler.runCurrent()

        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BEAR))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.purchaseMessage shouldBe SupportPurchaseMessage.PENDING
        supporterRepository.chosenAnimals shouldBe emptyList()
    }

    @Test
    fun `Given failed purchase - When tier clicked - Then the failed message shows and the error is recorded`() {
        supporterRepository.purchaseResult = BillingPurchaseResult.Failed("Play Billing error 6")
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.purchaseMessage shouldBe SupportPurchaseMessage.FAILED
        crashlyticsService.recordedExceptions.single().message shouldBe "Play Billing error 6"
        analyticsService.loggedEvents.last() shouldBe AnalyticsEvent.SupportPurchaseFailed
    }

    @Test
    fun `Given purchase message - When dismissed - Then the message hides`() {
        supporterRepository.purchaseResult = BillingPurchaseResult.Failed("Play Billing error 6")
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(SupportUiEvents.PurchaseMessageDismissed)

        viewModel.uiState.value.purchaseMessage shouldBe null
    }

    @Test
    fun `Given active subscription - When its tier clicked - Then no purchase starts and the dialog shows`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BOAR))
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.BOAR))
        testDispatcher.scheduler.runCurrent()

        supporterRepository.purchasedAnimals shouldBe emptyList()
        viewModel.uiState.value.alreadyPurchasedAnimal shouldBe SupportAnimal.BOAR
    }

    @Test
    fun `Given already purchased dialog - When dismissed - Then the dialog hides`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BOAR))
        val viewModel = createViewModel()
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.BOAR))

        viewModel.onEvent(SupportUiEvents.AlreadyPurchasedDialogDismissed)

        viewModel.uiState.value.alreadyPurchasedAnimal shouldBe null
    }

    @Test
    fun `Given ongoing purchase - When another tier clicked - Then no second purchase starts`() {
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.OWL))
        viewModel.onEvent(SupportUiEvents.TierClicked(SupportAnimal.BEAR))

        viewModel.uiState.value.purchasingAnimal shouldBe SupportAnimal.OWL
        testDispatcher.scheduler.runCurrent()
        supporterRepository.purchasedAnimals shouldBe listOf(SupportAnimal.OWL)
    }

    @Test
    fun `Given owned animals - When created - Then they are the supporter choices with purchase counts`() {
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BOAR),
            oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 3),
        )

        val viewModel = createViewModel()

        viewModel.uiState.value.supporterChoices shouldBe listOf(
            SupporterChoice(SupportAnimal.OWL, purchaseCount = 3),
            SupporterChoice(SupportAnimal.BOAR, purchaseCount = 1),
        )
    }

    @Test
    fun `Given owned animals bought at different times - When created - Then the latest purchase is the first choice`() {
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BEAR),
            oneTimePurchaseCounts = mapOf(SupportAnimal.SQUIRREL to 1, SupportAnimal.OWL to 2),
            lastPurchaseTimes = mapOf(
                SupportAnimal.SQUIRREL to Instant.fromEpochMilliseconds(1_000),
                SupportAnimal.OWL to Instant.fromEpochMilliseconds(3_000),
                SupportAnimal.BEAR to Instant.fromEpochMilliseconds(2_000),
            ),
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.supporterChoices.map { it.animal } shouldBe listOf(
            SupportAnimal.OWL,
            SupportAnimal.BEAR,
            SupportAnimal.SQUIRREL,
        )
    }

    @Test
    fun `Given supporter choices - When another animal chosen - Then it becomes the supporter animal`() {
        supporterRepository.supporterState.value = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.BOAR),
            oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 1),
        )
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.SupporterAnimalChosen(SupportAnimal.OWL))
        testDispatcher.scheduler.runCurrent()

        viewModel.uiState.value.supporterAnimal shouldBe SupportAnimal.OWL
        viewModel.uiState.value.celebrationId shouldBe 0
        supporterRepository.chosenAnimals shouldBe listOf(SupportAnimal.OWL)
    }

    @Test
    fun `Given supporter animal - When it is chosen again - Then nothing is stored`() {
        supporterRepository.supporterState.value = SupporterState(activeSubscriptions = setOf(SupportAnimal.BOAR))
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.SupporterAnimalChosen(SupportAnimal.BOAR))
        testDispatcher.scheduler.runCurrent()

        supporterRepository.chosenAnimals shouldBe emptyList()
    }

    @Test
    fun `Given support sections - When opened - Then each opened section is logged`() {
        val viewModel = createViewModel()

        SupportSection.entries.forEach { viewModel.onEvent(SupportUiEvents.SectionOpened(it)) }

        analyticsService.loggedEvents.takeLast(3) shouldBe listOf(
            AnalyticsEvent.SupportSectionOpened(SupportSection.SPENDING),
            AnalyticsEvent.SupportSectionOpened(SupportSection.WHY),
            AnalyticsEvent.SupportSectionOpened(SupportSection.REWARDS),
        )
    }

    @Test
    fun `Given view model - When screen viewed - Then support screen view is logged`() {
        val viewModel = createViewModel()

        viewModel.onEvent(SupportUiEvents.ScreenViewed)

        analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.SUPPORT))
    }

    companion object {
        private val SWITCH_TIME = Instant.fromEpochMilliseconds(1_700_100_000_000)
        private val BEAR_PURCHASE = BillingPurchase(
            productId = SupportAnimal.BEAR.productId,
            purchaseToken = "bear_token",
            purchaseTimeMillis = 0,
            quantity = 1,
        )
        private val OWL_PRICE = ProductPrice(amountMicros = 999_000_000, currencyCode = "HUF")
        private val OWL_PURCHASE = BillingPurchase(
            productId = SupportAnimal.OWL.productId,
            purchaseToken = "token",
            purchaseTimeMillis = 0,
            quantity = 1,
        )
    }
}
