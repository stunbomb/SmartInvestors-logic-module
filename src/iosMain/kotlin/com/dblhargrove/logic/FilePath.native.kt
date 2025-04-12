package com.dblhargrove.logic

import platform.Foundation.*


actual fun writeFile(filename: String, content: String): Boolean {
    val dir = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    ).firstOrNull() as? String ?: return false

    val path = "$dir/$filename"
    return NSString.create(string = content).writeToFile(path, atomically = true)
}

actual fun readFile(filename: String): String? {
    val dir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        .firstOrNull() as? String ?: return null
    val path = "$dir/$filename"
    return NSString.stringWithContentsOfFile(path) as? String
}