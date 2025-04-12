package com.dblhargrove.database.Assets

import kotlinx.serialization.Serializable

@Serializable
data class SenateTransaction(
    val type: String? = null,
    val amount: String? = null,
    val office: String? = null,
    val lastName: String? = null,
    val firstName: String? = null,
    val dateRecieved: String? = null,
    val transactionDate: String? = null
)

