package com.dblhargrove.logic.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.dblhargrove.database.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/finance-database.db"
    return Room.databaseBuilder(
        name = dbFilePath
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val URL = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(URL?.path)
}
