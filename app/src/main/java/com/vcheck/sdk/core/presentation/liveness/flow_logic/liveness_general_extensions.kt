package com.vcheck.sdk.core.presentation.liveness.flow_logic

import android.graphics.Bitmap
import android.widget.Toast
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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