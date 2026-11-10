package hu.mostoha.mobile.kmp.huki.billing

import android.app.Application
import co.touchlab.kermit.Logger
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams.SubscriptionProductReplacementParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import hu.mostoha.mobile.kmp.huki.model.domain.BillingEntitlements
import hu.mostoha.mobile.kmp.huki.model.domain.BillingProduct
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchase
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.BillingSyncResult
import hu.mostoha.mobile.kmp.huki.model.domain.ProductPrice
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportType
import hu.mostoha.mobile.kmp.huki.service.BillingException
import hu.mostoha.mobile.kmp.huki.service.BillingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

class PlayBillingService(application: Application) : BillingService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectionMutex = Mutex()
    private val activityTracker = ResumedActivityTracker().also { application.registerActivityLifecycleCallbacks(it) }
    private val productDetailsById = mutableMapOf<String, ProductDetails>()
    private var purchaseFlow: PurchaseFlow? = null

    /**
     * Set before the first suspension, so a second purchase can't start while the flow is being prepared.
     */
    private var isPurchaseStarting = false
    private var purchaseListener: ((BillingPurchase) -> Unit)? = null

    private val billingClient = BillingClient.newBuilder(application)
        .setListener { billingResult, purchases ->
            scope.launch { onPurchasesUpdated(billingResult, purchases.orEmpty()) }
        }
        .enableAutoServiceReconnection()
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    override fun fetchProducts(
        productIds: List<String>,
        onSuccess: (List<BillingProduct>) -> Unit,
        onError: (String) -> Unit,
    ) {
        scope.launch {
            try {
                connect()
                onSuccess(queryProducts(productIds))
            } catch (exception: BillingException) {
                onError(exception.message.orEmpty())
            }
        }
    }

    override fun purchase(productId: String, onResult: (BillingPurchaseResult) -> Unit) {
        scope.launch {
            if (purchaseFlow != null || isPurchaseStarting) {
                onResult(BillingPurchaseResult.Failed("Play Billing: Another purchase is in progress"))
                return@launch
            }
            isPurchaseStarting = true
            try {
                connect()
                val productDetails = productDetailsById[productId]
                    ?: queryProducts(listOf(productId)).let { productDetailsById[productId] }
                    ?: throw BillingException("Play Billing: Unknown product $productId")
                val replacedSubscription = if (productDetails.subscriptionOfferDetails != null) {
                    queryReplacedSubscription(productId)
                } else {
                    null
                }
                val activity = activityTracker.resumedActivity ?: throw BillingException("Play Billing: No activity")
                purchaseFlow = PurchaseFlow(productId, onResult)
                val billingResult = billingClient.launchBillingFlow(
                    activity,
                    productDetails.toBillingFlowParams(replacedSubscription),
                )
                if (billingResult.responseCode != BillingResponseCode.OK) {
                    purchaseFlow = null
                    onResult(billingResult.toFailedResult())
                }
            } catch (exception: BillingException) {
                onResult(BillingPurchaseResult.Failed(exception.message.orEmpty()))
            } finally {
                isPurchaseStarting = false
            }
        }
    }

    /**
     * Play switches subscriptions immediately, so there is never a scheduled one.
     */
    override fun fetchEntitlements(onSuccess: (BillingEntitlements) -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                connect()
                onSuccess(BillingEntitlements(purchases = queryOwnedPurchases()))
            } catch (exception: BillingException) {
                onError(exception.message.orEmpty())
            }
        }
    }

    /**
     * Play has no separate sync, every entitlement fetch already queries its servers.
     */
    override fun syncPurchases(onResult: (BillingSyncResult) -> Unit) {
        onResult(BillingSyncResult.Synced)
    }

    override fun setPurchaseListener(listener: (BillingPurchase) -> Unit) {
        purchaseListener = listener
    }

    /**
     * A failed consume is retried with the next entitlement fetch, which reports the purchase again until then.
     */
    override fun consumePurchase(purchaseToken: String) {
        scope.launch {
            try {
                connect()
                val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
                billingClient.consumePurchase(params).billingResult.throwIfFailed()
            } catch (exception: BillingException) {
                Logger.w(exception) { "Billing: Failed to consume purchase." }
            }
        }
    }

    /**
     * Purchases of the running flow answer it, any other one (e.g. an approved pending purchase) goes to the listener.
     */
    private suspend fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>) {
        if (billingResult.responseCode == BillingResponseCode.OK) {
            onPurchasesUpdated(purchases)
            return
        }
        val flow = purchaseFlow ?: return
        purchaseFlow = null
        when (billingResult.responseCode) {
            BillingResponseCode.USER_CANCELED -> flow.onResult(BillingPurchaseResult.Cancelled)
            BillingResponseCode.ITEM_ALREADY_OWNED -> flow.onResult(ownedPurchaseResult(flow.productId))
            else -> flow.onResult(billingResult.toFailedResult())
        }
    }

    /**
     * An update without the flow's product, e.g. an approved pending purchase, leaves the flow open. An update
     * without any purchase answers the flow from the owned purchases.
     */
    private suspend fun onPurchasesUpdated(purchases: List<Purchase>) {
        if (purchases.isEmpty()) {
            val flow = purchaseFlow ?: return
            purchaseFlow = null
            flow.onResult(ownedPurchaseResult(flow.productId))
            return
        }
        val flow = purchaseFlow?.takeIf { flow -> purchases.any { flow.productId in it.products } }
        if (flow != null) {
            purchaseFlow = null
        }
        var flowResult: BillingPurchaseResult = BillingPurchaseResult.Pending
        purchases.forEach { purchase ->
            val billingPurchase = processPurchase(purchase)
            if (flow != null && flow.productId in purchase.products) {
                flowResult = billingPurchase?.let { BillingPurchaseResult.Success(it) } ?: BillingPurchaseResult.Pending
            } else if (billingPurchase != null) {
                purchaseListener?.invoke(billingPurchase)
            }
        }
        flow?.onResult(flowResult)
    }

    /**
     * A one-time product is owned while its purchase is pending or not yet consumed.
     */
    private suspend fun ownedPurchaseResult(productId: String): BillingPurchaseResult =
        try {
            val purchases = queryPurchases().filter { productId in it.products }
            val purchased = purchases.firstNotNullOfOrNull { processPurchase(it) }
            when {
                purchased != null -> BillingPurchaseResult.Success(purchased)
                purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> BillingPurchaseResult.Pending
                else -> BillingPurchaseResult.Failed("Play Billing: Owned product $productId not found")
            }
        } catch (exception: BillingException) {
            BillingPurchaseResult.Failed(exception.message.orEmpty())
        }

    private suspend fun queryOwnedPurchases(): List<BillingPurchase> =
        queryPurchases().mapNotNull { processPurchase(it) }

    private suspend fun queryPurchases(
        productTypes: List<String> = listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP),
    ): List<Purchase> =
        productTypes.flatMap { productType ->
            val params = QueryPurchasesParams.newBuilder().setProductType(productType).build()
            val result = billingClient.queryPurchasesAsync(params)
            result.billingResult.throwIfFailed()
            result.purchasesList
        }

    /**
     * A new subscription replaces the current one instead of running in parallel. Legacy users can own
     * several, the highest one is replaced.
     */
    private suspend fun queryReplacedSubscription(productId: String): Purchase? =
        queryPurchases(listOf(BillingClient.ProductType.SUBS))
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && productId !in it.products }
            .minByOrNull { purchase ->
                purchase.products.firstNotNullOfOrNull { SupportAnimal.fromProductId(it)?.supportLevel }
                    ?: Int.MAX_VALUE
            }

    /**
     * Unacknowledged purchases are refunded by Play after 3 days. One-time purchases are consumed only once
     * recorded, a consume acknowledges them. A failed acknowledge is retried on the next entitlement fetch.
     */
    private suspend fun processPurchase(purchase: Purchase): BillingPurchase? {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return null
        }
        val productId = purchase.products.firstOrNull() ?: return null
        val isSubscription = SupportAnimal.fromProductId(productId)?.type == SupportType.RECURRING
        if (isSubscription && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            val result = billingClient.acknowledgePurchase(params)
            if (result.responseCode != BillingResponseCode.OK) {
                Logger.w { "Billing: Failed to acknowledge purchase of $productId: ${result.responseCode}" }
            }
        }
        return BillingPurchase(
            productId = productId,
            purchaseToken = purchase.purchaseToken,
            purchaseTimeMillis = purchase.purchaseTime,
            quantity = purchase.quantity,
        )
    }

    /**
     * Subscriptions are bought through their base plan, the offer without an offer ID. A replaced subscription
     * ends immediately, its unused time is credited to the new one, in both directions.
     */
    private fun ProductDetails.toBillingFlowParams(replacedSubscription: Purchase?): BillingFlowParams {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(this)
        subscriptionOfferDetails?.firstOrNull { it.offerId == null }?.let {
            productDetailsParams.setOfferToken(it.offerToken)
        }
        val builder = BillingFlowParams.newBuilder()
        replacedSubscription?.let { purchase ->
            productDetailsParams.setSubscriptionProductReplacementParams(
                SubscriptionProductReplacementParams.newBuilder()
                    .setOldProductId(purchase.products.first())
                    .setReplacementMode(SubscriptionProductReplacementParams.ReplacementMode.WITH_TIME_PRORATION)
                    .build(),
            )
            builder.setSubscriptionUpdateParams(
                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(purchase.purchaseToken)
                    .build(),
            )
        }
        return builder
            .setProductDetailsParamsList(listOf(productDetailsParams.build()))
            .build()
    }

    private fun BillingResult.toFailedResult(): BillingPurchaseResult =
        BillingPurchaseResult.Failed("Play Billing error $responseCode: $debugMessage")

    /**
     * A product ID only resolves for its own product type, so both types are queried with every ID.
     * A failed query fails the whole fetch instead of leaving that type's products without a price.
     */
    private suspend fun queryProducts(productIds: List<String>): List<BillingProduct> =
        coroutineScope {
            listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS)
                .map { productType -> async { queryProductDetails(productIds, productType) } }
                .awaitAll()
                .flatMap { result ->
                    result.billingResult.throwIfFailed()
                    result.productDetailsList.orEmpty()
                }
                .onEach { productDetailsById[it.productId] = it }
                .mapNotNull { it.toBillingProduct() }
        }

    /**
     * Play answers with a transient SERVICE_UNAVAILABLE now and then, e.g. right after an install.
     */
    private suspend fun queryProductDetails(productIds: List<String>, productType: String): ProductDetailsResult {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map { productId ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                },
            )
            .build()
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingResponseCode.SERVICE_UNAVAILABLE) {
            return result
        }
        Logger.w { "Billing: Product query unavailable, retrying." }
        delay(QUERY_RETRY_DELAY)
        return billingClient.queryProductDetails(params)
    }

    /**
     * With auto reconnection the library reconnects on its own once connected, and a second
     * startConnection while any connection is in flight fails with DEVELOPER_ERROR.
     */
    private suspend fun connect() =
        connectionMutex.withLock {
            if (billingClient.connectionState != ConnectionState.DISCONNECTED) {
                return@withLock
            }
            val result = suspendCancellableCoroutine { continuation ->
                billingClient.startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            if (continuation.isActive) {
                                continuation.resume(billingResult)
                            }
                        }

                        override fun onBillingServiceDisconnected() {
                            Logger.w { "Billing: Service disconnected." }
                        }
                    },
                )
            }
            result.throwIfFailed()
        }

    private fun BillingResult.throwIfFailed() {
        if (responseCode != BillingResponseCode.OK) {
            throw BillingException("Play Billing error $responseCode: $debugMessage")
        }
    }

    /**
     * Picks the base offer: subscription offers and discounted one-time offers have an offer ID.
     */
    private fun ProductDetails.toBillingProduct(): BillingProduct? {
        val oneTimeOffer = oneTimePurchaseOfferDetailsList?.firstOrNull { it.offerId == null }
        val subscriptionPhase = subscriptionOfferDetails
            ?.firstOrNull { it.offerId == null }
            ?.pricingPhases
            ?.pricingPhaseList
            ?.lastOrNull()
        val (priceAmountMicros, currencyCode) = when {
            oneTimeOffer != null -> oneTimeOffer.priceAmountMicros to oneTimeOffer.priceCurrencyCode
            subscriptionPhase != null -> subscriptionPhase.priceAmountMicros to subscriptionPhase.priceCurrencyCode
            else -> return null
        }
        return BillingProduct(productId = productId, price = ProductPrice(priceAmountMicros, currencyCode))
    }

    private class PurchaseFlow(
        val productId: String,
        val onResult: (BillingPurchaseResult) -> Unit,
    )

    companion object {
        private val QUERY_RETRY_DELAY = 1.seconds
    }
}
