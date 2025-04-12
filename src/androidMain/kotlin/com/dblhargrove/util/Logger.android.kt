package com.dblhargrove.util

import android.util.Log

actual fun log(level: LogLevel, tag: String, message: String) {
    when (level) {
        LogLevel.DEBUG -> Log.d(tag, message)
        LogLevel.ERROR -> Log.e(tag, message)
    }
}
