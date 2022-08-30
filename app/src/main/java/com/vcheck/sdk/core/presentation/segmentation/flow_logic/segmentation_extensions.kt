package com.vcheck.sdk.core.presentation.segmentation.flow_logic

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ImageReader
import android.os.Environment
import android.util.Log
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.presentation.segmentation.VCheckSegmentationActivity
import com.vcheck.sdk.core.util.ImageUtils
import com.vcheck.sdk.core.util.fillBytes
import kotlinx.coroutines.launch
import java.io.File
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
        Log.d("Liveness","----------- SAVED IMAGE: PATH: ${file.path}")
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


//--------------

/// FOR TEST
//fun saveImageToGallery(bitmap: Bitmap, context: Context, folderName: String) {
//    if (android.os.Build.VERSION.SDK_INT >= 29) {
//        val values = contentValues()
//        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
//        values.put(MediaStore.Images.Media.IS_PENDING, true)
//        // RELATIVE_PATH and IS_PENDING are introduced in API 29.
//
//        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//        if (uri != null) {
//            saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
//            values.put(MediaStore.Images.Media.IS_PENDING, false)
//            context.contentResolver.update(uri, values, null, null)
//        }
//    } else {
//        val directory = File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
//        // getExternalStorageDirectory is deprecated in API 29
//
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//        val fileName = System.currentTimeMillis().toString() + ".png"
//        val file = File(directory, fileName)
//        saveImageToStream(bitmap, FileOutputStream(file))
//        val values = contentValues()
//        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
//        // .DATA is deprecated in API 29
//        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//    }
//}

//fun contentValues() : ContentValues {
//    val values = ContentValues()
//    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
//    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
//    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
//    return values
//}
//
//fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
//    if (outputStream != null) {
//        try {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            outputStream.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}

//    return when (windowManager.defaultDisplay.rotation) {
//        Surface.ROTATION_270 -> 270
//        Surface.ROTATION_180 -> 180
//        Surface.ROTATION_90 -> 90
//        else -> 0
//    }

