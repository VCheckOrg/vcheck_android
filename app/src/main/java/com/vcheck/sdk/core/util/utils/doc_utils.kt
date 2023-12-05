package com.vcheck.sdk.core.util.utils

import java.util.regex.Matcher
import java.util.regex.Pattern


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