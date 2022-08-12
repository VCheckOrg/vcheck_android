package com.vcheck.sdk.core.domain


//TODO duplicate for iOS!
enum class DocumentVerificationCode {
    VERIFICATION_NOT_INITIALIZED,// = 0
    USER_INTERACTED_COMPLETED,// = 1
    STAGE_NOT_FOUND,// = 2
    INVALID_STAGE_TYPE,// = 3
    PRIMARY_DOCUMENT_EXISTS,// = 4
    UPLOAD_ATTEMPTS_EXCEEDED,// = 5
    INVALID_DOCUMENT_TYPE,// = 6
    INVALID_PAGES_COUNT,// = 7
    INVALID_FILES,// = 8
    PHOTO_TOO_LARGE,// = 9
    PARSING_ERROR,// = 10
    INVALID_PAGE // = 11
}

fun DocumentVerificationCode.toCodeIdx(): Int {
    return when(this) {
        DocumentVerificationCode.VERIFICATION_NOT_INITIALIZED -> 0
        DocumentVerificationCode.USER_INTERACTED_COMPLETED -> 1
        DocumentVerificationCode.STAGE_NOT_FOUND -> 2
        DocumentVerificationCode.INVALID_STAGE_TYPE -> 3
        DocumentVerificationCode.PRIMARY_DOCUMENT_EXISTS -> 4
        DocumentVerificationCode.UPLOAD_ATTEMPTS_EXCEEDED -> 5
        DocumentVerificationCode.INVALID_DOCUMENT_TYPE -> 6
        DocumentVerificationCode.INVALID_PAGES_COUNT -> 7
        DocumentVerificationCode.INVALID_FILES -> 8
        DocumentVerificationCode.PHOTO_TOO_LARGE -> 9
        DocumentVerificationCode.PARSING_ERROR -> 10
        DocumentVerificationCode.INVALID_PAGE -> 11
    }
}

fun codeIdxToVerificationCode(codeIdx: Int)
    : DocumentVerificationCode {
    return when(codeIdx) {
        0 -> DocumentVerificationCode.VERIFICATION_NOT_INITIALIZED
        1 -> DocumentVerificationCode.USER_INTERACTED_COMPLETED
        2 -> DocumentVerificationCode.STAGE_NOT_FOUND
        3 -> DocumentVerificationCode.INVALID_STAGE_TYPE
        4 -> DocumentVerificationCode.PRIMARY_DOCUMENT_EXISTS
        5 -> DocumentVerificationCode.UPLOAD_ATTEMPTS_EXCEEDED
        6 -> DocumentVerificationCode.INVALID_DOCUMENT_TYPE
        7 -> DocumentVerificationCode.INVALID_PAGES_COUNT
        8 -> DocumentVerificationCode.INVALID_FILES
        9 -> DocumentVerificationCode.PHOTO_TOO_LARGE
        10 -> DocumentVerificationCode.PARSING_ERROR
        else -> DocumentVerificationCode.INVALID_PAGE
    }
}