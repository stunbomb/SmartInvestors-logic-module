package com.dblhargrove.logic.Assetlogic

import com.dblhargrove.logic.database.DatabaseProvider
import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun loadFinancialData(
    symbol: String,
    tableType: String,
    timeFrame: String,
    financialData: MutableList<Map<String, String>>
) {
    val db = DatabaseProvider.getDatabase()
    withContext(Dispatchers.IO) {
        try {
            val company = db.companyDao().getCompanyBySymbol(symbol)
            val jsonUrl = if (timeFrame == "Annual") company?.annual else company?.quarter

            if (jsonUrl.isNullOrBlank()) {
                log(LogLevel.ERROR,"loadFinancialData", "No URL found for $symbol in $timeFrame")
                return@withContext
            }

            log(LogLevel.DEBUG,"loadFinancialData", "Fetching data from $jsonUrl")

            // Fetch JSON from S3
            val client = HttpClient()
            val response: HttpResponse? = jsonUrl.let { client.get(it) }

            if (response != null) {
                if (response.status != HttpStatusCode.OK) {
                    log(LogLevel.ERROR,"loadFinancialData", "Failed to fetch JSON from S3 for $symbol")
                    return@withContext
                }
            }

            val jsonString = response?.body<String>()
            log(LogLevel.DEBUG,"loadFinancialData", "Extracted: jsonString=${jsonString}")
            val jsonElement = jsonString?.let { Json.parseToJsonElement(it) }
            log(LogLevel.DEBUG,"loadFinancialData", "Extracted: jsonElement=${jsonElement}")
            val financialDataList = jsonElement?.jsonObject?.get(tableType.lowercase().replace(" ", "_"))?.jsonArray
            log(LogLevel.DEBUG,"loadFinancialData", "Extracted: financialDataList=${financialDataList}")

            financialData.clear()

            financialDataList?.forEach { yearEntry ->
                log(LogLevel.DEBUG,"loadFinancialData", "Extracted: calendarYear=${yearEntry.jsonObject["calendarYear"]}, period=${yearEntry.jsonObject["period"]}")

                val dataObject = yearEntry.jsonObject["data"]?.jsonObject
                val year = dataObject?.get("calendarYear")?.jsonPrimitive?.contentOrNull ?: "N/A"
                val period = dataObject?.get("period")?.jsonPrimitive?.contentOrNull ?: "N/A"
                val columnKey = if (timeFrame == "Annual") year else "${year}_$period"

                val data = yearEntry.jsonObject["data"]?.jsonObject ?: return@forEach

                val rowData = mutableMapOf<String, String>()
                rowData["Year"] = columnKey

                data.forEach { (key, value) ->
                    if (key !in setOf("reportedCurrency", "cik", "fillingDate", "acceptedDate", "calendarYear", "period")) {
                        rowData[key] = value.toString().replace("\"", "")
                    }
                }

                financialData.add(rowData)
            }

            log(LogLevel.DEBUG,"loadFinancialData", "Fetched ${financialData.size} financial records for $symbol")

        } catch (e: Exception) {
            log(LogLevel.ERROR,"loadFinancialData", "Error loading data: ${e.message}")
        }
    }
}

suspend fun loadCFFinancialData(
    symbol: String,
    asset: String,
    financialData: MutableList<Map<String, String>>
) {
    val db = DatabaseProvider.getDatabase()
    withContext(Dispatchers.IO) {
        try {
            // Check if asset is either CRYPTO or FOREX
            if (asset !in setOf("crypto", "forex")) {
                log(LogLevel.ERROR,"loadCFFinancialData", "Invalid asset type: $asset")
                return@withContext
            }

            // Fetch the correct URL from Room Database
            val jsonUrl = when (asset) {
                "crypto" -> {
                    val cryptoData = db.cryptoDao().getCryptoBySymbol(symbol)
                    log(LogLevel.DEBUG,"loadCFFinancialData", "Crypto Data: $cryptoData")
                    cryptoData?.quote
                }
                "forex" -> {
                    val forexData = db.forexDao().getForexBySymbol(symbol)
                    log(LogLevel.DEBUG,"loadCFFinancialData", "Forex Data: $forexData")
                    forexData?.quote
                }
                else -> null
            }

            // Log what was retrieved from the database
            log(LogLevel.DEBUG,"loadCFFinancialData", "Retrieved JSON URL for $symbol ($asset): $jsonUrl")

            if (jsonUrl.isNullOrBlank()) {
                log(LogLevel.ERROR,"loadCFFinancialData", "No JSON URL found for $symbol ($asset)")
                return@withContext
            }

            log(LogLevel.DEBUG,"loadCFFinancialData", "Fetching data from $jsonUrl")

            // Fetch JSON from S3
            val client = HttpClient(CIO)
            val response: HttpResponse? = client.get(jsonUrl)

            if (response != null && response.status != HttpStatusCode.OK) {
                log(LogLevel.ERROR,"loadCFFinancialData", "Failed to fetch JSON from S3 for $symbol")
                return@withContext
            }

            val jsonString = response?.body<String>()
            log(LogLevel.DEBUG,"loadCFFinancialData", "Extracted: jsonString=${jsonString}")

            val jsonElement = jsonString?.let { Json.parseToJsonElement(it) }
            log(LogLevel.DEBUG,"loadCFFinancialData", "Extracted: jsonElement=${jsonElement}")
            val name = jsonElement?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: "N/A"


            val dataObject = jsonElement?.jsonObject
            if (dataObject == null) {
                log(LogLevel.ERROR,"loadCFFinancialData", "Failed to parse JSON object")
                return@withContext
            }

            financialData.clear()

            // Prepare data row
            val rowData = mutableMapOf<String, String>()
            rowData["Name"] = name

            dataObject.forEach { (key, value) ->
                if (key !in setOf("symbol", "name", "exchange", "timestamp", "eps", "pe", "earningsAnnouncement", "sharesOutstanding")) {
                    rowData[key] = value.jsonPrimitive.contentOrNull ?: "N/A"
                }
            }

            financialData.add(rowData)

            log(LogLevel.DEBUG,"loadCFFinancialData", "Fetched ${financialData.size} financial records for $symbol ($asset)")

        } catch (e: Exception) {
            log(LogLevel.ERROR,"loadCFFinancialData", "Error loading data: ${e.message}")
        }
    }
}
