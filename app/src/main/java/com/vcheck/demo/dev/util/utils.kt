package com.vcheck.demo.dev.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import java.io.File
import java.security.MessageDigest
import java.util.regex.Matcher
import java.util.regex.Pattern

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



//private val foreignDocsAndIDsDatePattern: Pattern = Pattern.compile(DATE_PATTERN_1)
//private const val DATE_PATTERN_1 = "(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((19|20)\\d\\d)"
//fun isValidForeignDocOrIDCardDate(date: String): Boolean {
//    val foreignDocsAndIDsDateMatcher: Matcher = foreignDocsAndIDsDatePattern.matcher(date)
//    return if (foreignDocsAndIDsDateMatcher.matches()) {
//        foreignDocsAndIDsDateMatcher.reset()
//        if (foreignDocsAndIDsDateMatcher.find()) {
//            val day: String = foreignDocsAndIDsDateMatcher.group(1)!!
//            val month: String = foreignDocsAndIDsDateMatcher.group(2)!!
//            val year: Int = foreignDocsAndIDsDateMatcher.group(3)!!.toInt()
//            if (date == "31" && (month == "4" || month == "6" || month == "9" || month == "11" || month == "04" || month == "06" || month == "09")) {
//                false
//            } else if (month == "2" || month == "02") {
//                if (year % 4 == 0) {
//                    !(day == "30" || day == "31")
//                } else {
//                    !(day == "29" || day == "30" || day == "31")
//                }
//            } else {
//                true
//            }
//        } else {
//            false
//        }
//    } else {
//        false
//    }
//}

//val commonInnerDocDateMatcher: Matcher = commonInnerDocDatePattern.matcher(date)


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

