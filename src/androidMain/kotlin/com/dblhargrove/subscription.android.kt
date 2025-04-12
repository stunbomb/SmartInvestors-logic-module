package com.dblhargrove

import android.content.Context
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

lateinit var revenueCatContext: Context

actual fun initializeRevenueCat() {
    Purchases.configure(
        PurchasesConfiguration.Builder( "goog_OPWJCmprAzIPYYZtiVsSClVaUGL").build()
    )
}
