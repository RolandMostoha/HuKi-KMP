# iOS Billing — Support tiers

Setup guide for the Support screen's in-app purchases on iOS (StoreKit 2).

| Tier                      | Type                                     | Notes                             |
|---------------------------|------------------------------------------|-----------------------------------|
| Squirrel / Owl / Hedgehog | **Consumable**                           | Repeatable tips, nothing unlocked |
| Deer / Boar / Bear        | **Auto-Renewable Subscription**, 1 month | One subscription group            |

### Product IDs
Shared with Google Play (legacy IDs from the old Android app, kept so existing supporters keep their badge). Lower level number = higher price (`level_0` added for the new top tiers). Permanent — never change or reuse; animal names are display names only.

| Animal   | Product ID                       | Type                                      | Price     |
|----------|----------------------------------|-------------------------------------------|-----------|
| Squirrel | `huki_support_one_time_level_2`  | Consumable                                | 499 HUF   |
| Owl      | `huki_support_one_time_level_1`  | Consumable                                | 999 HUF   |
| Hedgehog | `huki_support_one_time_level_0`  | Consumable                                | 1 490 HUF |
| Deer     | `huki_support_recurring_level_2` | Subscription, 1 month, Apple group rank 3 | 199 HUF   |
| Boar     | `huki_support_recurring_level_1` | Subscription, 1 month, Apple group rank 2 | 499 HUF   |
| Bear     | `huki_support_recurring_level_0` | Subscription, 1 month, Apple group rank 1 | 999 HUF   |

Subscription group: `HuKi Supporter` (HU: `HuKi támogató`). Family Sharing off.

## Part 1 — App Store Connect

### 1. Business prerequisites (slow, start first)
- [x] **Paid Apps Agreement** — *Business → Agreements* must be **Active**, otherwise products never load.
- [x] **Legal entity** — *Business → Edit Legal Entity* must be completed before the Paid Apps Agreement can be signed.
- [x] **Bank account + tax forms** — *Business*. US **W-8BEN**: Part I with the Hungarian tax ID (adóazonosító jel) in 6a, no US TIN; **Part II empty** — the US–HU tax treaty is terminated (from 2024), so Apple withholds 30% on US-storefront sales only. Signer title: `Owner`.
- [ ] **App Store Small Business Program** — developer.apple.com/app-store/small-business-program → commission 30% → **15%**. Submitted 2026-10-29, awaiting approval.
- [x] **EU DSA trader status** + **DAC7** (no personal services) — Active.

### 2. One-time products (Consumables)
*App → Monetization → In-App Purchases → +*
- [x] **Type**: Consumable
- [x] **Reference Name**: internal only (ASC lists + reports), e.g. `Squirrel`
- [x] **Product ID**: permanent, never reusable, e.g. `huki_support_one_time_level_2` (use the same IDs on Google Play → one shared mapping)
- [x] **Price**: base country **Hungary**, pick the HUF price point (499 / 999 / 1 490 — 1 499 has no Apple price point; prices include VAT, Apple is the merchant)
- [x] **Localizations (EN + HU)**: Display Name + Description ("A one-time tip to support HuKi")
- [x] **Review Information**: `tools/assets/support/huki_support_review_screenshot.png` + note (cosmetic supporter badge, Menu → Support)
- [x] **Availability**: all countries

Consumables aren't restored by default → add `SKIncludeConsumableInAppPurchaseHistory = YES` to Info.plist (iOS 18+) so `Transaction.all` returns past tips and supporter badges survive reinstall / new device.

### 3. Recurring products (Subscriptions)
*Monetization → Subscriptions*
- [x] Create **one Subscription Group** `HuKi Supporter`, localize the group name (EN/HU) → max one active tier, upgrade/downgrade between them
- [x] Add Deer, Boar, Bear: Product ID (`huki_support_recurring_level_2`), **1 month**, price (199 / 499 / 999 HUF — 249 has no Apple price point), EN/HU localization
- [x] **Review Information**: same screenshot + note (monthly, funds continuous development, badge while subscribed)
- [x] **Rank levels** (Bear highest): upgrade = immediate + prorated, downgrade = next renewal
- [x] **US prices** (manual US prices are allowed, each storefront is priced on its own): auto-conversion from HUF showed Deer and Boar at $0.99 each → set US manually via *Subscription Prices → View all Subscription Pricing*: Deer $0.99, Boar **$1.99**, Bear **$2.99** (check other storefronts for the same collision)
- [x] No introductory / promotional offers
- [x] Billing Grace Period: 16 days, all renewals, production + sandbox (StoreKit reports grace-period subs as active)
- [x] Google Play: same six IDs created, icons from `tools/assets/support/`; new purchase options use ID `base` (`legacy-base` is reserved)

