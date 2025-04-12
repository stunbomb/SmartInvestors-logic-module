package com.dblhargrove.database.Assets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Assets.Forex

@Dao
interface ForexDao {
    @Query("SELECT * FROM forex")
    suspend fun getAllForex(): List<Forex>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(forex: List<Forex>)

    @Query("DELETE FROM forex WHERE symbol NOT IN (:symbols)")
    suspend fun deleteAbsentForex(symbols: List<String>)

    @Query("SELECT * FROM forex WHERE symbol = :symbol LIMIT 1")
    fun getForexBySymbol(symbol: String): Forex?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllBlocking(forex: List<Forex>) // Create non-suspend version

    @Query("DELETE FROM forex WHERE symbol NOT IN (:forexSymbols)")
    fun deleteAbsentForexBlocking(forexSymbols: List<String>)

    @Query("SELECT asset FROM forex WHERE symbol = :symbol")
    suspend fun getAssetType(symbol: String): String?

    @Query("SELECT price FROM forex WHERE symbol = :symbol LIMIT 1")
    suspend fun getForexPriceBySymbol(symbol: String): Double?
}