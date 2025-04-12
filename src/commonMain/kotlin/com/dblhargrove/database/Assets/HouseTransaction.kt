package com.dblhargrove.database.Assets

import kotlinx.serialization.Serializable

@Serializable
data class HouseTransaction(
    val type: String,              // e.g., "Sale"
    val amount: String,            // e.g., "$15,001 - $50,000"
    val district: String,          // e.g., "TX10"
    val disclosureDate: String,    // e.g., "2024-12-18"
    val representative: String,    // e.g., "Michael McCaul"
    val transactionDate: String    // e.g., "2024-11-19"
)

