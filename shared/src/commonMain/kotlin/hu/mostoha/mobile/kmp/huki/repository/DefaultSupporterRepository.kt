package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.datastore.SupportPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.analytics.SupporterLevel
import hu.mostoha.mobile.kmp.huki.model.analytics.UserProperty
import hu.mostoha.mobile.kmp.huki.model.domain.BillingEntitlements
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchase
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.BillingSyncResult
import hu.mostoha.mobile.kmp.huki.model.domain.OneTimePurchaseRecord
import hu.mostoha.mobile.kmp.huki.model.domain.ScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportRestoreResult
import hu.mostoha.mobile.kmp.huki.model.domain.SupportType
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import hu.mostoha.mobile.kmp.huki.model.mapper.toDataStoreEntries
import hu.mostoha.mobile.kmp.huki.model.mapper.toOneTimePurchaseRecords
import hu.mostoha.mobile.kmp.huki.model.mapper.toScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.mapper.toSupporterLevel
import hu.mostoha.mobile.kmp.huki.model.mapper.toSupporterState
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.BillingException
import hu.mostoha.mobile.kmp.huki.service.BillingService
import hu.mostoha.mobile.kmp.huki.service.fetchEntitlements
import hu.mostoha.mobile.kmp.huki.service.purchase
import hu.mostoha.mobile.kmp.huki.service.syncPurchases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.IOException
import kotlin.time.Instant

