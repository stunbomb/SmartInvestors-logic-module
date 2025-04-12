package com.dblhargrove.database.Assets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Assets.Company

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies")
    suspend fun getAllCompanies(): List<Company>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(companies: List<Company>)

    @Query("DELETE FROM companies WHERE symbol NOT IN (:symbols)")
    suspend fun deleteAbsentCompanies(symbols: List<String>)

    @Query("SELECT * FROM companies WHERE symbol = :symbol LIMIT 1")
    fun getCompanyBySymbol(symbol: String): Company?

    @Query("SELECT * FROM companies LIMIT :batchSize OFFSET :offset")
    suspend fun getCompaniesBatch(batchSize: Int, offset: Int): List<Company>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllBlocking(companies: List<Company>) // Create non-suspend version

    @Query("DELETE FROM companies WHERE symbol NOT IN (:companySymbols)")
    fun deleteAbsentCompaniesBlocking(companySymbols: List<String>)

    @Query("SELECT asset FROM companies WHERE symbol = :symbol")
    suspend fun getAssetType(symbol: String): String?

    @Query("SELECT price FROM companies WHERE symbol = :symbol LIMIT 1")
    suspend fun getStockPriceBySymbol(symbol: String): Double?

}