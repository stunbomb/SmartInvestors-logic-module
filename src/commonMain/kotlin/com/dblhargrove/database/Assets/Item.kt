package com.dblhargrove.database.Assets

data class Item(
    val name: String,
    val symbol: String,
    val price: String,
    val Asset: String,
    val dividend: Double, // Default value
    val logo: String? = null,
    val cumulativeReturns: String? = null,
)
