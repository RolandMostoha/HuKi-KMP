import Shared
import StoreKit

class IosBillingService: BillingService {
    private var productsById: [String: Product] = [:]
    private var purchaseListener: ((BillingPurchase) -> Void)?
    private var updatesTask: Task<Void, Never>?

    deinit {
        updatesTask?.cancel()
    }

    func fetchProducts(
        productIds: [String],
        onSuccess: @escaping ([BillingProduct]) -> Void,
        onError: @escaping (String) -> Void
    ) {
        Task { @MainActor in
            do {
                let products = try await Product.products(for: productIds)
                products.forEach { productsById[$0.id] = $0 }
                onSuccess(products.map { product in
                    BillingProduct(
                        productId: product.id,
                        price: ProductPrice(
                            amountMicros: Int64(truncating: (product.price * 1_000_000) as NSDecimalNumber),
                            currencyCode: product.priceFormatStyle.currencyCode
                        )
                    )
                })
            } catch {
                onError(error.localizedDescription)
            }
        }
    }

    func purchase(productId: String, onResult: @escaping (BillingPurchaseResult) -> Void) {
        Task { @MainActor in
            do {
                guard let product = try await product(for: productId) else {
                    onResult(BillingPurchaseResultFailed(message: "StoreKit: Unknown product \(productId)"))
                    return
                }
                switch try await product.purchase() {
                case .success(let verification):
                    guard case .verified(let transaction) = verification else {
                        onResult(BillingPurchaseResultFailed(message: "StoreKit: Unverified transaction"))
                        return
                    }
                    await transaction.finish()
                    onResult(BillingPurchaseResultSuccess(purchase: transaction.toBillingPurchase()))
                case .pending:
                    onResult(BillingPurchaseResultPending.shared)
                case .userCancelled:
                    onResult(BillingPurchaseResultCancelled.shared)
                @unknown default:
                    onResult(BillingPurchaseResultFailed(message: "StoreKit: Unknown purchase result"))
                }
            } catch {
                onResult(BillingPurchaseResultFailed(message: error.localizedDescription))
            }
        }
    }

    /// Finished consumables are only in the history with `SKIncludeConsumableInAppPurchaseHistory` in Info.plist.
    /// Subscriptions are also derived from the history, as `currentEntitlements` can miss an active one.
    func fetchEntitlements(
        onSuccess: @escaping (BillingEntitlements) -> Void,
        onError: @escaping (String) -> Void
    ) {
        Task { @MainActor in
            var consumables: [Transaction] = []
            var latestSubscriptions: [String: Transaction] = [:]
            var subscriptionHistory: [String: [Transaction]] = [:]
            for await verification in Transaction.all {
                guard case .verified(let transaction) = verification, transaction.revocationDate == nil else {
                    continue
                }
                switch transaction.productType {
                case .consumable:
                    consumables.append(transaction)
                case .autoRenewable:
                    subscriptionHistory[transaction.productID, default: []].append(transaction)
                    let latestDate = latestSubscriptions[transaction.productID]?.purchaseDate ?? .distantPast
                    if transaction.purchaseDate > latestDate {
                        latestSubscriptions[transaction.productID] = transaction
                    }
                default:
                    break
                }
            }
            for await verification in Transaction.currentEntitlements {
                if case .verified(let transaction) = verification, transaction.productType == .autoRenewable {
                    latestSubscriptions[transaction.productID] = transaction
                }
            }
            let activeSubscriptions = latestSubscriptions.values.filter { $0.isActiveSubscription }
            // Right after an upgrade the replaced subscription can still look active, renewing into the new one
            let activeProductIds = Set(activeSubscriptions.map(\.productID))
            var scheduledSubscription: BillingScheduledSubscription?
            for subscription in activeSubscriptions where scheduledSubscription == nil {
                scheduledSubscription = await subscription.scheduledSubscription()
                    .flatMap { activeProductIds.contains($0.productId) ? nil : $0 }
            }
            onSuccess(
                BillingEntitlements(
                    purchases: activeSubscriptions.map { subscription in
                        subscription.toBillingPurchase(
                            purchaseDate: subscription.startDate(in: subscriptionHistory[subscription.productID] ?? [])
                        )
                    } + consumables.map { $0.toBillingPurchase() },
                    scheduledSubscription: scheduledSubscription
                )
            )
        }
    }

