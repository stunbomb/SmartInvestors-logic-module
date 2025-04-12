package com.dblhargrove.logic.News

import com.dblhargrove.database.AppDatabase
import com.dblhargrove.database.News.News
import com.dblhargrove.util.LogLevel
import com.dblhargrove.util.log

suspend fun loadNewsFromRoom(db: AppDatabase, assetType: String, pageSize: Int, offset: Int): List<News> {
    val news = db.newsDao().getPaginatedNewsByAsset(assetType.lowercase(), pageSize, offset)
    log(LogLevel.DEBUG,"NewsPagination", "Fetched ${news.size} news articles from Room for $assetType with offset $offset")
    return news
}



