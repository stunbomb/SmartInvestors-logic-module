package com.dblhargrove.logic.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dblhargrove.database.AppDatabase

@SuppressLint("StaticFieldLeak")
private lateinit var context: Context

fun provideAndroidContext(ctx: Context) {
    context = ctx.applicationContext
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("finance-database.db")
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        dbFile.absolutePath
    )
}
