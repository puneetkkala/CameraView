package com.algorythma.cameraview

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView

class CameraView(context: Context, private val mCamera: Camera) : SurfaceView(context), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder

    init {
        mHolder.addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder)
            mCamera.startPreview()
            startFaceDetection()
        } catch (e: Exception) {
        }

    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        if (mHolder.surface == null) return
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
        }

        try {
            mCamera.setPreviewDisplay(mHolder)
            mCamera.startPreview()
            startFaceDetection()
        } catch (e: Exception) {
        }
    }

    private fun startFaceDetection() {
        if (mCamera.parameters.maxNumDetectedFaces > 0) mCamera.startFaceDetection()
    }
}