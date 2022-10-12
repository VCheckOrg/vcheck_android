package com.vcheck.sdk.core.presentation.segmentation.flow_logic

import android.graphics.Bitmap
import android.graphics.Matrix
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.presentation.segmentation.VCheckSegmentationActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun VCheckSegmentationActivity.getScreenOrientation(): Int {
    return 0
}


fun VCheckSegmentationActivity.createTempFileForBitmapFrame(mBitmap: Bitmap): String {
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