    /// Starts listening only now, so a transaction is never finished before it can be recorded. The listener
    /// is set at app launch, as Apple requires, e.g. for Ask to Buy approvals and interrupted purchases.
    func setPurchaseListener(listener: @escaping (BillingPurchase) -> Void) {
        purchaseListener = listener
        guard updatesTask == nil else { return }
        updatesTask = Task { @MainActor [weak self] in
            for await verification in Transaction.unfinished {
                await self?.handleOutsideFlow(verification)
            }
            for await verification in Transaction.updates {
                await self?.handleOutsideFlow(verification)
            }
        }
    }

    /// Transactions are finished in the purchase flow, the history keeps the consumables.
    func consumePurchase(purchaseToken: String) {}

    func syncPurchases(onResult: @escaping (BillingSyncResult) -> Void) {
        Task { @MainActor in
            do {
                try await AppStore.sync()
                onResult(BillingSyncResultSynced.shared)
            } catch StoreKitError.userCancelled {
                onResult(BillingSyncResultCancelled.shared)
            } catch {
                onResult(BillingSyncResultFailed(message: error.localizedDescription))
            }
        }
    }

    private func product(for productId: String) async throws -> Product? {
        if let product = productsById[productId] {
            return product
        }
        let product = try await Product.products(for: [productId]).first
        productsById[productId] = product
        return product
    }

    @MainActor
    private func handleOutsideFlow(_ verification: VerificationResult<Transaction>) async {
        guard case .verified(let transaction) = verification else {
            return
        }
        await transaction.finish()
        if transaction.revocationDate == nil {
            purchaseListener?(transaction.toBillingPurchase())
        }
    }
}

/// Renewals follow the expiration closely, a larger gap is a new subscription.
private let renewalGapTolerance: TimeInterval = 60

private extension Transaction {
    var isActiveSubscription: Bool {
        revocationDate == nil && !isUpgraded && (expirationDate ?? .distantFuture) > Date()
    }

    /// The purchase of the first renewal in the unbroken chain leading to this transaction.
    func startDate(in history: [Transaction]) -> Date {
        var start = purchaseDate
        for older in history.sorted(by: { $0.purchaseDate > $1.purchaseDate }) where older.purchaseDate < start {
            guard let expiration = older.expirationDate,
                  expiration >= start.addingTimeInterval(-renewalGapTolerance) else {
                break
            }
            start = older.purchaseDate
        }
        return start
    }

    /// A downgrade within the subscription group only renews into the new product, it isn't a transaction yet.
    func scheduledSubscription() async -> BillingScheduledSubscription? {
        guard case .verified(let renewalInfo)? = await subscriptionStatus?.renewalInfo,
              renewalInfo.willAutoRenew,
              let nextProductId = renewalInfo.autoRenewPreference,
              nextProductId != productID,
              let startDate = renewalInfo.renewalDate ?? expirationDate else {
            return nil
        }
        return BillingScheduledSubscription(
            productId: nextProductId,
            startTimeMillis: Int64(startDate.timeIntervalSince1970 * 1000)
        )
    }

    func toBillingPurchase(purchaseDate: Date? = nil) -> BillingPurchase {
        BillingPurchase(
            productId: productID,
            purchaseToken: String(id),
            purchaseTimeMillis: Int64((purchaseDate ?? self.purchaseDate).timeIntervalSince1970 * 1000),
            quantity: Int32(purchasedQuantity)
        )
    }
}
