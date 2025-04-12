package com.dblhargrove.database.Assets

import kotlinx.serialization.Serializable

@Serializable
data class ChartData(
    val date: String,  // X-Axis (time)
    val close: Float   // Y-Axis (closing price)
)
