package com.dblhargrove.logic.database.User

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MUser(
    val email: String,

    val password: String,

    @SerialName("created_at")
    val created_at: String?=null,

)