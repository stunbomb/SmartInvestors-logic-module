package com.dblhargrove.background

import com.dblhargrove.database.AppDatabase
import com.dblhargrove.database.Assets.Company
import com.dblhargrove.database.Assets.Crypto
import com.dblhargrove.database.Assets.Forex
import com.dblhargrove.database.News.News
import com.dblhargrove.database.Terms.Terms
import com.dblhargrove.database.Videos.Videos
import com.dblhargrove.database.WatchList.WatchlistItems
import com.dblhargrove.network.SupabaseClient
import com.dblhargrove.portfolio.PortfolioViewModel
import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
* This file exists to setup titillating, scheduling, and triggering key functions in the app
*/
val assetInsertMutex = Mutex()

/*
* Function call that will retrieve list of stocks and their properties from supabase
*/
suspend fun fetchSupabaseCompanies(): List<Company> {
    val pageSize = 100L // Fetch 1000 rows per page
    var start = 0L
    var end = pageSize - 1
    val allCompanies = mutableListOf<Company>()

    try {
        while (true) {
            val companies = SupabaseClient.client.postgrest["USAStocks"]
                .select{
                    range(start, end)
                }// Fetch a range of rows
                .decodeList<Company>()

            if (companies.isEmpty()) break // Exit the loop if no more rows are returned

            allCompanies.addAll(companies)
            start += pageSize // Move to the next page
            end += pageSize
        }

        log(LogLevel.DEBUG,"CompanyLog", "Fetched ${allCompanies.size} companies from Supabase.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"CompanyLog", "Error fetching data: ${e.message}")
    }

    return allCompanies
}

/*
* Function call that will retrieve list of cryptos and their properties from supabase
*/
suspend fun fetchSupabaseCryptos(): List<Crypto> {
    val pageSize = 100L // Fetch 1000 rows per page
    var start = 0L
    var end = pageSize - 1
    val allCryptos = mutableListOf<Crypto>()

    try {
        while (true) {
            val cryptos = SupabaseClient.client.postgrest["CryptoList"]
                .select{
                    range(start, end)
                }// Fetch a range of rows
                .decodeList<Crypto>()

            if (cryptos.isEmpty()) break // Exit the loop if no more rows are returned

            allCryptos.addAll(cryptos)
            start += pageSize // Move to the next page
            end += pageSize
        }

        log(LogLevel.DEBUG,"CryptoLog", "Fetched ${allCryptos.size} cryptos from Supabase.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"CryptoLog", "Error fetching data: ${e.message}")
    }

    return allCryptos
}

/*
* Function call that will retrieve list of forexes and their properties from supabase
*/
suspend fun fetchSupabaseForexes(): List<Forex> {
    val pageSize = 100L // Fetch 1000 rows per page
    var start = 0L
    var end = pageSize - 1
    val allForexs = mutableListOf<Forex>()

    try {
        while (true) {
            val forexs = SupabaseClient.client.postgrest["ForexList"]
                .select{
                    range(start, end)
                }// Fetch a range of rows
                .decodeList<Forex>()

            if (forexs.isEmpty()) break // Exit the loop if no more rows are returned

            allForexs.addAll(forexs)
            start += pageSize // Move to the next page
            end += pageSize
        }

        log(LogLevel.DEBUG,"ForexLog", "Fetched ${allForexs.size} forexs from Supabase.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"ForexLog", "Error fetching data: ${e.message}")
    }

    return allForexs
}

/*
* Function called that does the following
* retrieve stocks, crypto, and forex from supabase along with their properties
* Insert them into room DB
* removed ones that exist in room but not in supabase
*/
suspend fun syncCompaniesWithRoom(db: AppDatabase) {
    try {
        // Step 1: Fetch data from Supabase
        val supabaseCompanies = fetchSupabaseCompanies()
        val supabaseCryptos = fetchSupabaseCryptos()
        val supabaseForexes = fetchSupabaseForexes()
        val supabaseVideos = fetchSupabaseVideos()
        val supabaseTerms = fetchSupabaseTerms()

        // Step 2: Extract all symbols from the fetched data
        val companySymbols = supabaseCompanies.map { it.symbol }
        val cryptoSymbols = supabaseCryptos.map { it.symbol }
        val forexSymbols = supabaseForexes.map { it.symbol }
        val vids = supabaseVideos.map { it.vid }

        // Step 3: Insert or update companies in Room
        withContext(Dispatchers.IO) {
            assetInsertMutex.withLock {
                // Insert data
                db.termsDao().insertAllTermsBlocking(supabaseTerms)
                db.companyDao().insertAllBlocking(supabaseCompanies)
                db.cryptoDao().insertAllBlocking(supabaseCryptos)
                db.forexDao().insertAllBlocking(supabaseForexes)
                db.videosDao().insertAllBlocking(supabaseVideos)

                // Delete data
                db.companyDao().deleteAbsentCompaniesBlocking(companySymbols)
                db.cryptoDao().deleteAbsentCryptoBlocking(cryptoSymbols)
                db.forexDao().deleteAbsentForexBlocking(forexSymbols)
                db.videosDao().deleteAbsentVideosBlocking(vids)
            }
        }

        log(LogLevel.DEBUG,"financeLog", "Supabase sync completed successfully.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"financeLog", "Error syncing companies from Supabase: ${e.message}")
    }
}

suspend fun logAllCompanies(db: AppDatabase) {
        try {
            // Retrieve all companies from Room
            val companies = db.companyDao().getAllCompanies()
            val cryptos = db.cryptoDao().getAllCryptos()
            val forexs = db.forexDao().getAllForex()

            // Log each company
            /*companies.forEach { company ->
                log(LogLevel.DEBUG,"companyLog", "Symbol: ${company.symbol}, Name: ${company.name}, Price: ${company.price}, Dividend: ${company.dividend}, Logo: ${company.logo}, returns: ${company.cumulativeReturns}, senate: ${company.senate}, house: ${company.house}")
            }*/

            // Log each company
            cryptos.forEach { crypto ->
                log(LogLevel.DEBUG,"cryptoLog", "Symbol: ${crypto.symbol}, Name: ${crypto.name}, Price: ${crypto.price}, Dividend: ${crypto.dividend}, Logo: ${crypto.logo}, returns: ${crypto.cumulativeReturns}, senate: ${crypto.senate}, house: ${crypto.house}")
            }

            // Log each company
            /*forexs.forEach { forex ->
                log(LogLevel.DEBUG,"forexLog", "Symbol: ${forex.symbol}, Name: ${forex.name}, Price: ${forex.price}, Dividend: ${forex.dividend}, Logo: ${forex.logo}, returns: ${forex.cumulativeReturns}")
            }*/

            // Log the total number of companies
            log(LogLevel.DEBUG,"financeLog", "Total companies: ${companies.size}")
            log(LogLevel.DEBUG,"financeLog", "Total cryptos: ${cryptos.size}")
            log(LogLevel.DEBUG,"financeLog", "Total forexs: ${forexs.size}")
        } catch (e: Exception) {
            log(LogLevel.ERROR,"financeLog", "Error logging companies: ${e.message}")
        }
}

/*
* Function that retrieves all news articles from supabase
*/
suspend fun fetchSupabaseNews(): List<News> {
    val pageSize = 100L // Fetch 100 rows per page
    var start = 0L
    var end = pageSize - 1
    val allNews = mutableListOf<News>()

    try {
        while (true) {
            val newsList = SupabaseClient.client.postgrest["news"]
                .select {
                    range(start, end)
                }
                .decodeList<News>()

            if (newsList.isEmpty()) break // Exit loop if no more rows

            allNews.addAll(newsList)
            start += pageSize
            end += pageSize
        }

        log(LogLevel.DEBUG,"NewsLog", "Fetched ${allNews.size} news articles from Supabase.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"NewsLog", "Error fetching news data: ${e.message}")
    }

    return allNews
}


/*
* Function that insert retrieved articles from supabase into room and remove ones in room that are not in supabase
*/
suspend fun syncNewsWithRoom(db: AppDatabase) {
    try {
        // Step 1: Fetch data from Supabase
        val supabaseNews = fetchSupabaseNews()

        // Step 2: Extract all symbols (news is tied to assets)
        val newsSymbols = supabaseNews.map { it.symbol }

        // Step 3: Insert or update news in Room
        db.newsDao().insertAllNews(supabaseNews)

        // Step 4: Remove outdated news
        db.newsDao().deleteAbsentNews(newsSymbols)

        log(LogLevel.DEBUG,"NewsSync", "News sync completed successfully.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"NewsSync", "Error syncing news from Supabase: ${e.message}")
    }
}


/*
* Function that keep assets prices in the watchlist db = to the ones in the stock, crypto, or forex table
*/
suspend fun updateWatchListPrices(db: AppDatabase) {
    withContext(Dispatchers.IO) {
        try {
            val watchListDao = db.WatchlistDao()
            val companyDao = db.companyDao()
            val cryptoDao = db.cryptoDao()
            val forexDao = db.forexDao()

            // Get all watchlist items
            val watchListItems = watchListDao.getAllWatchlistItems()

            watchListItems.forEach { watchItem ->
                val updatedPrice = when (watchItem.asset) {
                    "Stock" -> companyDao.getCompanyBySymbol(watchItem.symbol)?.price
                    "Crypto" -> cryptoDao.getCryptoBySymbol(watchItem.symbol)?.price
                    "Forex" -> forexDao.getForexBySymbol(watchItem.symbol)?.price
                    else -> null
                }

                // Update watchlist item if the price has changed
                updatedPrice?.let {
                    if (it != watchItem.price) {
                        watchListDao.updateWatchListItem(
                            WatchlistItems(watchItem.symbol, watchItem.name, it, watchItem.asset, watchItem.dividend)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            log(LogLevel.ERROR,"WatchListUpdate", "Error updating watchlist prices: ${e.message}")
        }
    }
}

/*
* Function that keep assets prices in the asset db = to the ones in the stock, crypto, or forex table
* This is done to make sure users assets values are up to date
*/
suspend fun updateAssetPrices(db: AppDatabase) {
    withContext(Dispatchers.IO) {
        try {
            val assetDao = db.assetDao()
            val companyDao = db.companyDao()
            val cryptoDao = db.cryptoDao()
            val forexDao = db.forexDao()

            // Get all assets (assuming this returns a List<Asset>)
            val assetList = assetDao.getAllAssets()  // Adjust this method to fit your asset DAO

            assetList.forEach { asset ->
                // Fetch the latest price from the relevant table
                val updatedPrice = when (asset.symbol) {
                    "Stock" -> companyDao.getCompanyBySymbol(asset.symbol)?.price
                    "Crypto" -> cryptoDao.getCryptoBySymbol(asset.symbol)?.price
                    "Forex" -> forexDao.getForexBySymbol(asset.symbol)?.price
                    else -> null
                }

                // If a new price is found, recalculate the asset's current price
                updatedPrice?.let { newPrice ->
                    if (newPrice != asset.currentPrice) {
                        val updatedAsset = asset.copy(
                            currentPrice = newPrice, // Update with the new price
                            shares = asset.shares  // Keep the shares unchanged
                        )

                        // Update the asset in the database
                        assetDao.insertAsset(updatedAsset)  // Assuming `upsertAsset` can insert/update
                    }
                }
            }
        } catch (e: Exception) {
            log(LogLevel.ERROR,"AssetUpdate", "Error updating asset prices: ${e.message}")
        }
    }
}

/*
* Function used to constant monitor the stop loss set by the user for an asset.
* Increase stop loss as the value increases
* initials the sale once the stop loss is reached*/
suspend fun monitorStopLossForAssets(db: AppDatabase, portfolioViewModel: PortfolioViewModel) {
    withContext(Dispatchers.IO) {
        try {
            val assetDao = db.assetDao()

            // Get all assets
            val assetList = assetDao.getAllAssets()  // Adjust this method to fit your asset DAO

            assetList.forEach { asset ->
                // Call the stopLossPoint() function for each asset and get the updated stopLossSell
                val updatedStopLossSell = portfolioViewModel.stopLossPoint(asset)

                // If the function determines a sale, handle it accordingly
                if (updatedStopLossSell == 0.0) {
                    // The asset should be sold
                    val amountFromSale = asset.shares * asset.currentPrice
                    portfolioViewModel.updatePortfolioBalanceforAssets(asset.portfolioId, amountFromSale)
                    portfolioViewModel.deleteAsset(asset)
                    //Log("StopLoss", "Asset sold due to stop loss: ${asset.symbol}")
                } else {
                    // Otherwise, just update the stopLossSell if it has changed
                    if (updatedStopLossSell != asset.stopLossSell) {
                        val updatedAsset = asset.copy(stopLossSell = updatedStopLossSell)
                        assetDao.insertAsset(updatedAsset)
                        //Log("StopLoss", "Updated stopLossSell for asset: ${asset.symbol}")
                    }
                }
            }
        } catch (e: Exception) {
            log(LogLevel.ERROR,"StopLossMonitor", "Error monitoring stop loss for assets: ${e.message}")
        }
    }
}

/*
* Function used to retrieve terms from supabase dictionary and save them in room
*/
suspend fun fetchSupabaseTerms(): List<Terms> {
    val pageSize = 100L // Fetch 1000 rows per page
    var start = 0L
    var end = pageSize - 1
    val allTerms = mutableListOf<Terms>()

    try {
        while (true) {
            val terms = SupabaseClient.client.postgrest["TermsList"]
                .select{
                    range(start, end)
                }// Fetch a range of rows
                .decodeList<Terms>()

            if (terms.isEmpty()) break // Exit the loop if no more rows are returned

            allTerms.addAll(terms)
            start += pageSize // Move to the next page
            end += pageSize
        }

        log(LogLevel.DEBUG,"TermsLog", "Fetched ${allTerms.size} terms from Supabase.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"TermsLog", "Error fetching data: ${e.message}")
    }

    return allTerms
}

/*
* Function used to retrieve terms from supabase dictionary and save them in room
*/
suspend fun fetchSupabaseVideos(): List<Videos> {
    val pageSize = 100L // Fetch 1000 rows per page
    var start = 0L
    var end = pageSize - 1
    val allVideos = mutableListOf<Videos>()

    try {
        while (true) {
            val videos = SupabaseClient.client.postgrest["videos"]
                .select{
                    range(start, end)
                }// Fetch a range of rows
                .decodeList<Videos>()

            log(LogLevel.DEBUG,"VideosLog", "Raw Response: $videos")

            if (videos.isEmpty()) break // Exit the loop if no more rows are returned

            allVideos.addAll(videos)
            start += pageSize // Move to the next page
            end += pageSize
        }

        log(LogLevel.DEBUG,"VideosLog", "Fetched ${allVideos.size} videos from Supabase.")
    } catch (e: Exception) {
        log(LogLevel.ERROR,"VideosLog", "Error fetching data: ${e.message}")
    }

    return allVideos
}