package com.dblhargrove

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform