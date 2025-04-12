package com.dblhargrove.database.Assets

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dblhargrove.database.DefaultDoubleSerializer
import kotlinx.serialization.Serializable

@Entity(tableName = "companies")
@Serializable // Supabase/serialization annotation

data class Company(
    @PrimaryKey val symbol: String,
    val name: String,
    val price: Double,
    val asset: String,
    @Serializable(with = DefaultDoubleSerializer::class) val dividend: Double = 0.0,
    val logo: String? = null,
    val cumulativeReturns: String? = null,
    val senate: List<SenateTransaction>? = emptyList(), // List of Senate transactions
    val house: List<HouseTransaction>? = emptyList(),
    val annual: String? = null,
    val quarter: String? = null,
    val profile : String? = null,
)

