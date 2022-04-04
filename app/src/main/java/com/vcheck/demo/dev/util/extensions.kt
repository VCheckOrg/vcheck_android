package com.vcheck.demo.dev.util

import android.util.Log
import com.vcheck.demo.dev.util.StringFormatter.convertStringToUTF8
import java.security.MessageDigest


fun generateSHA256Hash(strToHash: String): String {

    Log.d("VERIF_ATTEMPT", "FULL STR TO HASH : $strToHash")

    val bytes = strToHash.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val result = digest.fold("") { str, it -> str + "%02x".format(it) }

    Log.d("VERIF_ATTEMPT", "FIRST HASH : $result")

    return result

//    try {
//        //val md = MessageDigest.getInstance("SHA-256")
//        val hashedBytes = md.digest(strToHash.toByteArray())
//        val output = StringBuilder(hashedBytes.size)
//        for (i in hashedBytes.indices) {
//            var hex = Integer.toHexString(0xFF and hashedBytes[i].toInt())
//            if (hex.length == 1) hex = "0$hex"
//            output.append(hex)
//        }
//        Log.d("VERIF_ATTEMPT", "SECOND HASH : $output")
//        return output.toString()
//    } catch (e: NoSuchAlgorithmException) {
//        e.printStackTrace()
//    }
//
//    return ""
}