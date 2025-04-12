package com.dblhargrove.logic.BottomNav

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dblhargrove.database.AppDatabase
import com.dblhargrove.database.Assets.Item
import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

suspend fun loadStocksFromRoom(
    db: AppDatabase,
    stocks: SnapshotStateList<Item>,
    batchSize: Int = 100  // ✅ Adjust batch size as needed
) {
    withContext(Dispatchers.IO) {
        try {
            val companyDao = db.companyDao()
            var offset = 0

            while (true) {
                val companies = companyDao.getCompaniesBatch(batchSize, offset)  // ✅ Fetch in chunks

                if (companies.isEmpty()) break  // ✅ Stop when no more records

                val items = companies.map { company ->
                    Item(company.name, company.symbol, company.price.toString(), company.asset, company.dividend, company.logo, company.cumulativeReturns)
                }

                withContext(Dispatchers.Main) {  // ✅ Update UI in chunks
                    stocks.addAll(items)
                }

                offset += batchSize  // ✅ Move to next batch
                delay(100)  // ✅ Small delay to prevent blocking
            }

        } catch (e: CancellationException) {
            log(LogLevel.ERROR,"loadStocksFromRoom","error ${e.message}")
            // Ignore this exception, it's normal when switching
        } catch (e: Exception) {
            log(LogLevel.ERROR,"loadStocksFromRoom", "Error loading companies: ${e.message}")
        }
    }
}


suspend fun loadCryptosFromRoom(db: AppDatabase, stocks: SnapshotStateList<Item>) {
    withContext(Dispatchers.IO) {
        try {
            val cryptoDao = db.cryptoDao()
            val cryptos = cryptoDao.getAllCryptos()
            val items = cryptos.map { Item(it.name, it.symbol, it.price.toString(), it.asset, it.dividend, it.logo, it.cumulativeReturns) }
            stocks.addAll(items)
        } catch (e: CancellationException) {
            // Ignore this exception, it's normal when switching
        }catch (e: Exception) {
            log(LogLevel.ERROR,"loadCryptosFromRoom", "Error loading cryptos: ${e.message}")
        }
    }
}

suspend fun loadForexFromRoom(db: AppDatabase, stocks: SnapshotStateList<Item>) {
    withContext(Dispatchers.IO) {
        try {
            val forexDao = db.forexDao()
            val forexs = forexDao.getAllForex()
            val items = forexs.map { Item(it.name, it.symbol, it.price.toString(), it.asset, it.dividend, it.logo, it.cumulativeReturns) }
            stocks.addAll(items)
        } catch (e: CancellationException) {
            // Ignore this exception, it's normal when switching
        }catch (e: Exception) {
            log(LogLevel.ERROR,"loadForexFromRoom", "Error loading forexs: ${e.message}")
        }
    }
}