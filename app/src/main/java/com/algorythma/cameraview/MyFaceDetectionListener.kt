package com.algorythma.cameraview

import android.hardware.Camera
import android.util.Log

class MyFaceDetectionListener : Camera.FaceDetectionListener {
    override fun onFaceDetection(faces: Array<out Camera.Face>?, p1: Camera?) {
        if (faces == null) return
        Log.d("Face Detection", "face detected: " + faces.size)
        faces.forEach {
            Log.d("Face Detection", "Location X: " + it.rect.centerX() + " Location Y: " + it.rect.centerY())
        }
    }
}