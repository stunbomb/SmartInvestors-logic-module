package com.dblhargrove.database.Terms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Terms.Terms

@Dao
interface TermsDao {
    @Query("SELECT * FROM terms")
    suspend fun getAllTerms(): List<Terms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTerms(terms: List<Terms>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTermsBlocking(terms: List<Terms>) // Create non-suspend version

}