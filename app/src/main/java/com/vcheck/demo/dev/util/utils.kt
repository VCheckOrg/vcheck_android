package com.vcheck.demo.dev.util

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity
import java.io.File
import java.security.MessageDigest
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max


fun generateSHA256Hash(strToHash: String): String {

    val bytes = strToHash.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}


// Vibrates the device for 100 milliseconds.
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


fun getCPUCoreNum(): Int {
    try {
        val pattern = Pattern.compile("cpu[0-9]+")
        val cpuNum = max(
            File("/sys/devices/system/cpu/")
                .walk()
                .maxDepth(1)
                .count { pattern.matcher(it.name).matches() },
            Runtime.getRuntime().availableProcessors())
        Log.d("PERFORMANCE", "================ CPU NUM: $cpuNum")
        return cpuNum
    } catch (e: Exception) {
        Log.d("PERFORMANCE", "================ CAUGHT EXCEPTION WHILE REQUESTING CPU NUM! RETURNING 1")
        return 3
    }
}

fun LivenessActivity.getAvailableDeviceRAM(): Long {
    val mi = ActivityManager.MemoryInfo()
    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
    activityManager!!.getMemoryInfo(mi)
    val memInMB = mi.totalMem / (1024 * 1024)
    Log.d("PERFORMANCE", "================ MEM IN MB : ${bytesToHuman(mi.totalMem)}")
    return memInMB
}

fun LivenessActivity.shouldDecreaseVideoStreamQuality(): Boolean {
    return (getCPUCoreNum() < 3 || getAvailableDeviceRAM() < 1000)
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