### 4. App Review requirements
- [x] **3.1.1**: tips via IAP, wording makes it clear **nothing is unlocked**
- [x] **3.1.2**: purchase UI shows **price, period, auto-renewal**, links **Terms of Use (EULA)** + **Privacy Policy** on the Support screen
- [x] EULA in *App Information → License Agreement* (or Apple standard EULA link in the description)
- [x] Clear review note for the "supporter" subscriptions (unlocking nothing is a review risk)
- [x] **Restore Purchases** button (iOS only: `AppStore.sync()` + entitlement fetch, result alert)
- [ ] First IAPs ship **with a new app version**: products "Ready to Submit", attached in the version page's *In-App Purchases and Subscriptions* section

### 5. Sandbox testers
- [ ] *Users and Access → Sandbox → Test Accounts*
- [ ] On device: *Settings → Developer → Sandbox Apple Account*

## Part 2 — Code

### 6. Xcode project
- [ ] *Signing & Capabilities* → **In-App Purchase**
- [x] Info.plist: `SKIncludeConsumableInAppPurchaseHistory = YES` (finished consumables stay in `Transaction.all`, so one-time support comes back after a reinstall)
- [x] **StoreKit Configuration File** `iosApp/HuKi.storekit`, synced from App Store Connect (storefront United States: the sync only copies the USD base prices, which a Hungarian storefront would show as "1 HUF"; locale `hu`, accelerated renewals), not in the app target, selected in the shared scheme (*Scheme → Run → Options → StoreKit Configuration*)
  - Only applies when run from Xcode; `ios_run_on_device.sh` (`devicectl`) still uses Sandbox
  - Product changes in App Store Connect: *Sync* button in the file editor

### 7. Architecture (KMP)
StoreKit 2 is Swift-only, so Kotlin/Native can't call it directly.
- **commonMain**
  - [x] `SupportAnimal.productId` — one ID mapping for both stores
  - [x] `BillingService` interface: callback-based `fetchProducts(productIds, onSuccess, onError)` (implementable from Swift and Play Billing), wrapped by a suspend `fetchProducts` extension, plus `purchase` (→ `BillingPurchaseResult` Success / Pending / Cancelled / Failed), `fetchEntitlements` and `setPurchaseListener`
  - [x] `SupportViewModel` + `SupportUiState` (tiers with price and `isActive`, `SupportPricesState`, `purchasingAnimal`, inline `purchaseMessage` PENDING / FAILED) + purchase analytics
  - [x] `SupporterRepository` (Koin single, created at start): supporter state from DataStore. One-time purchases use the legacy HuKi-Android keys and `productId;count;lastPurchaseTimeMillis` format (idempotent per purchase token), active subscriptions are cached for offline
- **iOS**: [x] Swift `IosBillingService` (StoreKit 2) implementing the Kotlin interface, handed to `initKoin` — products, purchase (`finish()` before reporting), `Transaction.unfinished` + `Transaction.updates` listener from init, entitlements (`currentEntitlements` + consumables from `Transaction.all`)
- **Android**: [x] `PlayBillingService` (Play Billing 9, `formattedPrice` of the base offer) behind the same interface — products, purchase from the resumed Activity, consume (one-time) / acknowledge (subscriptions), leftover unconsumed purchases finished on entitlement fetch. Subscriptions are not exclusive on Play, an active tier shows a tick and is not tappable. Real prices need a Google account (license tester) on the device and the non-`.debug` application ID

