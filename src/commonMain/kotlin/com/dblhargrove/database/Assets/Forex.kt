package com.dblhargrove.database.Assets

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "forex")
@Serializable // Supabase/serialization annotation
data class Forex(
    @PrimaryKey val symbol: String,
    val name: String,
    val price: Double,
    val asset: String,
    val dividend: Double, // Default value
    val logo: String? = null,
    val cumulativeReturns: String? = null,
    val quote: String? = null
)
