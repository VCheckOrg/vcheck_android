package com.vcheck.sdk.core.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Patterns
import android.webkit.URLUtil
import androidx.core.content.ContextCompat.getSystemService
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern


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


private const val DATE_PATTERN = "((?:19|20)\\d\\d)-(0?[1-9]|1[012])-([12][0-9]|3[01]|0?[1-9])"
private val docDatePattern: Pattern = Pattern.compile(DATE_PATTERN)

fun isValidDocRelatedDate(date: String): Boolean {
    val commonDocMatcher: Matcher = docDatePattern.matcher(date)
    return if (commonDocMatcher.matches()) {
        commonDocMatcher.reset()
        if (commonDocMatcher.find()) {
            val year: Int = commonDocMatcher.group(1)!!.toInt()
            val month: String = commonDocMatcher.group(2)!!
            val day: String = commonDocMatcher.group(3)!!
            if (date == "31" && (month == "4" || month == "6" || month == "9" || month == "11" || month == "04" || month == "06" || month == "09")) {
                false
            } else if (month == "2" || month == "02") {
                if (year % 4 == 0) {
                    !(day == "30" || day == "31")
                } else {
                    !(day == "29" || day == "30" || day == "31")
                }
            } else {
                true
            }
        } else {
            false
        }
    } else {
        false
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


fun String.isValidHexColor(): Boolean {
    val rgbColorPattern = Pattern.compile("^#(?:[0-9a-fA-F]{3}){1,2}\$")
    val argbColorPattern = Pattern.compile("^#(?:[0-9a-fA-F]{3,4}){1,2}\$")
    return (rgbColorPattern.matcher(this).matches() || argbColorPattern.matcher(this).matches())
}


fun String.isValidUrl(): Boolean  {
    try {
        val url = URL(this)
        return URLUtil.isValidUrl(url.toString()) && Patterns.WEB_URL.matcher(url.toString()).matches()
    } catch (ignored: MalformedURLException) {
        throw ignored
    }
}

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024



/// FOR TEST
fun saveImageToGallery(bitmap: Bitmap, context: Context, folderName: String) {
    if (Build.VERSION.SDK_INT >= 29) {
        val values = contentValues()
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        // RELATIVE_PATH and IS_PENDING are introduced in API 29.

        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
            values.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, values, null, null)
        }
    } else {
        val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + folderName)
        // getExternalStorageDirectory is deprecated in API 29

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = System.currentTimeMillis().toString() + ".png"
        val file = File(directory, fileName)
        saveImageToStream(bitmap, FileOutputStream(file))
        val values = contentValues()
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        // .DATA is deprecated in API 29
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}

fun contentValues() : ContentValues {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    return values
}

fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


inline fun <T> List<T>.moveItemToFirstPosition(predicate: (T) -> Boolean): List<T> {
    for (element in this.withIndex()) {
        if (predicate(element.value)) {
            return this.toMutableList().apply {
                removeAt(element.index)
                add(0, element.value)
            }.toList()
        }
    }
    return this
}