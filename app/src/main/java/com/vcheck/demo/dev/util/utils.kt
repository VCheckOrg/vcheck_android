package com.vcheck.demo.dev.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import java.io.File
import java.security.MessageDigest
import java.util.*

fun generateSHA256Hash(strToHash: String): String {

    Log.d("VERIF_ATTEMPT", "FULL STR TO HASH : $strToHash")

    val bytes = strToHash.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val result = digest.fold("") { str, it -> str + "%02x".format(it) }

    Log.d("VERIF_ATTEMPT", "FIRST HASH : $result")

    return result
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
    if (file.isDirectory) {
        for (child in file.listFiles()) {
            size += getFolderSize(child)
        }
    } else {
        size = file.length()
    }
    return size
}




//class CustomInputFilter : InputFilter {
//
//    private var regex = Pattern.compile("^[A-Z0-9]*$")
//
//    fun setRegex(customRegex: String) {
//        regex = Pattern.compile(customRegex)
//    }
//
//    override fun filter(
//        source: CharSequence,
//        start: Int,
//        end: Int,
//        dest: Spanned?,
//        dstart: Int,
//        dend: Int
//    ): CharSequence? {
//        val matcher = regex.matcher(source)
//        return if (matcher.find()) {
//            null
//        } else {
//            ""
//        }
//    }
//}

