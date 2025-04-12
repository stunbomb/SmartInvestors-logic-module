package com.dblhargrove

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

actual fun initializeRevenueCat() {
    val configuration = PurchasesConfiguration(apiKey = "appl_your_ios_api_key_here")
    Purchases.configure(configuration)
}