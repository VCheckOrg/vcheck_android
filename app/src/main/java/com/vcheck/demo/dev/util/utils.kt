package com.vcheck.demo.dev.util

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class CustomInputFilter : InputFilter {

    private var regex = Pattern.compile("^[A-Z0-9]*$")

    fun setRegex(customRegex: String) {
        regex = Pattern.compile(customRegex)
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val matcher = regex.matcher(source)
        return if (matcher.find()) {
            null
        } else {
            ""
        }
    }
}