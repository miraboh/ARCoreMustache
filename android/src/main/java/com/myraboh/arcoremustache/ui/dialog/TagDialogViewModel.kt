package com.myraboh.arcoremustache.ui.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myraboh.arcoremustache.data.Repository
import javax.inject.Inject
import javax.inject.Singleton
import com.myraboh.arcoremustache.util.Result
import com.myraboh.arcoremustache.util.data
import com.myraboh.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * Created by Myraboh on 12/14/22.
 */
@HiltViewModel
class TagDialogViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    /**
     * Make sure you implement checks for if it inserted successfully or not
     */
    fun insert(video: Video) {
        viewModelScope.launch {
            repository.insertVideo(video)
        }
    }
}