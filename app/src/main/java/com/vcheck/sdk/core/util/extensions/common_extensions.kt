package com.vcheck.sdk.core.util.extensions

import android.content.Context
import android.util.Patterns
import android.util.TypedValue
import android.webkit.URLUtil
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.regex.Pattern

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024

fun Int.dpToPx(context: Context): Int {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}

fun String.toFlagEmoji(): String {
    // 1. First check if the string consists of only 2 characters: ISO 3166-1
    // alpha-2 two-letter country codes (https://en.wikipedia.org/wiki/Regional_Indicator_Symbol).
    if (this.length != 2) {
        return this
    }
    val countryCodeCaps =
        this.uppercase(Locale.getDefault()) // upper case is important because we are calculating offset
    val firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6

    // 2. Then check if both characters are alphabet
    if (!countryCodeCaps[0].isLetter() || !countryCodeCaps[1].isLetter()) {
        return this
    }

    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
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