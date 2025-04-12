package com.dblhargrove.database.Portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HistoricalPortfolio")
data class HistoricalPortfolio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Unique ID
    val portfolioId: Int,  // Foreign key linking to Portfolio
    val timestamp: Long,   // Time of the snapshot (e.g., System.currentTimeMillis())
    val balance: Double,   // Available balance at the time
    val total: Double      // Total value of portfolio (assets + balance)
)