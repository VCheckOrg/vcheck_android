package com.vcheck.demo.dev.util

import android.util.Log
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

