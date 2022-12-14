package com.myraboh.arcoremustache.ui

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.myraboh.arcoremustache.common.SnackbarHelper
import com.myraboh.arcoremustache.databinding.ActivityVideoBinding
import com.myraboh.arcoremustache.helper.CameraPermissionHelper
import com.myraboh.arcoremustache.helper.DisplayRotationHelper
import com.myraboh.arcoremustache.helper.FullScreenHelper
import com.myraboh.arcoremustache.helper.TrackingStateHelper
import com.myraboh.arcoremustache.rendering.AugmentedFaceRenderer
import com.myraboh.arcoremustache.rendering.BackgroundRenderer
import com.myraboh.arcoremustache.ui.dialog.TagDialog
import com.myraboh.arcoremustache.ui.record.RecordingActivity
import com.myraboh.model.AppState
import com.myraboh.model.Mode
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@AndroidEntryPoint
class VideoActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private lateinit var binding: ActivityVideoBinding

    private var appState: AppState = AppState.Idle

    private var mode: Mode = Mode.FIRST

    private var session: Session? = null

    private var installRequested = false

    private var surfaceView: GLSurfaceView? = null

    var uri: String? = null

    private val MP4_VIDEO_MIME_TYPE = "video/mp4"
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1

    private val messageSnackbarHelper: SnackbarHelper = SnackbarHelper()
    private var displayRotationHelper: DisplayRotationHelper? = null
    private val trackingStateHelper: TrackingStateHelper = TrackingStateHelper(this)

    private val backgroundRenderer: BackgroundRenderer = BackgroundRenderer()

    private val augmentedFaceRenderer1: AugmentedFaceRenderer = AugmentedFaceRenderer()
    private val augmentedFaceRenderer2: AugmentedFaceRenderer = AugmentedFaceRenderer()
    private val augmentedFaceRenderer3: AugmentedFaceRenderer = AugmentedFaceRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayRotationHelper = DisplayRotationHelper( /*context=*/this)

        surfaceView = binding.surfaceview

        // Set up renderer.
        surfaceView!!.preserveEGLContextOnPause = true
        surfaceView!!.setEGLContextClientVersion(2)
        surfaceView!!.setEGLConfigChooser(8, 8, 8, 8, 16, 0) // Alpha used for plane blending.

        surfaceView!!.setRenderer(this)
        surfaceView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        surfaceView!!.setWillNotDraw(false)

        installRequested = false

        binding.mustache1.setOnClickListener {
            mode = Mode.FIRST
        }

        binding.mustache2.setOnClickListener {
            mode = Mode.SECOND
        }

        binding.mustache3.setOnClickListener {
            mode = Mode.THIRD
        }
    }

    override fun onResume() {
        super.onResume()

        if (session == null) {
            var exception: Exception? = null
            var message: String? = null
            try {
                val installStatus = ArCoreApk.getInstance()
                    .requestInstall(this, !installRequested)
                when (installStatus) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {
                    }
                    else -> {
                        println("Undefined installed status")
                    }
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this)
                    return
                }

                // Create the session and configure it to use a front-facing (selfie) camera.

                // Create the session and configure it to use a front-facing (selfie) camera.
                session = Session( /* context= */this,
                    EnumSet.noneOf(Session.Feature::class.java)
                )
                val cameraConfigFilter = CameraConfigFilter(session)
                cameraConfigFilter.setFacingDirection(CameraConfig.FacingDirection.FRONT)
                val cameraConfigs = session!!.getSupportedCameraConfigs(cameraConfigFilter)
                if (!cameraConfigs.isEmpty()) {
                    // Element 0 contains the camera config that best matches the session feature
                    // and filter settings.
                    session!!.setCameraConfig(cameraConfigs[0])
                } else {
                    message = "This device does not have a front-facing (selfie) camera"
                    exception = UnavailableDeviceNotCompatibleException(message)
                }
                configureSession()

            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
            }
            if (message != null) {
                messageSnackbarHelper.showError(this, message)
                println("Exception creating session:$exception")
                return
            }
        }
        // Note that order matters - see the note in onPause(), the reverse applies here.

        try {
            session?.resume()
        } catch (e: CameraNotAvailableException) {
            messageSnackbarHelper.showError(
                this,
                "Camera not available. Try restarting the app."
            )
            session = null
            return
        }

        surfaceView?.onResume()
        displayRotationHelper!!.onResume()

    }

    // Update the "Record" button based on app's internal state.
    private fun updateRecordButton() {
        val buttonViewRecord = binding.startRecordingButton
        val buttonViewRecorded = binding.recordedButton
        when (appState) {
            AppState.Idle -> {
                buttonViewRecord.text = "Start Recording"
                buttonViewRecord.setTextColor(Color.parseColor("#5eeded"))
                buttonViewRecord.visibility = View.VISIBLE
                buttonViewRecorded.visibility = View.VISIBLE
            }

            AppState.Recording -> {
                buttonViewRecord.text = "Stop Recording"
                buttonViewRecord.setTextColor(Color.RED)
                buttonViewRecord.visibility = View.VISIBLE
                buttonViewRecorded.visibility = View.GONE
            }
        }
    }

    // Handle the "Record" button click event.
    fun onClickRecord(view: View?) {
        Log.d(TAG, "onClickRecord")
        when (appState) {
            AppState.Idle -> {
                val hasStarted: Boolean = startRecording()
                Log.d(TAG, String.format("onClickRecord start: hasStarted %b", hasStarted))
                if (hasStarted) appState = AppState.Recording
            }
            AppState.Recording -> {
                val bundle = Bundle()
                bundle.putString("videoUri", uri)
                TagDialog().apply {
                    this.arguments = bundle
                    this.show(supportFragmentManager, "TagDialod")
                    saveRecording()
                }
            }
        }
        updateRecordButton()
    }

    private fun startRecording(): Boolean {
        val mp4FileUri: Uri = createMp4File() ?: return false
        Log.d(TAG, "startRecording at: $mp4FileUri")
        pauseARCoreSession()

        // Configure the ARCore session to start recording.
        val recordingConfig = RecordingConfig(session)
            .setMp4DatasetUri(mp4FileUri)
            .setAutoStopOnPause(true)
        try {
            // Prepare the session for recording, but do not start recording yet.
            session!!.startRecording(recordingConfig)
        } catch (e: RecordingFailedException) {
            Log.e(TAG, "startRecording - Failed to prepare to start recording", e)
            return false
        }
        val canResume: Boolean = resumeARCoreSession()
        if (!canResume) return false

        // Correctness checking: check the ARCore session's RecordingState.
        val recordingStatus = session!!.recordingStatus
        Log.d(TAG, String.format("startRecording - recordingStatus %s", recordingStatus))
        return recordingStatus == RecordingStatus.OK
    }

    private fun pauseARCoreSession() {
        // Pause the GLSurfaceView so that it doesn't update the ARCore session.
        // Pause the ARCore session so that we can update its configuration.
        // If the GLSurfaceView is not paused,
        //   onDrawFrame() will try to update the ARCore session
        //   while it's paused, resulting in a crash.
        surfaceView!!.onPause()
        session!!.pause()
    }

    private fun resumeARCoreSession(): Boolean {
        // We must resume the ARCore session before the GLSurfaceView.
        // Otherwise, the GLSurfaceView will try to update the ARCore session.
        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "CameraNotAvailableException in resumeARCoreSession", e)
            return false
        }
        surfaceView!!.onResume()
        return true
    }


    private fun stopRecording(): Boolean {
        try {
            session!!.stopRecording()
        } catch (e: RecordingFailedException) {
            Log.e(TAG, "stopRecording - Failed to stop recording", e)
            return false
        }

        // Correctness checking: check if the session stopped recording.
        return session!!.recordingStatus == RecordingStatus.NONE
    }

    fun saveRecording(){
        val hasStopped: Boolean = stopRecording()
        Log.d(TAG, String.format("onClickRecord stop: hasStopped %b", hasStopped))
        if (hasStopped) appState = AppState.Idle
        updateRecordButton()
    }

    private fun createMp4File(): Uri? {
        // Since we use legacy external storage for Android 10,
        // we still need to request for storage permission on Android 10.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (!checkAndRequestStoragePermission()) {
                Log.i(TAG, String.format("Didn't createMp4File. No storage permission, API Level = %d", Build.VERSION.SDK_INT));
                return null
            }
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val mp4FileName = "com.myraboh.arcoremustache" + dateFormat.format(Date()).toString() + ".mp4"
        uri = mp4FileName
        val resolver = this.contentResolver
        var videoCollection: Uri? = null
        videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        // Create a new Media file record.
        val newMp4FileDetails = ContentValues()
        newMp4FileDetails.put(MediaStore.Video.Media.DISPLAY_NAME, mp4FileName)
        newMp4FileDetails.put(MediaStore.Video.Media.MIME_TYPE, MP4_VIDEO_MIME_TYPE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // The Relative_Path column is only available since API Level 29.
            newMp4FileDetails.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        } else {
            // Use the Data column to set path for API Level <= 28.
            val mp4FileDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val absoluteMp4FilePath: String = File(mp4FileDir, mp4FileName).getAbsolutePath()
            newMp4FileDetails.put(MediaStore.Video.Media.DATA, absoluteMp4FilePath)
        }
        val newMp4FileUri = resolver.insert(videoCollection, newMp4FileDetails)
        // Ensure that this file exists and can be written.
        if (newMp4FileUri == null) {
            Log.e(TAG, java.lang.String.format("Failed to insert Video entity in MediaStore. API Level = %d",
                    Build.VERSION.SDK_INT
                )
            )
            return null
        }

        // This call ensures the file exist before we pass it to the ARCore API.
        if (!testFileWriteAccess(newMp4FileUri)) {
            return null
        }
        Log.d(
            TAG,
            java.lang.String.format(
                "createMp4File = %s, API Level = %d",
                newMp4FileUri,
                Build.VERSION.SDK_INT
            )
        )
        return newMp4FileUri
    }

    // Test if the file represented by the content Uri can be open with write access.
    private fun testFileWriteAccess(contentUri: Uri): Boolean {
        try {
            this.contentResolver.openOutputStream(contentUri).use { mp4File ->
                Log.d(
                    TAG,
                    String.format("Success in testFileWriteAccess %s", contentUri.toString())
                )
                return true
            }
        } catch (e: FileNotFoundException) {
            Log.e(
                TAG,
                String.format(
                    "FileNotFoundException in testFileWriteAccess %s",
                    contentUri.toString()
                ),
                e
            )
        } catch (e: IOException) {
            Log.e(
                TAG,
                String.format("IOException in testFileWriteAccess %s", contentUri.toString()),
                e
            )
        }
        return false
    }

    // Handle the click event of the "Playback" button.
    fun onClickRecorded(view: View?) {
        startActivity(Intent(this, RecordingActivity::class.java))
    }

    fun checkAndRequestStoragePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            this,
            android.R.style.Theme_Material_Dialog_Alert
        )
        builder
            .setTitle("Camera permission required")
            .setMessage("Add camera permission via Settings?")
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                // If Ok was hit, bring up the Settings app.
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts(
                    "package",
                    this.packageName,
                    null
                )
                this.startActivity(intent)
                // When the user closes the Settings app, allow the app to resume.
                // Allow the app to ask for permissions again now.
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setOnDismissListener {
            }
            .show()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }
    override fun onPause() {
        super.onPause()
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper!!.onPause()
            surfaceView?.onPause()
            session?.pause()
        }
        finish()
    }

    override fun onDestroy() {
        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session?.close()
            session = null
        }
        super.onDestroy()
    }

    private fun configureSession() {
        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        session!!.configure(config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        displayRotationHelper!!.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        Log.d("--------------------", "onSurfaceCreated")
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread( /*context=*/this)

            augmentedFaceRenderer1.createOnGlThread(this, "models/mustache1.png")
            augmentedFaceRenderer1.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)

            augmentedFaceRenderer2.createOnGlThread(this, "models/mustache2.png")
            augmentedFaceRenderer2.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)

            augmentedFaceRenderer3.createOnGlThread(this, "models/mustache3.png")
            augmentedFaceRenderer3.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)

        } catch (e: IOException) {
            Log.e("MAIN ACTIVITY::CLASS", "Failed to read an asset file", e)
        }
    }

    override fun onDrawFrame(p0: GL10?) {
        Log.d("--------------------", "onDrawFrame")
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        session?.let {
            // Notify ARCore session that the view size changed so that the perspective matrix and
            // the video background can be properly adjusted.
            // Notify ARCore session that the view size changed so that the perspective matrix and
            // the video background can be properly adjusted.

            displayRotationHelper!!.updateSessionIfNeeded(it)

            try {
                it.setCameraTextureName(backgroundRenderer.textureId)

                // Obtain the current frame from ARSession. When the configuration is set to
                // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
                // camera framerate.
                val frame: Frame = it.update()
                val camera: Camera = frame.camera

                // Get projection matrix.
                val projectionMatrix = FloatArray(16)
                camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)

                // Get camera matrix and draw.
                val viewMatrix = FloatArray(16)
                camera.getViewMatrix(viewMatrix, 0)

                // Compute lighting from average intensity of the image.
                // The first three components are color scaling factors.
                // The last one is the average pixel intensity in gamma space.
                val colorCorrectionRgba = FloatArray(4)
                frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)

                //handle(frame, camera)
                // If frame is ready, render camera preview image to the GL surface.
                backgroundRenderer.draw(frame)

                // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
                trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)
                val faces: Collection<AugmentedFace> = it.getAllTrackables(AugmentedFace::class.java)

                for (face in faces) {

                    // Face objects use transparency so they must be rendered back to front without depth write.
                    GLES20.glDepthMask(false)

                    // Each face's region poses, mesh vertices, and mesh normals are updated every frame.

                    // 1. Render the face mesh first, behind any 3D objects attached to the face regions.
                    val modelMatrix = FloatArray(16)
                    face.centerPose.toMatrix(modelMatrix, 0)

                    handleClicks(face,projectionMatrix,viewMatrix,modelMatrix,colorCorrectionRgba)

                    if (face.trackingState !== TrackingState.TRACKING) {
                        break
                    }
                }
            } catch (t: Throwable) {
                // Avoid crashing the application due to unhandled exceptions.
                println("Exception on the OpenGL thread $t")
            } finally {
                GLES20.glDepthMask(true)
            }
        }
    }

    private fun handleClicks(face: AugmentedFace, projectionMatrix: FloatArray, viewMatrix: FloatArray, modelMatrix: FloatArray, colorCorrectionRgba: FloatArray){
        when(mode) {
            Mode.FIRST -> augmentedFaceRenderer1.draw(
                projectionMatrix,
                viewMatrix,
                modelMatrix,
                colorCorrectionRgba,
                face
            )
            Mode.SECOND -> augmentedFaceRenderer2.draw(
                projectionMatrix,
                viewMatrix,
                modelMatrix,
                colorCorrectionRgba,
                face
            )
            Mode.THIRD -> augmentedFaceRenderer3.draw(
                projectionMatrix,
                viewMatrix,
                modelMatrix,
                colorCorrectionRgba,
                face
            )
        }
    }
}
