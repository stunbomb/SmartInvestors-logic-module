package com.dblhargrove.database.Portfolio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Portfolio.Portfolio
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    // Get all portfolios
    @Query("SELECT * FROM Portfolio")
    fun getAllPortfolios(): Flow<List<Portfolio>>

    // Get a specific portfolio by its ID
    @Query("SELECT * FROM Portfolio WHERE id = :portfolioId LIMIT 1")
    fun getPortfolioById(portfolioId: Int): Flow<Portfolio?>

    // Get a specific portfolio by its ID (synchronous)
    @Query("SELECT * FROM Portfolio WHERE id = :portfolioId LIMIT 1")
    suspend fun getPortfolioByIdSync(portfolioId: Int): Portfolio?

    @Query("SELECT * FROM Portfolio")
    suspend fun getAllPortfoliosSync(): List<Portfolio>

    // Insert or update a portfolio
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: Portfolio)

    // Delete a portfolio by its ID (optional if you want to implement delete functionality)
    @Query("DELETE FROM Portfolio WHERE id = :portfolioId")
    suspend fun deletePortfolio(portfolioId: Int)

    // Update balance and total value for a portfolio (based on portfolio ID)
    @Query("UPDATE Portfolio SET balance = :balance, total = :total WHERE id = :portfolioId")
    suspend fun updatePortfolioBalance(portfolioId: Int, balance: Double, total: Double)

    @Query("DELETE FROM Portfolio")
    suspend fun clearAllPortfolios()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(portfolios: List<Portfolio>)
}