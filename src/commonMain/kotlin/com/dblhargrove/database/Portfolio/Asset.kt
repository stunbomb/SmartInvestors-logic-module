package com.dblhargrove.database.Portfolio

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(
    tableName = "Asset",
    primaryKeys = ["portfolioId", "symbol"]  // ✅ Composite primary key
)
@Serializable
data class Asset(
    val portfolioId: Int,  // Foreign key linking the asset to its portfolio
    val symbol: String,  // Asset's symbol (e.g., AAPL for Apple)
    val company_name: String,  // Company name for the asset
    val shares: Double,  // Number of shares owned
    val purchasePrice: Double,  // Purchase price for the asset
    val currentPrice: Double,  // Current price of the asset
    val stopLoss: Double,      // Stop loss of asset
    val stopLossSell: Double,   // Amount that will be sold once reached
    val asset: String,
    var totalvalue: Double
)
