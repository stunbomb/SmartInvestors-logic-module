package com.dblhargrove.database.Assets

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "crypto")
@Serializable // Supabase/serialization annotation
data class Crypto(
    @PrimaryKey val symbol: String,
    val name: String,
    val price: Double,
    val asset: String,
    val dividend: Double, // Default value
    val logo: String? = null,
    val cumulativeReturns: String? = null,
    val senate: List<SenateTransaction>? = emptyList(), // List of Senate transactions
    val house: List<HouseTransaction>? = emptyList(),
    val quote: String? = null
)
