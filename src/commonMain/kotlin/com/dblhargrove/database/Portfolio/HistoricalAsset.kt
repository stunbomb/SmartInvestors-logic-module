package com.dblhargrove.database.Portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HistoricalAsset")
data class HistoricalAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Unique ID
    val assetId: Int,       // Foreign key linking to Asset
    val timestamp: Long,    // Time of the snapshot
    val shares: Double,     // Number of shares at the time
    val currentPrice: Double, // Current price at the time
    val totalValue: Double  // Total value of the asset (shares * currentPrice)
)
