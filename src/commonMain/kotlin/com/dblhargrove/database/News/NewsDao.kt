package com.dblhargrove.database.News

import androidx.room.*
import com.dblhargrove.database.News.News

@Dao
interface NewsDao {

    @Query("SELECT * FROM news WHERE symbol = :symbol")
    suspend fun getNewsBySymbol(symbol: String): News? // Return News instead of List<NewsArticle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: News) // Accepts a single news entry

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNews(newsList: List<News>) // Allows batch inserts

    @Query("DELETE FROM news WHERE symbol NOT IN (:symbols)")
    suspend fun deleteAbsentNews(symbols: List<String>)

    @Query("SELECT * FROM news WHERE asset = :assetType")
    suspend fun getNewsByAsset(assetType: String): List<News>

    /*Need to prevent loading too much data at once crash*/
    @Query("SELECT * FROM news WHERE asset = :assetType LIMIT :limit OFFSET :offset")
    suspend fun getNewsByAssetPaginated(assetType: String, limit: Int, offset: Int): List<News>

    @Query("SELECT * FROM news WHERE asset = :assetType LIMIT :pageSize OFFSET :offset")
    suspend fun getPaginatedNewsByAsset(assetType: String, pageSize: Int, offset: Int): List<News>



}

