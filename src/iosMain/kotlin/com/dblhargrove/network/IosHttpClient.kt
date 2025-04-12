package com.dblhargrove.logic.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

val iosHttpClient = HttpClient(Darwin)
