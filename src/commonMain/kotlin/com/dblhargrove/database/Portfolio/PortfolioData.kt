package com.dblhargrove.database.Portfolio

import com.dblhargrove.database.Assets.Item

data class PortfolioAsset(
    val asset: Item,   // The asset being tracked
    val shares: Double // The number of shares owned by the user
)
