package com.dblhargrove.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dblhargrove.database.Videos.Videos
import com.dblhargrove.database.Videos.VideosDao
import com.dblhargrove.database.Assets.Company
import com.dblhargrove.database.Assets.CompanyDao
import com.dblhargrove.database.Assets.Crypto
import com.dblhargrove.database.Assets.CryptoDao
import com.dblhargrove.database.Assets.Forex
import com.dblhargrove.database.Assets.ForexDao
import com.dblhargrove.database.News.News
import com.dblhargrove.database.News.NewsDao
import com.dblhargrove.database.News.SavedArticle
import com.dblhargrove.database.News.SavedNewsDao
import com.dblhargrove.database.Portfolio.Asset
import com.dblhargrove.database.Portfolio.AssetDao
import com.dblhargrove.database.Portfolio.HistoricalAsset
import com.dblhargrove.database.Portfolio.HistoricalAssetDao
import com.dblhargrove.database.Portfolio.HistoricalPortfolio
import com.dblhargrove.database.Portfolio.HistoricalPortfolioDao
import com.dblhargrove.database.Portfolio.Portfolio
import com.dblhargrove.database.Portfolio.PortfolioDao
import com.dblhargrove.database.Terms.Terms
import com.dblhargrove.database.Terms.TermsDao
import com.dblhargrove.database.WatchList.WatchlistDao
import com.dblhargrove.database.WatchList.WatchlistItems

@Database(entities = [Company::class, Crypto::class, Forex::class, WatchlistItems::class, Portfolio::class, Asset::class, SavedArticle::class, News::class, Terms::class, HistoricalPortfolio::class, HistoricalAsset::class, Videos::class], version = 1,exportSchema = false)


@TypeConverters(TransactionTypeConverter::class) // Register the converter
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun cryptoDao(): CryptoDao
    abstract fun forexDao(): ForexDao
    abstract fun WatchlistDao(): WatchlistDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun assetDao(): AssetDao
    abstract fun savedNewsDao(): SavedNewsDao
    abstract fun termsDao(): TermsDao
    abstract fun historicalPortfolioDao(): HistoricalPortfolioDao
    abstract fun historicalAssetDao(): HistoricalAssetDao
    abstract fun newsDao(): NewsDao
    abstract fun videosDao(): VideosDao
}