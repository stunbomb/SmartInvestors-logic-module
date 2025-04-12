package com.dblhargrove.database.WatchList

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dblhargrove.database.Assets.HouseTransaction
import com.dblhargrove.database.Assets.SenateTransaction
import kotlinx.serialization.Serializable

@Entity(tableName = "WatchList")
@Serializable // Supabase/serialization annotation
data class WatchlistItems(
    @PrimaryKey
    val symbol: String,
    val name: String,
    val price: Double,
    val asset: String,
    val dividend: Double, // Default value
    val logo: String? = null,
    val cumulativeReturns: String? = null,
    val senate: List<SenateTransaction>? = emptyList(), // List of Senate transactions
    val house: List<HouseTransaction>? = emptyList(),
    val quote: String? = null,
    val annual: String? = null,
    val quarter: String? = null
)