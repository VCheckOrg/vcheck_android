package com.vcheck.sdk.core.domain

enum class InitProviderResponseCode {
    VERIFICATION_NOT_INITIALIZED,
    PROVIDER_NOT_FOUND,
    COUNTRY_NOT_FOUND,
    PROVIDER_ALREADY_INITIALIZED
}

fun InitProviderResponseCode.toCodeIdx(): Int {
    return when(this) {
        InitProviderResponseCode.VERIFICATION_NOT_INITIALIZED -> 0
        InitProviderResponseCode.PROVIDER_NOT_FOUND -> 1
        InitProviderResponseCode.COUNTRY_NOT_FOUND -> 2
        InitProviderResponseCode.PROVIDER_ALREADY_INITIALIZED -> 3
    }
}

fun codeIdxToInitVerificationCode(codeIdx: Int): InitProviderResponseCode {
    return when(codeIdx) {
        0 -> InitProviderResponseCode.VERIFICATION_NOT_INITIALIZED
        1 -> InitProviderResponseCode.PROVIDER_NOT_FOUND
        2 -> InitProviderResponseCode.COUNTRY_NOT_FOUND
        3 -> InitProviderResponseCode.PROVIDER_ALREADY_INITIALIZED
        else -> InitProviderResponseCode.VERIFICATION_NOT_INITIALIZED
    }
}