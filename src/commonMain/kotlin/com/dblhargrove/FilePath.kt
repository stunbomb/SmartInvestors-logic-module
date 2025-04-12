package com.dblhargrove.logic

expect fun writeFile(filename: String, content: String): Boolean

expect fun readFile(filename: String): String?
