package com.myraboh.arcoremustache.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.myraboh.arcoremustache.databinding.ActivityMainBinding
import com.myraboh.arcoremustache.helper.CameraPermissionHelper
import com.myraboh.arcoremustache.ui.record.RecordingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVideo.setOnClickListener {
            startActivity(Intent(this, VideoActivity::class.java))
        }

        binding.btnRecording.setOnClickListener {
            startActivity(Intent(this, RecordingActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }
    }
}