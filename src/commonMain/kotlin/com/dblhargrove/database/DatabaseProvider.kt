package com.dblhargrove.logic.database

import androidx.room.RoomDatabase
import com.dblhargrove.database.AppDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

expect object DatabaseProvider {
    fun initialize()
    fun getDatabase(): AppDatabase
}
