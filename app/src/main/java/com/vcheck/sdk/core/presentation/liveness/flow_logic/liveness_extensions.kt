package com.vcheck.sdk.core.presentation.liveness.flow_logic

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ImageReader
import android.os.Environment
import android.util.Log
import android.view.Surface
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.ImageUtils
import com.vcheck.sdk.core.util.fillBytes
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun VCheckLivenessActivity.onImageAvailableImpl(reader: ImageReader?) {
    // We need wait until we have some size from onPreviewSizeChosen
    openLivenessCameraParams?.apply {
        if (previewWidth == 0 || previewHeight == 0) {
            return
        }
        if (rgbBytes == null) {
            rgbBytes = IntArray(previewWidth * previewHeight)
        }
        try {
            val image = reader?.acquireLatestImage() ?: return
            if (isProcessingFrame) {
                image.close()
                return
            }
            isProcessingFrame = true
            val planes = image.planes
            fillBytes(planes, yuvBytes)
            yRowStride = planes[0].rowStride
            val uvRowStride = planes[1].rowStride
            val uvPixelStride = planes[1].pixelStride
            imageConverter = Runnable {
                ImageUtils.convertYUV420ToARGB8888(
                    yuvBytes[0]!!,
                    yuvBytes[1]!!,
                    yuvBytes[2]!!,
                    previewWidth,
                    previewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    rgbBytes!!
                )
            }
            postInferenceCallback = Runnable {
                image.close()
                isProcessingFrame = false
            }
            processImage()
        } catch (e: Exception) {
            return
        }
    }
}

fun VCheckLivenessActivity.getScreenOrientation(): Int {
    return when (windowManager.defaultDisplay.rotation) {
        Surface.ROTATION_270 -> 270
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_90 -> 90
        else -> 0
    }
}


fun VCheckLivenessActivity.createTempFileForBitmapFrame(mBitmap: Bitmap): String {
    val f3 = File(Environment.getExternalStorageDirectory().toString() + "/frames/")
    if (!f3.exists()) f3.mkdirs()
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


//TODO rotate image if image captured on samsung devices (?)
//Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
fun VCheckLivenessActivity.rotateBitmap(input: Bitmap): Bitmap? {
    val rotationMatrix = Matrix()
    rotationMatrix.setRotate(openLivenessCameraParams!!.sensorOrientation.toFloat())
    return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
}
