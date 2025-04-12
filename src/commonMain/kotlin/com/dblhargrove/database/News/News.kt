package com.dblhargrove.database.News

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "news")
@Serializable
data class News(
    @PrimaryKey val symbol: String,  // Primary key is the asset symbol
    val name: String,
    val asset: String,
    val news: List<NewsArticle> // Stores the list of news articles
)

@Serializable
data class NewsArticle(
    @PrimaryKey val url: String, // URL as a unique identifier
    val title: String,
    val image: String? = null,
    val site: String,
    val text: String,
    val publishedDate: String
)

