package com.dblhargrove.database.Portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "Portfolio")
@Serializable // Supabase/serialization annotation
data class Portfolio(
    @PrimaryKey(autoGenerate = true) val id: Int, // New unique ID for each portfolio
    val asset: String,
    val name: String,
    val shares: Double = 0.0,
    val balance: Double = 0.0,       // New field: Available balance for the portfolio
    var total: Double = 0.0,
    val userId: String
)


