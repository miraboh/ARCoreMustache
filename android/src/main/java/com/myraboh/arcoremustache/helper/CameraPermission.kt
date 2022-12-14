package com.myraboh.arcoremustache.helper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


/**
 * Created by Myraboh on 12/12/22.
 */
/** Helper to ask camera permission.  */
object CameraPermissionHelper {
    private const val CAMERA_PERMISSION_CODE = 0
    private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    /** Check to see we have the necessary permissions for this app.  */
    fun hasCameraPermission(activity: Activity?): Boolean {
        return (ContextCompat.checkSelfPermission(activity!!, CAMERA_PERMISSION)
                == PackageManager.PERMISSION_GRANTED)
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't.  */
    fun requestCameraPermission(activity: Activity?) {
        ActivityCompat.requestPermissions(
            activity!!, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE
        )
    }
}