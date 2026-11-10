package hu.mostoha.mobile.kmp.huki.model.domain

data class BillingProduct(
    val productId: String,
    val price: ProductPrice,
)

/**
 * Formatted by the UI, so it follows the current app language.
 */
data class ProductPrice(
    val amountMicros: Long,
    val currencyCode: String,
)
