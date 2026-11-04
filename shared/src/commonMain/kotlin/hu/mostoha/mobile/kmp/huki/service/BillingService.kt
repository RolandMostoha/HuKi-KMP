package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.domain.BillingEntitlements
import hu.mostoha.mobile.kmp.huki.model.domain.BillingProduct
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchase
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface BillingService {
    /**
     * Products unknown to the store are left out of the result, so it can be smaller than [productIds].
     */
    fun fetchProducts(productIds: List<String>, onSuccess: (List<BillingProduct>) -> Unit, onError: (String) -> Unit)

    /**
     * Opens the store's purchase dialog. A subscription is acknowledged before [onResult] is called.
     */
    fun purchase(productId: String, onResult: (BillingPurchaseResult) -> Unit)

    fun fetchEntitlements(onSuccess: (BillingEntitlements) -> Unit, onError: (String) -> Unit)

    /**
     * Receives purchases completed outside [purchase], e.g. approved pending purchases.
     */
    fun setPurchaseListener(listener: (BillingPurchase) -> Unit)

    /**
     * Called once a one-time purchase is recorded, so it can be bought again.
     */
    fun consumePurchase(purchaseToken: String)
}

class BillingException(message: String) : Exception(message)

suspend fun BillingService.fetchProducts(productIds: List<String>): List<BillingProduct> =
    suspendCancellableCoroutine { continuation ->
        fetchProducts(
            productIds = productIds,
            onSuccess = { products -> continuation.resume(products) },
            onError = { message -> continuation.resumeWithException(BillingException(message)) },
        )
    }

suspend fun BillingService.purchase(productId: String): BillingPurchaseResult =
    suspendCancellableCoroutine { continuation ->
        purchase(productId = productId, onResult = { result -> continuation.resume(result) })
    }

suspend fun BillingService.fetchEntitlements(): BillingEntitlements =
    suspendCancellableCoroutine { continuation ->
        fetchEntitlements(
            onSuccess = { purchases -> continuation.resume(purchases) },
            onError = { message -> continuation.resumeWithException(BillingException(message)) },
        )
    }
