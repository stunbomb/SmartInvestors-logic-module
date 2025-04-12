package com.dblhargrove.logic

import android.content.Context
import java.io.File
lateinit var appContext: Context

actual fun writeFile(filename: String, content: String): Boolean {
    return try {
        val file = File(appContext.filesDir, filename)
        file.writeText(content)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual fun readFile(filename: String): String? {
    return try {
        val file = File(appContext.filesDir, filename)
        if (file.exists()) file.readText() else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
