package com.dblhargrove.database.Assets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Assets.Crypto

@Dao
interface CryptoDao {
    @Query("SELECT * FROM crypto")
    suspend fun getAllCryptos(): List<Crypto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crypto: List<Crypto>)

    @Query("DELETE FROM crypto WHERE symbol NOT IN (:symbols)")
    suspend fun deleteAbsentCrypto(symbols: List<String>)

    @Query("SELECT * FROM crypto WHERE symbol = :symbol LIMIT 1")
    fun getCryptoBySymbol(symbol: String): Crypto?

    @Query("UPDATE crypto SET cumulativeReturns = :cumulativeReturns WHERE symbol = :symbol")
    suspend fun updateCumulativeReturns(symbol: String, cumulativeReturns: Map<String, Double>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllBlocking(crypto: List<Crypto>) // Create non-suspend version

    @Query("DELETE FROM crypto WHERE symbol NOT IN (:cryptoSymbols)")
    fun deleteAbsentCryptoBlocking(cryptoSymbols: List<String>)

    @Query("SELECT asset FROM crypto WHERE symbol = :symbol")
    suspend fun getAssetType(symbol: String): String?

    @Query("SELECT price FROM crypto WHERE symbol = :symbol LIMIT 1")
    suspend fun getCryptoPriceBySymbol(symbol: String): Double?
}