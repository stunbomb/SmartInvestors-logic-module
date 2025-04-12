package com.dblhargrove.database.Portfolio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.dblhargrove.database.Portfolio.Asset

@Dao
interface AssetDao {

    @Query("SELECT * FROM Asset WHERE portfolioId = :portfolioId")
    suspend fun getAssetsByPortfolio(portfolioId: Int): List<Asset>

    @Query("SELECT * FROM Asset WHERE portfolioId = :portfolioId AND symbol = :symbol LIMIT 1")
    suspend fun getAssetByPortfolioAndSymbolSync(portfolioId: Int, symbol: String): Asset?

    @Upsert
    suspend fun insertAsset(asset: Asset)  // ✅ Room now treats portfolioId + symbol as unique

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssetBlocking(assets: Asset) // Create non-suspend version

    @Delete
    suspend fun deleteAsset(asset: Asset)

    @Query("SELECT * FROM Asset")
    suspend fun getAllAssets(): List<Asset>

    @Query("DELETE FROM Asset")
    suspend fun clearAllAssets()

    // ✅ This should use `Upsert` to prevent conflicts when bulk inserting
    @Upsert
    suspend fun insertAll(assets: List<Asset>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllBlocking(assets: List<Asset>) // Create non-suspend version


    // ✅ Update stop-loss for a specific asset (Now based on portfolioId & symbol, not id)
    @Query("UPDATE Asset SET stopLoss = :stopLoss WHERE portfolioId = :portfolioId AND symbol = :symbol")
    suspend fun updateStopLoss(portfolioId: Int, symbol: String, stopLoss: Double)

    // ✅ Fetch assets where the current price is below or equal to the stop-loss
    @Query("SELECT * FROM Asset WHERE currentPrice <= stopLoss")
    suspend fun getAssetsToSell(): List<Asset>

    @Query("SELECT * FROM Asset WHERE portfolioId = :portfolioId")
    suspend fun getAssetsByPortfolioId(portfolioId: Int): List<Asset>
}