class DefaultSupporterRepository(
    private val billingService: BillingService,
    private val dataStore: DataStore<Preferences>,
    private val analyticsService: AnalyticsService,
    private val scope: CoroutineScope,
) : SupporterRepository {
    /**
     * Not persisted: the stores cache their entitlements on the device, so they stay the single source.
     */
    private val activeSubscriptions = MutableStateFlow<Map<SupportAnimal, Instant>>(emptyMap())
    private val scheduledSubscription = MutableStateFlow<ScheduledSubscription?>(null)

    /**
     * Subscriptions are known only after a fetch, until then a recurring supporter looks like a one-time one.
     */
    private var isEntitlementsFetched = false
    private var reportedSupporterLevel: SupporterLevel? = null

    /**
     * Fetches run one at a time, so an older result can't overwrite a newer one.
     */
    private val entitlementsMutex = Mutex()

    override val supporterState: StateFlow<SupporterState> = combine(
        dataStore.data
            .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
            .map { preferences -> preferences.toSupporterState() },
        activeSubscriptions,
        scheduledSubscription,
    ) { storedState, activeSubscriptions, scheduledSubscription ->
        storedState.copy(
            activeSubscriptions = activeSubscriptions.keys,
            scheduledSubscription = scheduledSubscription,
            lastPurchaseTimes = storedState.lastPurchaseTimes + activeSubscriptions,
        )
    }.stateIn(scope, SharingStarted.Eagerly, SupporterState())

    init {
        billingService.setPurchaseListener { purchase -> scope.launch { recordPurchase(purchase) } }
    }

    override suspend fun refresh() {
        try {
            entitlementsMutex.withLock { storeEntitlements(billingService.fetchEntitlements()) }
        } catch (exception: BillingException) {
            Logger.w(exception) { "Billing: Failed to refresh entitlements." }
        }
    }

    /**
     * Runs in the repository scope: once the store has charged and consumed the purchase it must be
     * recorded, even if the caller is cancelled, e.g. the screen is closed during the purchase.
     */
    override suspend fun purchase(animal: SupportAnimal): BillingPurchaseResult =
        scope.async {
            val result = billingService.purchase(animal.productId)
            if (result !is BillingPurchaseResult.Success) {
                return@async result
            }
            recordPurchase(result.purchase)
            val scheduled = scheduledSubscription.value?.takeIf {
                it.animal == animal &&
                    animal !in activeSubscriptions.value.keys
            }
            if (scheduled != null) {
                BillingPurchaseResult.Scheduled(scheduled.startTime)
            } else {
                chooseAnimal(animal)
                result
            }
        }.await()

    override suspend fun restore(): SupportRestoreResult =
        when (val syncResult = billingService.syncPurchases()) {
            BillingSyncResult.Cancelled -> SupportRestoreResult.Cancelled
            is BillingSyncResult.Failed -> SupportRestoreResult.Failed(syncResult.message)
            BillingSyncResult.Synced -> try {
                val entitlements = entitlementsMutex.withLock {
                    billingService.fetchEntitlements().also { storeEntitlements(it) }
                }
                if (entitlements.purchases.any { it.type != null }) {
                    SupportRestoreResult.Restored
                } else {
                    SupportRestoreResult.NothingToRestore
                }
            } catch (exception: BillingException) {
                SupportRestoreResult.Failed(exception.message.orEmpty())
            }
        }

    override suspend fun chooseAnimal(animal: SupportAnimal) {
        try {
            dataStore.edit { preferences -> preferences[SupportPreferenceKeys.CHOSEN_ANIMAL] = animal.productId }
        } catch (exception: IOException) {
            Logger.e(exception) { "Support: Failed to store the chosen animal." }
        }
    }

    private suspend fun recordPurchase(purchase: BillingPurchase) {
        when (purchase.type) {
            SupportType.ONE_TIME -> recordOneTimePurchases(listOf(purchase))
            SupportType.RECURRING -> recordSubscription(purchase)
            null -> Logger.w { "Billing: Unknown product purchased: ${purchase.productId}" }
        }
        reportSupporterLevel()
    }

    /**
     * Re-fetched instead of added, because a new subscription can end another one, e.g. an upgrade
     * within an App Store subscription group.
     */
    private suspend fun recordSubscription(purchase: BillingPurchase) {
        entitlementsMutex.withLock {
            try {
                storeEntitlements(billingService.fetchEntitlements())
            } catch (exception: BillingException) {
                Logger.w(exception) { "Billing: Failed to fetch entitlements after a subscription purchase." }
                SupportAnimal.fromProductId(purchase.productId)?.let { animal ->
                    activeSubscriptions.update {
                        it +
                            (animal to Instant.fromEpochMilliseconds(purchase.purchaseTimeMillis))
                    }
                }
            }
        }
    }

    private suspend fun storeEntitlements(entitlements: BillingEntitlements) {
        val (subscriptions, oneTimePurchases) = entitlements.purchases.partition { it.type == SupportType.RECURRING }
        recordOneTimePurchases(oneTimePurchases)
        activeSubscriptions.value = subscriptions
            .mapNotNull { purchase ->
                SupportAnimal.fromProductId(purchase.productId)?.let {
                    it to
                        Instant.fromEpochMilliseconds(purchase.purchaseTimeMillis)
                }
            }
            .toMap()
        scheduledSubscription.value = entitlements.scheduledSubscription
            ?.toScheduledSubscription()
            ?.takeUnless { it.animal in activeSubscriptions.value.keys }
        isEntitlementsFetched = true
        reportSupporterLevel()
    }

    /**
     * Read from the stored data, as the state flow can still lag behind a write that just completed.
     */
    private suspend fun reportSupporterLevel() {
        if (!isEntitlementsFetched) return
        val preferences = try {
            dataStore.data.first()
        } catch (exception: IOException) {
            Logger.w(exception) { "Support: Failed to read the supporter level." }
            return
        }
        val level = preferences.toSupporterState()
            .copy(activeSubscriptions = activeSubscriptions.value.keys)
            .toSupporterLevel()
        if (level == reportedSupporterLevel) return
        reportedSupporterLevel = level
        analyticsService.setUserProperty(UserProperty.Supporter(level))
    }

    /**
     * Idempotent per purchase token: the same purchase can arrive more than once, e.g. from the
     * purchase flow and a later refresh, but must count only once. Consumed only once stored, until
     * then the store keeps reporting it, so a failed write is retried with the next refresh.
     */
    private suspend fun recordOneTimePurchases(purchases: List<BillingPurchase>) {
        if (purchases.isEmpty()) return
        try {
            storeOneTimePurchases(purchases)
        } catch (exception: IOException) {
            Logger.e(exception) { "Support: Failed to store one-time purchases." }
            return
        }
        purchases.forEach { billingService.consumePurchase(it.purchaseToken) }
    }

    /**
     * Skips recorded tokens before editing, as iOS reports the whole consumable history on every refresh.
     */
    private suspend fun storeOneTimePurchases(purchases: List<BillingPurchase>) {
        val recordedTokens = dataStore.data.first()[SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS].orEmpty()
        if (purchases.all { it.purchaseToken in recordedTokens }) return
        dataStore.edit { preferences ->
            val tokens = preferences[SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS].orEmpty().toMutableSet()
            val records = preferences[SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS]
                .orEmpty()
                .toOneTimePurchaseRecords()
                .associateBy { it.productId }
                .toMutableMap()
            purchases.filter { tokens.add(it.purchaseToken) }.forEach { purchase ->
                val previous = records[purchase.productId]
                records[purchase.productId] = OneTimePurchaseRecord(
                    productId = purchase.productId,
                    count = (previous?.count ?: 0) + purchase.quantity.coerceAtLeast(1),
                    lastPurchaseTimeMillis = maxOf(
                        previous?.lastPurchaseTimeMillis ?: 0,
                        purchase.purchaseTimeMillis,
                    ),
                )
            }
            preferences[SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS] = tokens
            preferences[SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS] =
                records.values.toList().toDataStoreEntries()
        }
    }

    private val BillingPurchase.type: SupportType?
        get() = SupportAnimal.fromProductId(productId)?.type
}
