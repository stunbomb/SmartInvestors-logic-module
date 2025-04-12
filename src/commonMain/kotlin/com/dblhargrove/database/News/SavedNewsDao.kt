package com.dblhargrove.database.News

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.News.SavedArticle

@Dao
interface SavedNewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: SavedArticle)

    @Query("SELECT * FROM savedarticle")
    suspend fun getAllSavedArticles(): List<SavedArticle>

    @Query("DELETE FROM savedarticle WHERE url = :url")
    suspend fun deleteArticleByUrl(url: String)

    @Delete
    suspend fun deleteArticle(article: SavedArticle)
}
