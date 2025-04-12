package com.dblhargrove.database.Videos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dblhargrove.database.Videos.Videos

@Dao
interface VideosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<Videos>)

    @Query("SELECT * FROM videos WHERE category = :category AND list = :list")
    fun getVideosByCategoryAndList(category: String, list: String): List<Videos>

    @Query("DELETE FROM videos WHERE vid NOT IN (:vid)")
    suspend fun deleteAbsentVideos(vid: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllBlocking(videos: List<Videos>) // Create non-suspend version

    @Query("DELETE FROM videos WHERE vid NOT IN (:vid)")
    fun deleteAbsentVideosBlocking(vid: List<String>)
}