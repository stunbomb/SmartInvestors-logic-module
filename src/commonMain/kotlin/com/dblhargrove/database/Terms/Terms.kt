package com.dblhargrove.database.Terms

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "terms")
@Serializable // Supabase/serialization annotation
data class Terms(
    @PrimaryKey val term: String,
    val Definitions: String
)
