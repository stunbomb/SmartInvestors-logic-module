package com.dblhargrove.database.Videos

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity(tableName = "videos")
@Serializable // Supabase/serialization annotation
data class Videos(
    @PrimaryKey val id: Int? = null,
    val vid: String, //Youtube id of the video
    val channel: String, //Youtube channel
    val category: String, // category it falls in
    val title: String, //Video title
    val list: String
)