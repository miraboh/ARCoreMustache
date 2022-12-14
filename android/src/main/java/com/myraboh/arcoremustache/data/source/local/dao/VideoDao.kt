package com.myraboh.arcoremustache.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.myraboh.model.Video
import kotlinx.coroutines.flow.Flow


/**
 * Created by Myraboh on 12/13/22.
 */
@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video)

    @Query("SELECT * FROM Video")
    fun getAllVideos(): Flow<List<Video>>

}