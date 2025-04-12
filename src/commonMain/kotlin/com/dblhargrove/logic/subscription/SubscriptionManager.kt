package com.dblhargrove.logic.subscription

import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo

object SubscriptionManager {
    var hasPremium: Boolean = false

    suspend fun updatePremiumStatus() {
        try {
            Purchases.sharedInstance.getCustomerInfo(
                onError = { error -> /* Optional error handling */ },
                onSuccess = { customerInfo ->
                    val entitlementId = "premium"
                    val isActive = customerInfo.entitlements[entitlementId]?.isActive == true
                    hasPremium = isActive
                    log(LogLevel.DEBUG, "RevenueCat", "Premium status: $hasPremium")
                },
            )
        } catch (e: Exception) {
            log(LogLevel.ERROR, "RevenueCat", "Failed to fetch customer info: ${e.message}")
        }
    }
}