package com.vcheck.sdk.core.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.media.Image
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import java.io.File
import java.security.MessageDigest
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


fun generateSHA256Hash(strToHash: String): String {

    val bytes = strToHash.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}


// Vibrates the device for 100 milliseconds.
// Vibrate Permission is already in Manifest
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

fun VCheckLivenessActivity.getAvailableDeviceRAM(): Long {
    val mi = ActivityManager.MemoryInfo()
    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
    activityManager!!.getMemoryInfo(mi)
    val memInMB = mi.totalMem / (1024 * 1024)
    return memInMB
}

fun VCheckLivenessActivity.shouldDecreaseVideoStreamQuality(): Boolean {
    return (getAvailableDeviceRAM() <= 2000)
}

private fun floatForm(d: Double): String {
    return String.format(Locale.US, "%.2f", d)
}

private fun bytesToHuman(size: Long): String {
    val Kb: Long = 1024
    val Mb = Kb * 1024
    val Gb = Mb * 1024
    val Tb = Gb * 1024
    val Pb = Tb * 1024
    val Eb = Pb * 1024
    if (size < Kb) return floatForm(size.toDouble()) + " byte"
    if (size in Kb until Mb) return floatForm(size.toDouble() / Kb) + " KB"
    if (size in Mb until Gb) return floatForm(size.toDouble() / Mb) + " MB"
    if (size in Gb until Tb) return floatForm(size.toDouble() / Gb) + " GB"
    if (size in Tb until Pb) return floatForm(size.toDouble() / Tb) + " TB"
    if (size in Pb until Eb) return floatForm(size.toDouble() / Pb) + " Pb"
    return if (size >= Eb) floatForm(size.toDouble() / Eb) + " Eb" else "0"
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

fun String.matchesURL(): Boolean {
    val regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    return Pattern.matches(regex, this)
}

fun String.isValidHexColor(): Boolean {
    val rgbColorPattern = Pattern.compile("^#(?:[0-9a-fA-F]{3}){1,2}\$")
    val argbColorPattern = Pattern.compile("^#(?:[0-9a-fA-F]{3,4}){1,2}\$")
        //"#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})")
    return (rgbColorPattern.matcher(this).matches() || argbColorPattern.matcher(this).matches())
}


///**
// * This method converts dp unit to equivalent pixels, depending on device density.
// *
// * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
// * @param context Context to get resources and device specific display metrics
// * @return A float value to represent px equivalent to dp depending on device density
// */
//fun Float.dpToPixels(context: Context): Float {
//    return this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
//}
//
///**
// * This method converts device specific pixels to density independent pixels.
// *
// * @param px A value in px (pixels) unit. Which we need to convert into db
// * @param context Context to get resources and device specific display metrics
// * @return A float value to represent dp equivalent to px value
// */
//fun Float.pixelsToDp(context: Context): Float {
//    return this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
//}