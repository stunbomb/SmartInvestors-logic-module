package com.dblhargrove.database.News

import kotlinx.serialization.Serializable


@Serializable
data class NewsTransaction(
    val url: String,              // e.g., "Sale"
    val title: String,
    val site: String,            // e.g., "$15,001 - $50,000"
    val text: String,          // e.g., "TX10"
    val image: String,    // e.g., "2024-12-18"
    val symbol: String,    // e.g., "Michael McCaul"
    val publishedDate: String    // e.g., "2024-11-19"
)
