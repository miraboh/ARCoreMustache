package com.myraboh.arcoremustache.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.myraboh.arcoremustache.data.source.local.dao.VideoDao
import com.myraboh.model.Video


/**
 * Created by Myraboh on 12/13/22.
 */
@Database(
    entities = [Video::class],
    exportSchema = true,
    version = 1
)
abstract class VideoDatabase: RoomDatabase() {
    abstract fun getVideos(): VideoDao
}