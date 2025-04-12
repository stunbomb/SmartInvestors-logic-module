package com.dblhargrove.database.News

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savedarticle")
data class SavedArticle(
    @PrimaryKey val url: String, // URL as a unique identifier
    val title: String,
    val image: String? = null,
    val site: String,
    val text: String,
    val publishedDate: String
)
