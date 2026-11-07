package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.preferences.core.stringSetPreferencesKey
import hu.mostoha.mobile.kmp.huki.model.analytics.SupporterLevel
import hu.mostoha.mobile.kmp.huki.model.analytics.UserProperty
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchase
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.BillingScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.domain.ScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeBillingService
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.IOException
import kotlin.test.Test
import kotlin.time.Instant

class DefaultSupporterRepositoryTest {

    private val billingService = FakeBillingService()
    private val dataStore = FakeDataStore()
    private val analyticsService = FakeAnalyticsService()

    private suspend fun TestScope.createRepository(): DefaultSupporterRepository {
        val repository = DefaultSupporterRepository(billingService, dataStore, analyticsService, backgroundScope)
        repository.refresh()
        runCurrent()
        return repository
    }

    @Test
    fun `Given store entitlements - When refreshed - Then subscriptions and one-time purchases are stored`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.DEER), purchase(SupportAnimal.OWL))

            val repository = createRepository()

            repository.supporterState.value shouldBe SupporterState(
                activeSubscriptions = setOf(SupportAnimal.DEER),
                oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 1),
                lastPurchaseTimes = mapOf(SupportAnimal.OWL to PURCHASE_INSTANT, SupportAnimal.DEER to PURCHASE_INSTANT),
            )
        }

    @Test
    fun `Given store entitlements - When refreshed - Then the supporter level is set`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.DEER), purchase(SupportAnimal.OWL))

            createRepository()

            analyticsService.userProperties shouldBe listOf(UserProperty.Supporter(SupporterLevel.RECURRING))
        }

    @Test
    fun `Given failed entitlements fetch - When refreshed - Then the supporter level is not set`() =
        runTest {
            billingService.entitlementsErrorMessage = "The Internet connection appears to be offline."

            createRepository()

            analyticsService.userProperties shouldBe emptyList()
        }

    @Test
    fun `Given one-time supporter - When a subscription is purchased - Then the supporter level changes`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.OWL))
            val repository = createRepository()
            billingService.entitlements = listOf(purchase(SupportAnimal.OWL), purchase(SupportAnimal.DEER))
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.DEER))

            repository.purchase(SupportAnimal.DEER)
            runCurrent()

            analyticsService.userProperties shouldBe listOf(
                UserProperty.Supporter(SupporterLevel.ONE_TIME),
                UserProperty.Supporter(SupporterLevel.RECURRING),
            )
        }

    @Test
    fun `Given loaded subscriptions - When refresh fails - Then they are kept`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.DEER))
            val repository = createRepository()
            billingService.entitlementsErrorMessage = "The Internet connection appears to be offline."

            repository.refresh()
            runCurrent()

            repository.supporterState.value.activeSubscriptions shouldBe setOf(SupportAnimal.DEER)
        }

    @Test
    fun `Given cancelled subscription - When refreshed - Then it is no longer active`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.DEER))
            val repository = createRepository()
            billingService.entitlements = emptyList()

            repository.refresh()
            runCurrent()

            repository.supporterState.value.activeSubscriptions shouldBe emptySet()
        }

    @Test
    fun `Given successful purchase - When purchased - Then the one-time purchase is recorded`() =
        runTest {
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.SQUIRREL))
            val repository = createRepository()

            repository.purchase(SupportAnimal.SQUIRREL)
            runCurrent()

            repository.supporterState.value.oneTimePurchaseCounts shouldBe mapOf(SupportAnimal.SQUIRREL to 1)
        }

    @Test
    fun `Given the same purchase twice - When recorded - Then it counts once`() =
        runTest {
            val squirrel = purchase(SupportAnimal.SQUIRREL)
            billingService.purchaseResult = BillingPurchaseResult.Success(squirrel)
            val repository = createRepository()
            repository.purchase(SupportAnimal.SQUIRREL)
            billingService.entitlements = listOf(squirrel)

            repository.refresh()

            dataStore.storedOneTimeEntries() shouldBe setOf("${SupportAnimal.SQUIRREL.productId};1;$PURCHASE_TIME")
        }

    @Test
    fun `Given two purchases of the same product - When recorded - Then the count increments`() =
        runTest {
            val repository = createRepository()
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, token = "first"))
            repository.purchase(SupportAnimal.OWL)
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, token = "second"))

            repository.purchase(SupportAnimal.OWL)

            dataStore.storedOneTimeEntries() shouldBe setOf("${SupportAnimal.OWL.productId};2;$PURCHASE_TIME")
        }

    @Test
    fun `Given purchase completed outside the flow - When received - Then it is recorded`() =
        runTest {
            val repository = createRepository()

            billingService.completePurchaseOutsideFlow(purchase(SupportAnimal.BEAR))
            runCurrent()

            repository.supporterState.value.activeSubscriptions shouldBe setOf(SupportAnimal.BEAR)
        }

    @Test
    fun `Given active subscription - When upgraded in the store - Then only the new subscription is active`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.BOAR))
            val repository = createRepository()
            val bear = purchase(SupportAnimal.BEAR)
            billingService.purchaseResult = BillingPurchaseResult.Success(bear)
            billingService.entitlements = emptyList()

            repository.purchase(SupportAnimal.BEAR)
            runCurrent()

            repository.supporterState.value.activeSubscriptions shouldBe setOf(SupportAnimal.BEAR)
        }

    @Test
    fun `Given parallel subscriptions in the store - When purchased - Then both are active`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.BOAR))
            val repository = createRepository()
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.BEAR))

            repository.purchase(SupportAnimal.BEAR)
            runCurrent()

            repository.supporterState.value.activeSubscriptions shouldBe setOf(SupportAnimal.BOAR, SupportAnimal.BEAR)
        }

    @Test
    fun `Given subscription purchase - When entitlements fail to load - Then the purchase is still active`() =
        runTest {
            val repository = createRepository()
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.DEER))
            billingService.entitlementsErrorMessage = "The Internet connection appears to be offline."

            repository.purchase(SupportAnimal.DEER)
            runCurrent()

            repository.supporterState.value.activeSubscriptions shouldBe setOf(SupportAnimal.DEER)
        }

    @Test
    fun `Given subscription and one-time purchases - When state read - Then the subscription is the supporter animal`() {
        val state = SupporterState(
            activeSubscriptions = setOf(SupportAnimal.DEER, SupportAnimal.BOAR),
            oneTimePurchaseCounts = mapOf(SupportAnimal.HEDGEHOG to 1),
        )

        val actual = state.supporterAnimal

        actual shouldBe SupportAnimal.BOAR
    }

    @Test
    fun `Given one-time purchases only - When state read - Then the highest one is the supporter animal`() {
        val state = SupporterState(oneTimePurchaseCounts = mapOf(SupportAnimal.SQUIRREL to 1, SupportAnimal.HEDGEHOG to 1))

        val actual = state.supporterAnimal

        actual shouldBe SupportAnimal.HEDGEHOG
    }

    @Test
    fun `Given purchase in progress - When the caller is cancelled - Then the purchase is still recorded`() =
        runTest {
            val repository = createRepository()
            billingService.isPurchaseHeld = true
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.SQUIRREL))
            val caller = launch { repository.purchase(SupportAnimal.SQUIRREL) }
            runCurrent()

            caller.cancel()
            billingService.completeHeldPurchase()
            runCurrent()

            repository.supporterState.value.oneTimePurchaseCounts shouldBe mapOf(SupportAnimal.SQUIRREL to 1)
        }

    @Test
    fun `Given owned animals - When one is chosen - Then it is the supporter animal`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.BEAR), purchase(SupportAnimal.SQUIRREL))
            val repository = createRepository()

            repository.chooseAnimal(SupportAnimal.SQUIRREL)
            runCurrent()

            repository.supporterState.value.supporterAnimal shouldBe SupportAnimal.SQUIRREL
        }

    @Test
    fun `Given chosen subscription - When it is no longer active - Then the highest owned animal shows`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.DEER), purchase(SupportAnimal.OWL))
            val repository = createRepository()
            repository.chooseAnimal(SupportAnimal.DEER)
            billingService.entitlements = listOf(purchase(SupportAnimal.OWL))

            repository.refresh()
            runCurrent()

            repository.supporterState.value.supporterAnimal shouldBe SupportAnimal.OWL
        }

    @Test
    fun `Given chosen animal - When another one is purchased - Then the purchased one is chosen`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.BEAR))
            val repository = createRepository()
            repository.chooseAnimal(SupportAnimal.BEAR)
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.SQUIRREL))

            repository.purchase(SupportAnimal.SQUIRREL)
            runCurrent()

            repository.supporterState.value.chosenAnimal shouldBe SupportAnimal.SQUIRREL
        }

    @Test
    fun `Given one-time product bought twice - When state read - Then its count is two`() =
        runTest {
            val repository = createRepository()
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, token = "first"))
            repository.purchase(SupportAnimal.OWL)
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, token = "second"))

            repository.purchase(SupportAnimal.OWL)
            runCurrent()

            repository.supporterState.value.oneTimePurchaseCounts shouldBe mapOf(SupportAnimal.OWL to 2)
        }

    @Test
    fun `Given one-time product bought in quantity of three - When state read - Then its count is three`() =
        runTest {
            val repository = createRepository()
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, quantity = 3))

            repository.purchase(SupportAnimal.OWL)
            runCurrent()

            repository.supporterState.value.oneTimePurchaseCounts shouldBe mapOf(SupportAnimal.OWL to 3)
        }

    @Test
    fun `Given multi-quantity product bought again - When state read - Then quantities are summed`() =
        runTest {
            val repository = createRepository()
            billingService.purchaseResult =
                BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, token = "first", quantity = 2))
            repository.purchase(SupportAnimal.OWL)
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, token = "second"))

            repository.purchase(SupportAnimal.OWL)
            runCurrent()

            repository.supporterState.value.oneTimePurchaseCounts shouldBe mapOf(SupportAnimal.OWL to 3)
        }

    @Test
    fun `Given active subscription - When a downgrade is scheduled - Then the purchase is scheduled and not chosen`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.BEAR))
            val repository = createRepository()
            repository.chooseAnimal(SupportAnimal.BEAR)
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.BOAR))
            billingService.scheduledAfterPurchase = BillingScheduledSubscription(SupportAnimal.BOAR.productId, SWITCH_TIME)

            val result = repository.purchase(SupportAnimal.BOAR)
            runCurrent()

            result shouldBe BillingPurchaseResult.Scheduled(Instant.fromEpochMilliseconds(SWITCH_TIME))
            repository.supporterState.value shouldBe SupporterState(
                activeSubscriptions = setOf(SupportAnimal.BEAR),
                chosenAnimal = SupportAnimal.BEAR,
                scheduledSubscription = ScheduledSubscription(SupportAnimal.BOAR, Instant.fromEpochMilliseconds(SWITCH_TIME)),
                lastPurchaseTimes = mapOf(SupportAnimal.BEAR to PURCHASE_INSTANT),
            )
        }

    @Test
    fun `Given scheduled subscription - When it starts - Then it is active and no longer scheduled`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.BEAR))
            billingService.scheduledSubscription = BillingScheduledSubscription(SupportAnimal.BOAR.productId, SWITCH_TIME)
            val repository = createRepository()
            billingService.entitlements = listOf(purchase(SupportAnimal.BOAR))
            billingService.scheduledSubscription = null

            repository.refresh()
            runCurrent()

            repository.supporterState.value.activeSubscriptions shouldBe setOf(SupportAnimal.BOAR)
            repository.supporterState.value.scheduledSubscription shouldBe null
        }

    @Test
    fun `Given scheduled subscription that is already active - When refreshed - Then it is not scheduled`() =
        runTest {
            billingService.entitlements = listOf(purchase(SupportAnimal.BEAR))
            billingService.scheduledSubscription = BillingScheduledSubscription(SupportAnimal.BEAR.productId, SWITCH_TIME)

            val repository = createRepository()

            repository.supporterState.value.scheduledSubscription shouldBe null
        }

    @Test
    fun `Given one-time purchase - When recorded - Then it is consumed`() =
        runTest {
            val repository = createRepository()
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL, token = "owl"))

            repository.purchase(SupportAnimal.OWL)
            runCurrent()

            billingService.consumedPurchaseTokens shouldBe listOf("owl")
        }

    @Test
    fun `Given failing storage - When a one-time purchase is recorded - Then it is not consumed`() =
        runTest {
            val repository = createRepository()
            dataStore.writeError = IOException("No space left on device")
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.OWL))

            repository.purchase(SupportAnimal.OWL)
            runCurrent()

            billingService.consumedPurchaseTokens shouldBe emptyList()
        }

    @Test
    fun `Given subscription purchase - When recorded - Then it is not consumed`() =
        runTest {
            val repository = createRepository()
            billingService.purchaseResult = BillingPurchaseResult.Success(purchase(SupportAnimal.BEAR))

            repository.purchase(SupportAnimal.BEAR)
            runCurrent()

            billingService.consumedPurchaseTokens shouldBe emptyList()
        }

    private suspend fun FakeDataStore.storedOneTimeEntries(): Set<String>? = data.first()[stringSetPreferencesKey("support_purchased_one_time_products")]

    private fun purchase(animal: SupportAnimal, token: String = "token_${animal.name}", quantity: Int = 1) =
        BillingPurchase(
            productId = animal.productId,
            purchaseToken = token,
            purchaseTimeMillis = PURCHASE_TIME,
            quantity = quantity,
        )

    companion object {
        private const val PURCHASE_TIME = 1_700_000_000_000
        private const val SWITCH_TIME = 1_700_100_000_000
        private val PURCHASE_INSTANT = Instant.fromEpochMilliseconds(PURCHASE_TIME)
    }
}
