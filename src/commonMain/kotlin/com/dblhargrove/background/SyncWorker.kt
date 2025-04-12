package com.dblhargrove.background

import com.dblhargrove.portfolio.PortfolioViewModel
import com.dblhargrove.database.AppDatabase
import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log
import kotlinx.coroutines.delay

suspend fun runSync(db: AppDatabase) {
    val portfolioDao = db.portfolioDao()
    val portfolios = portfolioDao.getAllPortfoliosSync()

    portfolios.forEach { portfolio ->
        val total = portfolio.total
        PortfolioViewModel().updatePortfolioHistoryJson(portfolio.id, total)
    }

    delay(100)

    log(LogLevel.DEBUG, "SyncManager", "Portfolio history updated at 11:59 PM")

}
