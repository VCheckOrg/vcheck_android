package com.vcheck.sdk.core.util.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.ColorUtils
import java.io.File


@SuppressLint("MissingPermission")
fun vibrateDevice(context: Context, duration: Long) {
    val vibrator = getSystemService(context, Vibrator::class.java)
    vibrator?.let {
        if (Build.VERSION.SDK_INT >= 26) {
            it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(100)
        }
    }
}


fun getFolderSizeLabel(file: File): String {
    val size = getFolderSize(file).toDouble() / 1000.0 // Get size and convert bytes into KB.
    return if (size >= 1024) {
        (size / 1024).toString() + " MB"
    } else {
        "$size KB"
    }
}


fun getFolderSize(file: File): Long {
    var size: Long = 0
    if (file.isDirectory && file.listFiles() != null && file.listFiles()!!.isNotEmpty()) {
        for (child in file.listFiles()!!) {
            size += getFolderSize(child)
        }
    } else {
        size = file.length()
    }
    return size
}


fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray?>) {
    for (i in planes.indices) {
        val buffer = planes[i].buffer
        if (yuvBytes[i] == null) {
            yuvBytes[i] = ByteArray(buffer.capacity())
        }
        buffer[yuvBytes[i]!!]
    }
}


fun isColorDark(color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) < 0.5
}