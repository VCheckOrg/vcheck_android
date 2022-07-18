package com.vcheck.demo.dev.presentation.liveness.flow_logic

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import com.vcheck.demo.dev.presentation.liveness.VCheckLivenessActivity
import com.vcheck.demo.dev.util.ImageUtils
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

fun fillBytes(
    planes: Array<Image.Plane>,
    yuvBytes: Array<ByteArray?>) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (i in planes.indices) {
        val buffer = planes[i].buffer
        if (yuvBytes[i] == null) {
            yuvBytes[i] = ByteArray(buffer.capacity())
        }
        buffer[yuvBytes[i]!!]
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
    val path = Environment.getExternalStorageDirectory().toString() + "/frames/" + "${System.currentTimeMillis()}" + ".jpg" //!
    val file = File(path)
    return try {
        outStream = FileOutputStream(file)
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
        outStream.close()
        Log.d("Liveness","----------- SAVED IMAGE: PATH: $path")
        //Toast.makeText(applicationContext, "Saved", Toast.LENGTH_LONG).show()
        path
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        path
    }
}


//TODO rotate image if image captured on samsung devices (?)
//Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
fun VCheckLivenessActivity.rotateBitmap(input: Bitmap): Bitmap? {
    val rotationMatrix = Matrix()
    rotationMatrix.setRotate(openLivenessCameraParams!!.sensorOrientation.toFloat())
    return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
}
