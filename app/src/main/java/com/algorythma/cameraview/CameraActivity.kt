@file:Suppress("DEPRECATION")

package com.algorythma.cameraview

import android.graphics.Rect
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CameraActivity : AppCompatActivity(), Camera.PictureCallback {

    companion object {
        private val MEDIA_TYPE_IMAGE = 1
        private val MEDIA_TYPE_VIDEO = 2
        private fun getOutputMediaFile(type: Int): File? {
            val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraView")
            if (!mediaStorageDir.exists()) mediaStorageDir.mkdir()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            return when (type) {
                MEDIA_TYPE_IMAGE -> File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
                MEDIA_TYPE_VIDEO -> File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
                else -> null
            }
        }

        fun getCameraInstance(): Camera {
            var c: Camera? = null
            try {
                c = Camera.open()
            } catch (e: Exception) {
            }
            return c!!
        }
    }

    private lateinit var mMediaRecorder: MediaRecorder
    private lateinit var mCamera: Camera
    private lateinit var cameraView: CameraView
    private var isRecording = false
    private var isVideo = false
    private var isTimeLapse = false

    private fun prepareVideoRecorder(): Boolean {
        mCamera = getCameraInstance()
        mMediaRecorder = MediaRecorder()
        mCamera.unlock()
        mMediaRecorder.setCamera(mCamera)
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mMediaRecorder.setProfile(CamcorderProfile.get(if (isTimeLapse) CamcorderProfile.QUALITY_TIME_LAPSE_HIGH else CamcorderProfile.QUALITY_HIGH))
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO)?.path)
        mMediaRecorder.setPreviewDisplay(cameraView.holder.surface)
        if (isTimeLapse) mMediaRecorder.setCaptureRate(0.1)
        try {
            mMediaRecorder.prepare()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        val pictureFile: File? = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: return
        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
        } catch (e: Exception) {
        }
    }

    private fun releaseMediaRecorder() {
        mMediaRecorder.reset()
        mMediaRecorder.release()
        mCamera.lock()
    }

    override fun onPause() {
        super.onPause()
        releaseMediaRecorder()
        mCamera.release()
    }

    private fun whenVideo() {
        when {
            isRecording -> {
                mMediaRecorder.stop()
                releaseMediaRecorder()
                mCamera.lock()
                button_capture.setText(R.string.capture)
                isRecording = false
            }
            else -> {
                if (prepareVideoRecorder()) {
                    mMediaRecorder.start()
                    button_capture.setText(R.string.stop)
                    isRecording = true
                } else releaseMediaRecorder()
            }
        }
    }

    private fun whenCamera() {
        if (mCamera.parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) mCamera.parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        if (mCamera.parameters.maxNumMeteringAreas > 0) {
            val meteringAreas = ArrayList<Camera.Area>()
            meteringAreas.add(Camera.Area(Rect(-100, -100, 100, 100), 600))
            meteringAreas.add(Camera.Area(Rect(800, -1000, 1000, -800), 400))
            mCamera.parameters.meteringAreas = meteringAreas
            mCamera.setFaceDetectionListener(MyFaceDetectionListener())
        }
        mCamera.takePicture(null, null, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        mCamera = getCameraInstance()
        cameraView = CameraView(this, mCamera)
        camera_preview.addView(cameraView)
        button_capture.setOnClickListener {
            if (isVideo) whenVideo() else whenCamera()
        }
    }
}