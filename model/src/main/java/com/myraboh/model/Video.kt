package com.myraboh.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


/**
 * Created by Myraboh on 12/13/22.
 */
@Parcelize
@Entity
data class Video(
    @PrimaryKey(autoGenerate = false)
    var uri: String = "",
    var duration: String? = null,
    var tag: String? = null
    ): Parcelable
