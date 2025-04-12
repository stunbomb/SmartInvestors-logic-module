package com.dblhargrove.database.WatchList

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dblhargrove.database.WatchList.WatchlistItems

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM WatchList WHERE symbol = :symbol LIMIT 1")
    suspend fun getCompanyBySymbol(symbol: String): WatchlistItems?

    @Query("SELECT * FROM WatchList WHERE symbol = :symbol LIMIT 1")
    suspend fun getWatchlistItem(symbol: String): WatchlistItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(watchlist: WatchlistItems)

    @Query("DELETE FROM WatchList WHERE symbol = :symbol")
    suspend fun deleteWatchlistItem(symbol: String)

    @Query("SELECT * FROM WatchList")
    suspend fun getAllWatchlistItems(): List<WatchlistItems>

    @Update
    fun updateWatchListItem(watchlistItem: WatchlistItems)

    // New Query to fetch items by asset type
    @Query("SELECT * FROM WatchList WHERE asset = :assetType")
    suspend fun getItemsByAssetType(assetType: String): List<WatchlistItems>
    
}