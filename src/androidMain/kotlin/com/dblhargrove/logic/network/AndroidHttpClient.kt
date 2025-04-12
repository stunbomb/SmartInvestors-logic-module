package com.dblhargrove.logic.network

import com.dblhargrove.network.initHttpClient
import io.ktor.client.engine.okhttp.OkHttp

fun setupHttpClient() {
    initHttpClient(OkHttp.create())
}
