package com.vcheck.sdk.core.presentation.liveness.flow_logic

import android.graphics.Bitmap
import android.os.Environment
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


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


////TODO rotate image if image captured on samsung devices (?)
////Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
//fun VCheckLivenessActivity.rotateBitmap(input: Bitmap): Bitmap? {
//    val rotationMatrix = Matrix()
//    rotationMatrix.setRotate(openLivenessCameraParams!!.sensorOrientation.toFloat())
//    return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
//}
