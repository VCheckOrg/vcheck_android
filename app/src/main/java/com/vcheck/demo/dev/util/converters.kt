package com.vcheck.demo.dev.util

import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

object StringFormatter {
    // convert UTF-8 to internal Java String format
//    fun convertUTF8ToString(s: String): String? {
//        var out: String? = null
//        out = try {
//            String(s.toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
//        } catch (e: UnsupportedEncodingException) {
//            return null
//        }
//        return out
//    }

    // convert internal Java String format to UTF-8
    fun convertStringToUTF8(s: String): String? {
        var out: String? = null
        out = try {
            String(s.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))
        } catch (e: UnsupportedEncodingException) {
            return null
        }
        return out
    }
}