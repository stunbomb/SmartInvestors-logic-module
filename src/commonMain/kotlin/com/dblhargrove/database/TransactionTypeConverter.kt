package com.dblhargrove.database

import androidx.room.TypeConverter
import com.dblhargrove.database.Assets.HouseTransaction
import com.dblhargrove.database.Assets.SenateTransaction
import com.dblhargrove.database.News.NewsArticle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TransactionTypeConverter {

    @TypeConverter
    fun fromSenateTransactionList(value: List<SenateTransaction>?): String {
        return Json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toSenateTransactionList(value: String): List<SenateTransaction> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromHouseTransactionList(value: List<HouseTransaction>?): String {
        return Json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toHouseTransactionList(value: String): List<HouseTransaction> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromNewsArticleList(value: List<NewsArticle>?): String {
        return Json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toNewsArticleList(value: String): List<NewsArticle> {
        return Json.decodeFromString(value)
    }

   // ✅ **New Converters for `cumulativeReturns`**
    @TypeConverter
    fun fromCumulativeReturns(value: Map<String, Double>?): String {
        return Json.encodeToString(value ?: emptyMap())
    }

    @TypeConverter
    fun toCumulativeReturns(value: String): Map<String, Double> {
        return Json.decodeFromString(value)
    }
}
