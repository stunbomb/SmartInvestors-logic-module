package com.dblhargrove.portfolio


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dblhargrove.database.AppDatabase
import com.dblhargrove.logic.database.DatabaseProvider
import com.dblhargrove.database.Portfolio.Asset
import com.dblhargrove.database.Portfolio.HistoricalPortfolio
import com.dblhargrove.database.Portfolio.Portfolio
import com.dblhargrove.network.SupabaseClient.client
import com.dblhargrove.logic.readFile
import com.dblhargrove.logic.writeFile
import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.random.Random

class PortfolioViewModel(): ViewModel() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val db = DatabaseProvider.getDatabase()
    val portfolioDao = db.portfolioDao()
    var assetDao = db.assetDao()
    var historicalPortfolioDao = db.historicalPortfolioDao() // Add this
    val timestampMillis: Long = Clock.System.now().toEpochMilliseconds()
    val assetInsertMutex = Mutex()


    // ✅ Use the existing `_portfolios` instead of redeclaring it inside the function
    private val _portfolios = MutableStateFlow<List<Portfolio>>(emptyList())
    val portfolios: StateFlow<List<Portfolio>> get() = _portfolios

    init {
        log(LogLevel.DEBUG,"PortfolioViewModel", "Initializing PortfolioViewModel...")

        viewModelScope.launch {
            portfolioDao.getAllPortfolios().collectLatest { latestPortfolios ->
                _portfolios.value = latestPortfolios
                log(LogLevel.DEBUG,
                    "PortfolioViewModel",
                    "Portfolios updated in ViewModel: ${latestPortfolios.size}"
                )
            }
        }

    }

    /* Observe changes in the Portfolio table and sync with supabase
    * When every a portfolio has a change it automatically updated in supabase to match said change
    * */

    // ✅ Function to return all portfolios (does NOT recollect the Flow)
    fun getAllPortfolios(): List<Portfolio> {
        return _portfolios.value
    }

    /*
    * Function to Calculate dividen of a stock asset
    * */
    fun dividendUpdate(symbol: String){}


    /*
    * Function that updates the portfolio balance based on the amount of money deposited from the bank
    * portfolioId: the unique id of the portfolio whose balance is being updated
    * amount: the amount of money being transferred from the bank
    */
    fun updatePortfolioBalance(portfolioId: Int, amount: Double) {

        coroutineScope.launch {
            try {
                // Get the portfolio by its ID
                val portfolio = portfolioDao.getPortfolioByIdSync(portfolioId)

                if (portfolio != null) {
                    // Update the portfolio's balance
                    val updatedBalance = portfolio.balance + amount
                    val updatedPortfolio = portfolio.copy(balance = updatedBalance)
                    portfolioDao.insertPortfolio(updatedPortfolio)

                    log(LogLevel.DEBUG,"PortfolioUpdate", "Portfolio balance updated: $updatedPortfolio")

                    // Calculate the total value (balance + total asset value)
                    val assets = assetDao.getAssetsByPortfolio(portfolioId)
                    val totalAssetValue = assets.sumOf { it.shares * it.currentPrice }
                    val total = updatedBalance + totalAssetValue

                    // Insert a snapshot into the HistoricalPortfolio table
                    val historicalSnapshot = HistoricalPortfolio(
                        portfolioId = portfolioId,
                        timestamp = timestampMillis,
                        balance = updatedBalance,
                        total = total
                    )
                    historicalPortfolioDao.insertHistoricalPortfolio(historicalSnapshot)
                    savePortfolioToFirebase()

                    log(LogLevel.DEBUG,"PortfolioUpdate", "Historical snapshot added: $historicalSnapshot")
                } else {
                    log(LogLevel.ERROR,"PortfolioUpdate", "Portfolio not found for ID: $portfolioId")
                }
            } catch (e: Exception) {
                log(LogLevel.ERROR,"PortfolioUpdate", "Error updating portfolio balance: ${e.message}")
            }
        }

    }

    /*
    * Function that updates the Portfolio's balance after selling off its asset(s)
    * portfolioId: is the unique id of the portfolio that is holding the asset(s) in question
    * amountFromSale: the amount made from the sale of the asset(s)
    */
    suspend fun updatePortfolioBalanceforAssets(portfolioId: Int, amountFromSale: Double) {
        val portfolio = portfolioDao.getPortfolioByIdSync(portfolioId)
        portfolio?.let {
            try {
                // Update the portfolio balance
                val updatedBalance = it.balance + amountFromSale
                val updatedPortfolio = it.copy(balance = updatedBalance)
                portfolioDao.insertPortfolio(updatedPortfolio)

                log(LogLevel.DEBUG,"PortfolioUpdate", "Portfolio balance updated after asset sale: $updatedPortfolio")

                // Calculate the total value (balance + total asset value)
                val assets = assetDao.getAssetsByPortfolio(portfolioId)
                val totalAssetValue = assets.sumOf { asset -> asset.shares * asset.currentPrice }
                val total = updatedBalance + totalAssetValue

                // Insert a snapshot into the HistoricalPortfolio table
                val historicalSnapshot = HistoricalPortfolio(
                    portfolioId = portfolioId,
                    timestamp = timestampMillis,
                    balance = updatedBalance,
                    total = total
                )
                historicalPortfolioDao.insertHistoricalPortfolio(historicalSnapshot)
                savePortfolioToFirebase()

                log(LogLevel.DEBUG,"PortfolioUpdate", "Historical snapshot added after asset sale: $historicalSnapshot")
            } catch (e: Exception) {
                log(LogLevel.ERROR,"PortfolioUpdate", "Error updating portfolio balance after asset sale: ${e.message}")
            }
        } ?: log(LogLevel.ERROR,"PortfolioUpdate", "Portfolio not found for ID: $portfolioId")
    }

    /*
    * Function called when ever a portfolio is being created
    * portfolioName: is the name given to the portfolio
    * type: is the the asset class the portfolio will hold
    * each created portfolio tagged to the unique id of the user for authentication and logging.
    */
    fun createPortfolio(portfolioName: String, type: String) {
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            log(LogLevel.ERROR,"PortfolioViewModel", "User not logged in. Portfolio cannot be created.")
            return
        }

        coroutineScope.launch {
            val portfolio = Portfolio(
                id = Random.nextInt(100000, 999999), // You'll need a function to generate random IDs
                asset = generateSymbolFromType(type),
                name = portfolioName,
                shares = 0.0,
                balance = 0.0,
                total = 0.0,
                userId = userId  // Add the userId field to associate it with the logged-in user
            )

            // Insert portfolio into the local Room database
            portfolioDao.insertPortfolio(portfolio)
            log(LogLevel.DEBUG,"SupabaseDebug", "Attempting to insert portfolio in room: $portfolio")
            delay(500)

            // Save portfolio to Firebase
            savePortfolioToFirebase()

            // 🔹 Create portfolio history JSON
            createPortfolioHistoryJson(portfolio)
        }

    }

    /*
    * Creates a JSON file that tracks the daily performance of the user's portfolio
    * */
    @Serializable
    data class HistoryEntry(
        val timestamp: Long,
        val shares: Double,
        val balance: Double,
        val total: Double
    )

    @Serializable
    data class HistoryData(
        val portfolioId: Int,
        val userId: String,
        val name: String,
        val asset: String,
        val history: List<HistoryEntry>
    )

    fun createPortfolioHistoryJson(portfolio: Portfolio) {
        val historyData = HistoryData(
            portfolioId = portfolio.id,
            userId = portfolio.userId,
            name = portfolio.name,
            asset = portfolio.asset,
            history = listOf(
                HistoryEntry(
                    timestamp = timestampMillis,
                    shares = portfolio.shares,
                    balance = portfolio.balance,
                    total = portfolio.total
                )
            )
        )

        val jsonString = Json.encodeToString(historyData)

        val fileName = "portfolio_${portfolio.id}.json"

        try {
            writeFile(fileName, jsonString)

            println("Created portfolio history JSON at:")
        } catch (e: Exception) {
            println("Failed to write history file: ${e.message}")
        }
    }

    /*
    * This function updates the JSON daily*/
    fun updatePortfolioHistoryJson(portfolioId: Int, total: Double) {
        val fileName = "portfolio_${portfolioId}.json"
        val existingJson = readFile(fileName)

        if (existingJson == null) {
            println("No history file found for Portfolio ID=$portfolioId")
            return
        }

        try {
            // Decode the existing JSON into HistoryData
            val currentData = Json.decodeFromString<HistoryData>(existingJson)

            // Create a new entry
            val newEntry = HistoryEntry(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                shares = currentData.history.lastOrNull()?.shares ?: 0.0,
                balance = currentData.history.lastOrNull()?.balance ?: 0.0,
                total = total
            )

            // Create updated HistoryData with the new entry appended
            val updatedData = currentData.copy(
                history = currentData.history + newEntry
            )

            // Encode and write back to file
            val updatedJson = Json.encodeToString(updatedData)
            writeFile(fileName, updatedJson)

            println("Updated history for Portfolio ID=$portfolioId")

        } catch (e: Exception) {
            println("Failed to update history file for Portfolio ID=$portfolioId: ${e.message}")
        }
    }


    /*Function to Read and Extract the JSON data
    * */
    fun loadPortfolioHistory(portfolioId: Int): List<Pair<Long, Double>> {
        val fileName = "portfolio_${portfolioId}.json"
        val jsonString = readFile(fileName)

        if (jsonString == null) {
            log(LogLevel.ERROR, "PortfolioHistory", "No history file found for Portfolio ID=$portfolioId")
            return emptyList()
        }

        return try {
            val historyData = Json.decodeFromString<HistoryData>(jsonString)

            historyData.history.map { entry ->
                entry.timestamp to entry.total
            }

        } catch (e: Exception) {
            log(LogLevel.ERROR, "PortfolioHistory", "Failed to read history for Portfolio ID=$portfolioId: ${e.message}")
            emptyList()
        }
    }


    /*This functions updates the total value of an asset in a portfolio
    */
    suspend fun updatePortfolioTotal(db: AppDatabase) {
        val portfolioDao = db.portfolioDao()
        val assetDao = db.assetDao()

        // Fetch all portfolios
        val allPortfolios = portfolioDao.getAllPortfoliosSync()

        for (portfolio in allPortfolios) {
            // ✅ First, update the total value of each asset in this portfolio
            calculateTotalValueForAssets(portfolio.id, db)

            // Fetch updated assets
            val assets = assetDao.getAssetsByPortfolioId(portfolio.id)

            // Calculate new total value
            val newTotal = assets.sumOf { it.totalvalue }

            // ✅ Update the portfolio's total in the database
            portfolio.total += newTotal

            log(LogLevel.DEBUG,"UpdatePortfolioTotal", "Updated Portfolio ID=${portfolio.id} total to $newTotal")
        }
    }


    /*Function that calculates the total value of all assets in a portolfio
    * */
    suspend fun calculateTotalValueForAssets(portfolioId: Int, db: AppDatabase) {
        val assetDao = db.assetDao()

        // Fetch all assets in the portfolio
        val assets = assetDao.getAssetsByPortfolioId(portfolioId)

        for (asset in assets) {
            val newTotalValue = asset.currentPrice * asset.shares

            // Update the asset's total value
            asset.totalvalue += newTotalValue

            log(LogLevel.DEBUG,"CalculateTotalValue", "Updated ${asset.symbol} totalvalue to $newTotalValue")
        }
    }


    /*
    * Function that saves the portfolio to superbase.
    * The function takes all the portfolios on the device and saves them to superbase.
    * */
    fun savePortfolioToFirebase() {
        log(LogLevel.DEBUG,"SupabaseDebug", "savePortfolioToFirebase() is being called.")
        coroutineScope.launch {
            val allPortfolios = portfolioDao.getAllPortfoliosSync() // Fetch all stored portfolios
            log(LogLevel.DEBUG,"RoomDebug", "Portfolios currently in Room: $allPortfolios")

            if (allPortfolios.isEmpty()) {
                log(LogLevel.DEBUG,"SupabaseSync", "No portfolios to save.")
                return@launch
            }

            // 🔹 Ensure `balance`, `total`, and `shares` are NEVER null before sending to Supabase
            val sanitizedPortfolios = allPortfolios.map { portfolio ->
                portfolio.copy(
                    shares = portfolio.shares.takeUnless { it.isNaN() } ?: 0.0,
                    balance = portfolio.balance.takeUnless { it.isNaN() } ?: 0.0,
                    total = portfolio.total.takeUnless { it.isNaN() } ?: 0.0,
                )
            }

            sanitizedPortfolios.forEach {
                log(LogLevel.DEBUG,"SupabaseDebug", "Portfolio being sent -> ID: ${it.id}, Shares: ${it.shares}, Balance: ${it.balance}, Total: ${it.total}")
            }

            try {
                log(LogLevel.DEBUG,"SupabaseDebug", "Attempting to upsert portfolios: $sanitizedPortfolios")
                client.from("portfolios").upsert(sanitizedPortfolios)
                log(LogLevel.DEBUG,"SupabaseSync", "Portfolios saved successfully to Supabase.")
            } catch (e: Exception) {
                log(LogLevel.ERROR,"SupabaseSync", "Error saving portfolios to Supabase: ${e.message}")
            }
        }
    }

    /*
    * This function stores the file in Supabase, and then deletes it from the device
    * This function can also delete it from Supabase*/
    suspend fun saveOrDeletePortfolioHistoryInSupabase(portfolioId: Int, delete: Boolean = false) {
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            log(LogLevel.ERROR, "Supabase", "User not authenticated. Cannot perform actions.")
            return
        }

        val fileName = "portfolio_${portfolioId}.json"
        val folder = portfolioId.toString()
        val folderPath = "smartinvestorscharts/$userId/$folder/"
        val fullPath = "$folderPath/$fileName" // Folder structure in bucket
        val bucket = client.storage["smartinvestorscharts"]

        try {
            withContext(Dispatchers.IO) {
                if (delete) {
                    // 🔥 Delete all files in the folder (i.e. this "sub-bucket")
                    val files = bucket.list(folder)
                    for (file in files) {
                        bucket.delete(folderPath)
                    }
                    log(LogLevel.DEBUG, "Supabase", "Deleted all files in portfolio folder: $folder")
                } else {
                    val jsonString = readFile(fileName) ?: run {
                        log(LogLevel.ERROR, "Supabase", "Local history file not found for portfolio $portfolioId.")
                        return@withContext
                    }

                    val byteArray = jsonString.encodeToByteArray()
                    bucket.upload(path = fullPath, data = byteArray) {
                        upsert = true
                    }
                    log(LogLevel.DEBUG, "Supabase", "Uploaded history JSON to: $fullPath")
                }
            }
        } catch (e: Exception) {
            log(LogLevel.ERROR, "Supabase", "Failed to sync portfolio $portfolioId: ${e.message}")
        }
    }

    /*This function loads the JSON from S3
    * */
    suspend fun loadPortfolioHistoryFromSupabase(portfolioId: Int, onComplete: (String?) -> Unit) {
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            log(LogLevel.ERROR, "supabase", "User not authenticated. Cannot load portfolio history.")
            onComplete(null)
            return
        }

        val fileName = "portfolio_${portfolioId}.json"
        val pathInBucket = "smartinvestorscharts/$userId/$portfolioId/$fileName" // Folder structure
        val bucket = client.storage["smartinvestorscharts"]

        try {
            // 🔹 Start the download
            val byteArray = bucket.downloadPublic(pathInBucket)

            // 🔹 Convert to JSON string
            val jsonString = byteArray.decodeToString()

            // ✅ Return it to the callback
            onComplete(jsonString)

        } catch (e: Exception) {
            log(LogLevel.ERROR, "SupabaseDownload", "Failed to download portfolio $portfolioId: ${e.message}")
            onComplete(null)
        }
    }


    /*
    * function calls that removes a portfolio from supabase
    */
    private fun deletePortfolioFromFirestore(portfolioId: Int) {
        coroutineScope.launch {
            client.from("portfolios").delete {
                filter {
                    eq("id", portfolioId)
                }
            }
        }
    }


        /*
        * Function that saves assets added to a portoflio in supabase
        */

    fun saveAssetstoSupabase() {
        log(LogLevel.DEBUG,"SupabaseDebug", "saveAssetstoSupabase() is being called.")
        coroutineScope.launch {
            val allAssets = assetDao.getAllAssets() // ✅ Retrieve all assets

            if (allAssets.isEmpty()) {
                log(LogLevel.DEBUG,"SupabaseSync", "No assets to save.")
                return@launch
            }

            try {
                client.from("assets").upsert(allAssets)
                log(LogLevel.DEBUG,"SupabaseSync", "Assets saved successfully to Supabase.")
            } catch (e: Exception) {
                log(LogLevel.ERROR,"SupabaseSync", "Error saving assets to Supabase: ${e.message}")
            }
        }
    }


    private fun generateSymbolFromType(type: String): String {
        return when (type) {
            "Stock" -> "stock"
            "Crypto" -> "crypto"
            "Forex" -> "forex"
            else -> "UNKNOWN_${timestampMillis}"
        }
    }

    /*
    * Function calls that removes a portfolio
    */
    fun deletePortfolio(portfolioId: Int) {

        coroutineScope.launch {
            try {
                // Delete the portfolio from the local Room database
                portfolioDao.deletePortfolio(portfolioId)
                log(LogLevel.DEBUG,"PortfolioViewModel", "Portfolio with ID=$portfolioId deleted successfully.")

                // Delete the portfolio from Firestore
                deletePortfolioFromFirestore(portfolioId)
                //saveOrDeletePortfolioHistoryInS3(portfolioId, context, delete = true)
            } catch (e: Exception) {
                log(LogLevel.ERROR,"PortfolioViewModel", "Error deleting portfolio with ID=$portfolioId: ${e.message}")
            }
        }

    }

    /*
     * Function to update an existing portfolio with new assets.
     * When a new assets is purchase the function updates the number of share in the portfolio
     * When new shares are added the total value of the portfolio is updated by the shares being multiple by the current price of the asset.
     */
    fun updatePortfolio(portfolioId: Int, sharesPurchased: Double, price: Double) {

        coroutineScope.launch {
            try {
                val existingPortfolio = portfolioDao.getPortfolioByIdSync(portfolioId)
                log(LogLevel.DEBUG,"PortfolioUpdate", "Existing Portfolio Before Update: $existingPortfolio")

                if (existingPortfolio != null) {
                    // Get all assets in the portfolio
                    assetDao.getAssetsByPortfolio(portfolioId)

                    // Update total shares and total value in the portfolio
                    val totalShares = existingPortfolio.shares + sharesPurchased
                    val totalValue = existingPortfolio.total + (totalShares * price) + existingPortfolio.balance

                    val updatedPortfolio = existingPortfolio.copy(
                        shares = totalShares,
                        balance = existingPortfolio.balance - (sharesPurchased * price),
                        total = totalValue
                    )
                    log(LogLevel.DEBUG,"PortfolioUpdate", "Before Insert: Portfolio(id=${existingPortfolio.id}, balance=${existingPortfolio.balance}, shares=${existingPortfolio.shares}, total=${existingPortfolio.total})")

                    portfolioDao.insertPortfolio(updatedPortfolio)
                    log(LogLevel.DEBUG,"PortfolioUpdate", "After Insert: Portfolio(id=${updatedPortfolio.id}, balance=${updatedPortfolio.balance}, shares=${updatedPortfolio.shares}, total=${updatedPortfolio.total})")

                    savePortfolioToFirebase()

                }
            } catch (e: Exception) {
                log(LogLevel.ERROR,"PortfolioUpdate", "Error updating portfolio: ${e.message}")
            }
        }

    }

    /*
    * This function inserts/updates purchases assets into the asset table*/
    suspend fun insertAsset(asset: Asset) = withContext(Dispatchers.IO) {
        assetInsertMutex.withLock {
            log(LogLevel.DEBUG, "DatabaseDebug", "Inserting Asset: $asset")
            assetDao.insertAssetBlocking(asset)
            log(LogLevel.DEBUG, "DatabaseDebug", "Asset inserted successfully")

            delay(100) // Allow DB commit to complete

            val updatedAssets = assetDao.getAssetsByPortfolio(asset.portfolioId)
            log(LogLevel.DEBUG, "DatabaseDebug", "Updated assets after insert: $updatedAssets")
            saveAssetstoSupabase()
        }
    }


    /*
    * This functions removes sold assets from the asset table
    */
    fun deleteAsset(asset: Asset) {
        coroutineScope.launch {
            assetDao.deleteAsset(asset)  // Calls the delete function in the AssetDao
        }

    }

    /*
    * This function is used to retrieve a specific asset from the portfolio that holds it.
    * This is done because the sale feature only allows portfolios that have to asset to be selected.
    * When the user choices that portfolio and sale the asset, we only only want it sold from that portfolio.*/
    suspend fun getAssetByPortfolioAndSymbolSync(portfolioId: Int, symbol: String): Asset? {
        return assetDao.getAssetByPortfolioAndSymbolSync(portfolioId, symbol)
    }

    /* Function to get assets by portfolio ID
     * ViewModel code to fetch assets
     **/
    suspend fun getAssetsByPortfolio(portfolioId: Int): List<Asset> {
        val assets = assetDao.getAssetsByPortfolio(portfolioId)
        log(LogLevel.DEBUG,"getassets", "Assets retrieved from DB: $assets") // ✅ Log retrieved assets
        return assets
    }

    /*
    * Debug only code to log all assets and ensure they are in the table
    */
    fun logAllAssets() {
        coroutineScope.launch {
            val allAssets = assetDao.getAllAssets()
            allAssets.forEach { asset ->
                log(LogLevel.DEBUG,"AssetTable", "Asset: $asset")
            }
        }
    }

    fun getAssetByPortfolioAndSymbol(portfolioId: Int, symbol: String): Flow<Asset?> = flow {
        val asset = assetDao.getAssetByPortfolioAndSymbolSync(portfolioId, symbol)
        emit(asset)
    }

    /*
    * Function that will be called when a user logs out.
    * Goal is to clear user's portfolio and asset table.
    * This way when a new user logs in the old users data will not rollover
    */
    fun clearPortfolioAndAssets() {
        coroutineScope.launch(Dispatchers.IO) {
            portfolioDao.clearAllPortfolios()
            assetDao.clearAllAssets()
        }
    }

    /*
    * Function called when the user logins in
    * This function will retrieve their portfolios from supabase and load them
    */
    suspend fun loadPortfoliosFromSupabase(onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val userId = client.auth.currentUserOrNull()?.id
                log(LogLevel.DEBUG,"DebugCheck", "Checking user authentication")
                if (userId == null) {
                    log(LogLevel.ERROR,"SupabaseSync", "User not authenticated")
                    onComplete()
                    return@withContext
                }

                log(LogLevel.DEBUG,"DebugCheck", "Fetching portfolios for user: $userId from Supabase")

                val portfoliosFromSupabase: List<Portfolio> = client.postgrest.from("portfolios").select(columns = Columns.list("userId","id, asset, name, shares, balance, total"))
                {
                    filter {
                        eq("userId", userId)
                    }

                }
                    .decodeList()

                log(LogLevel.DEBUG,"DebugCheck", "Fetched ${portfoliosFromSupabase.size} portfolios")

                if (portfoliosFromSupabase.isEmpty()) {
                    log(LogLevel.DEBUG,"SupabaseSync", "No portfolios found for user: $userId")
                    onComplete()
                    return@withContext
                }

                val processedPortfolioIds = mutableSetOf<Int>()

                val portfoliosToInsert = portfoliosFromSupabase.map { portfolioFromSupabase ->
                    val id = portfolioFromSupabase.id

                    if (processedPortfolioIds.contains(id)) {
                        log(LogLevel.DEBUG,"PortfolioDebug", "Skipping already processed Portfolio ID=$id")
                        return@map null
                    }
                    processedPortfolioIds.add(id)

                    log(LogLevel.DEBUG,"PortfolioDebug", "Processing Portfolio ID=$id")

                    val existingPortfolio = portfolioDao.getPortfolioByIdSync(id)
                    existingPortfolio?.copy(balance = portfolioFromSupabase.balance) ?: portfolioFromSupabase
                }.filterNotNull()

                log(LogLevel.DEBUG,"DebugCheck", "Portfolios to insert: ${portfoliosToInsert.size}")

                portfolioDao.insertAll(portfoliosToInsert)
                log(LogLevel.DEBUG,"SupabaseSync", "Portfolios merged and saved into Room.")

                CoroutineScope(Dispatchers.IO).launch {
                    portfoliosToInsert.forEach { portfolio ->
                        log(LogLevel.DEBUG,"AssetDebug", "Calling loadAssetsForPortfolio for Portfolio ID=${portfolio.id}")
                        loadAssetsForPortfolio(portfolio)

                        // 🔹 Load portfolio history from S3 after inserting the portfolio
                        loadPortfolioHistoryFromSupabase(portfolio.id) { jsonData ->
                            if (jsonData != null) {
                                log(LogLevel.DEBUG,"S3Download", "Loaded history for Portfolio ID=${portfolio.id}")
                                // 🔹 Optionally, parse JSON and store it in the database if needed
                            } else {
                                log(LogLevel.ERROR,"S3Download", "Failed to load portfolio history for Portfolio ID=${portfolio.id}")
                            }
                        }
                    }
                    onComplete()
                }
            } catch (e: Exception) {
                log(LogLevel.ERROR,"SupabaseSync", "Failed to load portfolios: ${e.message}")
                onComplete()
            }
        }
    }

    /*
    * Function called when the user logins in
    * This function will retrieve their assets from supabase and load them into their portfolio
    */
    private suspend fun loadAssetsForPortfolio(portfolio: Portfolio) {
        withContext(Dispatchers.IO) {
            try {
                val userId = client.auth.currentUserOrNull()?.id
                log(LogLevel.DEBUG,"AssetLoad", "Fetching assets for Portfolio ID=${portfolio.id}")

                if (userId == null || userId != portfolio.userId) {
                    log(LogLevel.ERROR,"AssetLoad", "Unauthorized or user not authenticated")
                    return@withContext
                }

                val assetsFromSupabase: List<Asset> = client.postgrest.from("assets").select(columns = Columns.list("portfolioId","symbol, company_name, shares, purchasePrice, currentPrice,stopLoss,stopLossSell,asset"))
                {
                    filter {
                        eq("portfolioId", portfolio.id)
                    }

                }
                    .decodeList()

                log(LogLevel.DEBUG,"AssetLoad", "Fetched ${assetsFromSupabase.size} assets for Portfolio ID=${portfolio.id}")

                if (assetsFromSupabase.isEmpty()) {
                    log(LogLevel.DEBUG,"AssetLoad", "No assets found for Portfolio ID=${portfolio.id}")
                    return@withContext
                }

                log(LogLevel.DEBUG,"AssetLoad", "Inserting assets into Room: $assetsFromSupabase")
                assetDao.insertAll(assetsFromSupabase)
                log(LogLevel.DEBUG,"DebugCheck", "Inserted assets into Room for Portfolio ID=${portfolio.id}")

            } catch (e: Exception) {
                log(LogLevel.ERROR,"AssetLoad", "Failed to load assets for Portfolio ID=${portfolio.id}: ${e.message}")
            }
        }
    }

    /*
    * Function call that manages the stop loss set by the user during asset purchase
    * As the price of the asset rises the stop loss wil rise to maintain a set distance
    * when the stop loss is reached the asset will be sold, removed from the table, and the portfolio balance updated.
    */
    fun stopLossPoint(asset: Asset): Double {
        if (asset.stopLoss == 0.0) {
            // No stop loss set, return 0 to indicate no action
            return 0.0
        } else if (asset.stopLoss > 0 && asset.stopLossSell == asset.currentPrice) {
            // Stop loss condition met, sell the asset
            val sharesToSell = asset.shares
            val amountFromSale = sharesToSell * asset.currentPrice

            // Update the portfolio's balance after the sale
            coroutineScope.launch {
                updatePortfolioBalanceforAssets(asset.portfolioId, amountFromSale)
                deleteAsset(asset)  // Remove the asset since the stop loss condition was triggered
            }

            // Return 0 to indicate the asset has been sold
            return 0.0
        } else {
            // Adjust stopLossSell based on the current price
            return asset.currentPrice - asset.stopLoss
        }
    }



}




