package com.dblhargrove.database.Portfolio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Portfolio.HistoricalAsset

@Dao
interface HistoricalAssetDao {
    // Insert a new historical asset snapshot
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalAsset(historicalAsset: HistoricalAsset)

    // Fetch all historical snapshots for a specific asset
    @Query("SELECT * FROM HistoricalAsset WHERE assetId = :assetId ORDER BY timestamp ASC")
    suspend fun getHistoricalDataForAsset(assetId: Int): List<HistoricalAsset>

    // Fetch the latest historical snapshot for a specific asset
    @Query("SELECT * FROM HistoricalAsset WHERE assetId = :assetId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHistoricalDataForAsset(assetId: Int): HistoricalAsset?

    // Check if a snapshot exists for the current day
    @Query("SELECT * FROM HistoricalAsset WHERE assetId = :assetId AND DATE(timestamp / 1000, 'unixepoch') = DATE(:currentTimestamp / 1000, 'unixepoch')")
    suspend fun getSnapshotForDay(assetId: Int, currentTimestamp: Long): HistoricalAsset?
}
