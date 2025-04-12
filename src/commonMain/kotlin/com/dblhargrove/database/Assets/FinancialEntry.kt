package com.dblhargrove.database.Assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FinancialEntry(
    val year: Int,
    val data: Map<String, JsonElement> // Allows both numbers and strings
)

@Serializable
data class FinancialDataResponse(
    val key_metrics: List<FinancialEntry>? = null,
    val balance_sheet: List<FinancialEntry>? = null,
    val income_statement: List<FinancialEntry>? = null,
    val cash_flow: List<FinancialEntry>? = null
)

