# Billing mapping

The iOS project declares these StoreKit product IDs and reference prices:

| iOS product | Android product | Type | iOS reference price | Android display |
| --- | --- | --- | --- | --- |
| `com.alteregoai.pro.monthly` | `alteregoai_pro_monthly` | Auto-renewing subscription | £9.99/month | Google Play localized price |
| `com.alteregoai.pro.yearly` | `alteregoai_pro_yearly` | Auto-renewing subscription | £79.99/year | Google Play localized price |
| `com.alteregoai.elite.monthly` | `alteregoai_elite_monthly` | Auto-renewing subscription | £19.99/month | Google Play localized price |

Android code is in `android/app/src/main/java/com/alteregoai/app/core/BillingRepository.kt`. It queries subscription `ProductDetails`, selects an eligible offer token, launches the Google Play purchase flow, restores subscriptions, handles pending/cancelled states, acknowledges purchased subscriptions, and exposes observable ownership to the paywall.

Before production entitlement use, add server-side purchase-token verification and map verified entitlements to the user's account. The current iOS product is local-only, so there is no existing account identity or server endpoint available for this verification yet.
