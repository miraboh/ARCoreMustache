package com.myraboh.arcoremustache.data

import com.myraboh.arcoremustache.data.source.local.dao.VideoDao
import com.myraboh.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Created by Myraboh on 12/13/22.
 */
interface Repository {
    suspend fun insertVideo(video: Video?)
    fun getAllVideo(): Flow<List<Video>>
}

@Singleton
class RepositoryImpl @Inject constructor(
    private val videoDao: VideoDao
): Repository{

    override suspend fun insertVideo(video: Video?) {
        withContext(Dispatchers.IO){
            if (video == null)
                return@withContext
            videoDao.insertVideo(video)
        }
    }

    override fun getAllVideo(): Flow<List<Video>> = videoDao.getAllVideos()
}