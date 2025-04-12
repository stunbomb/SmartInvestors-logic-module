package com.dblhargrove.logic.NavDrawer

import com.dblhargrove.logic.database.DatabaseProvider
import com.dblhargrove.database.Videos.Videos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

val db = DatabaseProvider.getDatabase()

suspend fun fetchVideosFromRoom(category: String, list: String): List<Videos> {
    return withContext(Dispatchers.IO) {
        db.videosDao().getVideosByCategoryAndList(category, list)

    }
}