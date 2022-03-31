package com.vcheck.demo.util

import android.util.Log
import java.security.MessageDigest


fun generateSHA256Hash(strToHash: String): String {
    Log.d("VERIF_ATTEMPT", "FULL STR TO HASH : $strToHash")
    val bytes = strToHash.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}