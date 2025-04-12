package com.dblhargrove.logic.database

import com.dblhargrove.database.AppDatabase

private var database: AppDatabase? = null

actual object DatabaseProvider {
    actual fun initialize() {
        if (database == null) {
            database = getDatabaseBuilder().build()
        }
    }

    actual fun getDatabase(): AppDatabase {
        return database ?: throw IllegalStateException("Database not initialized.")
    }
}
