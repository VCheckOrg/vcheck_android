package com.vcheck.sdk.core.presentation.segmentation.flow_logic

import android.graphics.Bitmap
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.presentation.segmentation.VCheckSegmentationActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

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

    //Log.d(VCheckSegmentationActivity.TAG, "CROPPING BITMAP: W - $originalWidth | H - $originalHeight")

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