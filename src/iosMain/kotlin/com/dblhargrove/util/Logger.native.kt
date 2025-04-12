package com.dblhargrove.util

import platform.Foundation.NSLog

actual fun log(level: LogLevel, tag: String, message: String) {
    NSLog("$tag [$level]: $message")
}
