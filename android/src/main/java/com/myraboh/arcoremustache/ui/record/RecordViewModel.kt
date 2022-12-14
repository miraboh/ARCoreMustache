package com.myraboh.arcoremustache.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myraboh.arcoremustache.data.Repository
import com.myraboh.arcoremustache.util.Result
import com.myraboh.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Created by Myraboh on 12/14/22.
 */
@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    private val _getVideo = MutableStateFlow<Result<List<Video>>>(Result.Loading)
    val getVideos: StateFlow<Result<List<Video>>> = _getVideo

    fun observes() {
        viewModelScope.launch {
            repository.getAllVideo().collect {
                _getVideo.value = com.myraboh.arcoremustache.util.Result.Success(it)
            }
        }
    }

    fun insert(video: Video) {
        viewModelScope.launch {
            repository.insertVideo(video)
        }
    }
}