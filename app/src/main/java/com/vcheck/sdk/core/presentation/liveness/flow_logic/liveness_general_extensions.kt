package com.vcheck.sdk.core.presentation.liveness.flow_logic

import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


fun getFrontFacingCameraId(cManager: CameraManager): String? {
    try {
        var cameraId: String?
        var cameraOrientation: Int
        var characteristics: CameraCharacteristics
        for (i in cManager.cameraIdList.indices) {
            cameraId = cManager.cameraIdList[i]
            characteristics = cManager.getCameraCharacteristics(cameraId)
            cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
            if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId
            }
        }
    } catch (e: CameraAccessException) {
        e.printStackTrace()
    }
    return null
}

fun VCheckLivenessActivity.createTempFileForBitmapFrame(mBitmap: Bitmap): String {
    var outStream: OutputStream? = null
    val file = File.createTempFile("${System.currentTimeMillis()}", ".jpg", this.cacheDir)
    return try {
        outStream = FileOutputStream(file)
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
        outStream.close()
        //Log.d("Liveness","----------- SAVED IMAGE: PATH: ${file.path}")
        file.path
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun VCheckLivenessActivity.createVideoFile(): File? {
    return try {
        val storageDir: File = this.cacheDir
        File.createTempFile(
            "faceVideo${System.currentTimeMillis()}", ".mp4", storageDir).apply {
            videoPath = this.path
        }
    } catch (e: IOException) {
        showSingleToast(e.message)
        null
    }
}

fun VCheckLivenessActivity.showSingleToast(message: String?) {
    if (mToast != null) {
        mToast?.cancel()
    }
    mToast = Toast.makeText(this, message, Toast.LENGTH_LONG)
    mToast?.show()
}

fun VCheckLivenessActivity.unMirrorBitmap(input: Bitmap): Bitmap? {
    val rotationMatrix = Matrix()
    rotationMatrix.setScale( -1F , 1F)

    return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
}

fun VCheckLivenessActivity.setUpMediaRecorder() {
    val videoFile = createVideoFile()
    mediaRecorder = MediaRecorder().apply {
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile(videoFile!!.path)
        setVideoEncodingBitRate(800000)
        setVideoFrameRate(30)
        setVideoSize(previewSize.width, previewSize.height)
        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        setOrientationHint(270)
    }
    try {
        mediaRecorder.prepare()
    } catch (e: java.lang.Exception) {
        Log.e(VCheckLivenessActivity.TAG, ("setUpMediaRecorder error: ${e.cause.toString()}" + e.message))
        e.printStackTrace()
        return
    }
}
