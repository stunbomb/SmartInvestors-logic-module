package com.dblhargrove.util

enum class LogLevel { DEBUG, ERROR }

expect fun log(level: LogLevel, tag: String, message: String)
