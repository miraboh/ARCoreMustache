package com.myraboh.arcoremustache.base

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


/**
 * Created by Myraboh on 12/14/22.
 */
@HiltAndroidApp
class MustacheApp: Application(){
    override fun onCreate() {
        super.onCreate()
    }
}