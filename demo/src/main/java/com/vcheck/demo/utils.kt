package com.vcheck.demo

import android.app.Activity
import android.view.WindowManager
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import java.security.MessageDigest

fun generateSHA256Hash(strToHash: String): String {

    val bytes = strToHash.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun Activity.changeDemoActivityStatusBarColor(color: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isColorDark(color)
}

fun isColorDark(color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) < 0.5
}