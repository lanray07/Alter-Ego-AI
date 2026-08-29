package com.alteregoai.app.core

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.alteregoai.app.data.SubscriptionPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingState(
    val prices: Map<String, String> = emptyMap(),
    val ownedProductIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val activePlan: SubscriptionPlan get() = when {
        ownedProductIds.contains(AppConstants.eliteMonthlyProductId) -> SubscriptionPlan.ELITE
        ownedProductIds.any { it == AppConstants.proMonthlyProductId || it == AppConstants.proYearlyProductId } -> SubscriptionPlan.PRO
        else -> SubscriptionPlan.FREE
    }
}

class BillingRepository(context: Context) : PurchasesUpdatedListener {
    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state.asStateFlow()
    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()
    private var productDetails: Map<String, ProductDetails> = emptyMap()

    init { connect() }

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: com.android.billingclient.api.BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    refreshPurchases()
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Google Play billing is unavailable (${result.debugMessage}).")
                }
            }
            override fun onBillingServiceDisconnected() { _state.value = _state.value.copy(isLoading = false, error = "Google Play billing disconnected. Try again later.") }
        })
    }

    private fun queryProducts() {
        val products = AppConstants.productIds.map { id -> QueryProductDetailsParams.Product.newBuilder().setProductId(id).setProductType(BillingClient.ProductType.SUBS).build() }
        billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(products).build()) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = detailsResult.productDetailsList
                productDetails = details.associateBy { it.productId }
                _state.value = _state.value.copy(prices = details.associate { it.productId to (it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "Available in Google Play") }, isLoading = false)
            } else _state.value = _state.value.copy(isLoading = false, error = "Subscription products are not available yet. Add the configured product IDs in Play Console.")
        }
    }

    private fun refreshPurchases() {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) updateOwnedPurchases(purchases)
        }
    }

    fun purchase(activity: Activity, productId: String) {
        val details = productDetails[productId] ?: run { _state.value = _state.value.copy(error = "Product is not loaded yet. Try again in a moment."); return }
        val offer = details.subscriptionOfferDetails?.firstOrNull() ?: run { _state.value = _state.value.copy(error = "This subscription has no eligible offer configured in Google Play."); return }
        val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(offer.offerToken).build()
        billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
    }

    fun restore() { if (billingClient.isReady) refreshPurchases() else connect() }

    override fun onPurchasesUpdated(result: com.android.billingclient.api.BillingResult, purchases: List<Purchase>?) {
        when {
            result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> updateOwnedPurchases(purchases)
            result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _state.value = _state.value.copy(error = "Purchase could not be completed. ${result.debugMessage}")
        }
    }

    private fun updateOwnedPurchases(purchases: List<Purchase>) {
        // Server-side purchase verification is still required before production entitlement use.
        purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }.forEach { purchase ->
            billingClient.acknowledgePurchase(com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) { }
        }
        _state.value = _state.value.copy(ownedProductIds = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.flatMap { it.products }.toSet())
    }
}
