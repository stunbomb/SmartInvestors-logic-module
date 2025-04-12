package com.dblhargrove.database.Portfolio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Portfolio.HistoricalPortfolio

@Dao
interface HistoricalPortfolioDao {
    // Insert a new historical portfolio snapshot
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalPortfolio(historicalPortfolio: HistoricalPortfolio)

    // Fetch all historical snapshots for a specific portfolio
    @Query("SELECT * FROM HistoricalPortfolio WHERE portfolioId = :portfolioId ORDER BY timestamp ASC")
    suspend fun getHistoricalDataForPortfolio(portfolioId: Int): List<HistoricalPortfolio>

    // Fetch the latest historical snapshot for a specific portfolio
    @Query("SELECT * FROM HistoricalPortfolio WHERE portfolioId = :portfolioId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHistoricalDataForPortfolio(portfolioId: Int): HistoricalPortfolio?

}
