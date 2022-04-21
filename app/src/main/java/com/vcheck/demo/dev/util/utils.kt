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

/**
 * This method is to change the country code like "us" into flag emoji.
 * Stolen from https://stackoverflow.com/a/35849652/75579
 * 1. It first checks if the string consists of only 2 characters:
 * ISO 3166-1 alpha-2 two-letter country codes (https://en.wikipedia.org/wiki/Regional_Indicator_Symbol).
 * 2. It then checks if both characters are alphabet
 * do nothing if it doesn't fulfil the 2 checks
 * caveat: if you enter an invalid 2 letter country code, say "XX",
 * it will pass the 2 checks, and it will return unknown result
 */
fun String.toFlagEmoji(): String {
    // 1. It first checks if the string consists of only 2 characters: ISO 3166-1
    // alpha-2 two-letter country codes (https://en.wikipedia.org/wiki/Regional_Indicator_Symbol).
    if (this.length != 2) {
        return this
    }
    val countryCodeCaps =
        this.uppercase(Locale.getDefault()) // upper case is important because we are calculating offset
    val firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6

    // 2. It then checks if both characters are alphabet
    if (!countryCodeCaps[0].isLetter() || !countryCodeCaps[1].isLetter()) {
        return this
    }

    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
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