### 8. StoreKit 2 flow (Swift)
```swift
// 1. Load products and replace the hardcoded `prices` map with product.displayPrice
let products = try await Product.products(for: SupportAnimal.allProductIds)

// 2. Purchase
switch try await product.purchase() {
case .success(let verification):
    guard case .verified(let transaction) = verification else { return } // jailbreak / tampered
    // grant: update owned animals
    await transaction.finish()            // mandatory, or it's redelivered forever
case .pending:       break                // Ask to Buy / SCA bank confirmation
case .userCancelled: break
@unknown default:    break
}

// 3. Listener, started at app launch in HukiApp: renewals, purchases made on other devices, pending approvals
Task.detached {
    for await update in Transaction.updates {
        if case .verified(let t) = update { /* refresh state */ await t.finish() }
    }
}

// 4. Supporter state
for await result in Transaction.currentEntitlements { … }  // active subscriptions
for await result in Transaction.all { … }                  // consumable history (with the plist key)

// 5. Restore button
try await AppStore.sync()
```
- [x] Prices: always `product.displayPrice` (storefront currency), never hardcoded HUF; "/ month" from `product.subscription?.subscriptionPeriod`
- [ ] Manage subscription: `.manageSubscriptionsSheet(isPresented:)` in the supporter state
- [ ] Current tier (highlight, upgrade/downgrade): `product.subscription?.status` → `renewalInfo`
- [ ] iOS 18: SwiftUI `@Environment(\.purchase)` instead of `product.purchase()` (correct window scene for the sheet)

### 9. Chores
- [ ] **Offline**: `Product.products` fails without network → disabled tiles + "Prices unavailable offline" note, retry when back online. `currentEntitlements` is cached by StoreKit and works offline → supporter badge still shows
- [ ] **Analytics**: `Analytics.logTransaction(transaction)` after verified purchase (Firebase doesn't auto-log StoreKit 2) + events like `support_tier_selected_squirrel`, `support_purchase_cancelled`
- [ ] **Crashlytics**: non-fatals for product load failures and unverified transactions
- [x] **Privacy**: App Privacy label (Purchases → Purchase History, if sent to Firebase) + Privacy Policy
- [ ] **Maestro**: purchase sheet is system UI → E2E stops at tapping the tile; with the StoreKit config file the simulator shows real product data
- [ ] **Docs**: AGENTS.md (billing section) + README

## Part 3 — Testing

| | Where it runs | Needs App Store Connect setup? | Real Apple servers? |
|---|---|---|---|
| **StoreKit Configuration file** | Simulator or device, run from Xcode | **No** | No, fully local |
| **Sandbox** | **Debug build from Xcode** on a physical device (simulator also works on recent iOS/Xcode) | **Yes** | Yes, sandbox |
| **TestFlight** | TestFlight build | Yes | Yes, sandbox (testers never charged) |

**StoreKit Configuration file**: purchases, cancellations, Ask to Buy / pending, failed transactions, refunds, accelerated renewals; reset via *Debug → StoreKit → Manage Transactions*.

Test cases (turn one on in the `HuKi.storekit` Configuration Settings, then back off):

| Setting | Expected in HuKi |
|---|---|
| Ask to Buy | Pending message; approve in *Manage Transactions* → badge + celebration via `Transaction.updates` |
| Interrupted Purchases | Failed message; resolve in *Manage Transactions* → badge via the listener |
| Grace Period + Billing Retry | Tier stays "Active" during grace |
| Billing Retry only | Tier loses "Active" once the subscription lapses |
| Failures → Load Products | Prices-unavailable info + retry |
| Failures → Purchase / Verification | Failed message |
| Failures → Manage Subscriptions Sheet | Sheet error doesn't break the screen |

**Sandbox from Xcode** requires:
- Paid Apps Agreement **Active** (otherwise `Product.products(for:)` returns empty)
- Products in at least **"Ready to Submit"** with price + localization (no review needed)
- Matching bundle ID `hu.mostoha.mobile.ios.huki`
- **Not run through the Xcode scheme** (its StoreKit config overrides sandbox): use `ios_run_on_device.sh`, or set the scheme's StoreKit Configuration to None
- Sandbox tester signed in under *Settings → Developer → Sandbox Apple Account*
- 1-month subscriptions renew every ~5 minutes, up to 12 times

**TestFlight**: final check of a near-release build or testing by others; adds nothing new for billing logic.

## Order of work
1. Paid Apps Agreement, tax, banking, Small Business Program
2. Create products in App Store Connect
3. Sync `HuKi.storekit`, develop against it
4. Shared `BillingService` + `SupportViewModel`, then the Swift implementation
5. Sandbox on device (Xcode Run), then TestFlight
6. Submit the new version with the IAPs attached
