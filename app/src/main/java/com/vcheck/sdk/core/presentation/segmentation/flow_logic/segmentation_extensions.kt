package com.vcheck.sdk.core.presentation.segmentation.flow_logic

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ImageReader
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.presentation.segmentation.VCheckSegmentationActivity
import com.vcheck.sdk.core.util.images.ImageUtils
import com.vcheck.sdk.core.util.fillBytes
import kotlinx.coroutines.launch
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


fun VCheckSegmentationActivity.onImageAvailableImpl(reader: ImageReader?) {
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
            scope.launch {  //!
                processImage()
            }
        } catch (e: Exception) {
            return
        }
    }
}


fun VCheckSegmentationActivity.getScreenOrientation(): Int {
    return 0
}


fun VCheckSegmentationActivity.createTempFileForBitmapFrame(mBitmap: Bitmap): String {
    val f3 = File(Environment.getExternalStorageDirectory().toString() + "/frames/")
    if (!f3.exists()) f3.mkdirs()
    var outStream: OutputStream? = null
    val file = File.createTempFile("${System.currentTimeMillis()}", ".jpg", this.cacheDir)
    return try {
        outStream = FileOutputStream(file)
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
        outStream.close()
        file.path
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun Bitmap.cropWithMask(): Bitmap {
    val maskDimens = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()!!.maskDimensions!!

    val originalWidth = this.width
    val originalHeight = this.height
    val desiredWidth = (originalWidth * (maskDimens.widthPercent / 100)).toInt()
    val desiredHeight = (desiredWidth * maskDimens.ratio).toInt()
    val cropHeightFromEachSide = ((originalHeight - desiredHeight) / 2)
    val cropWidthFromEachSide = ((originalWidth - desiredWidth) / 2)

    return Bitmap.createBitmap(
        this,
        cropWidthFromEachSide,
        cropHeightFromEachSide,
        desiredWidth,
        desiredHeight)
}


fun VCheckSegmentationActivity.rotateBitmap(input: Bitmap): Bitmap? {
    val rotationMatrix = Matrix()
    //rotationMatrix.setRotate(openLivenessCameraParams!!.sensorOrientation.toFloat())
    //Log.d("SEG", "SENSOR ORIENTATION: ${openLivenessCameraParams!!.sensorOrientation.toFloat()}")
    rotationMatrix.setRotate(90F)
    return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
}
