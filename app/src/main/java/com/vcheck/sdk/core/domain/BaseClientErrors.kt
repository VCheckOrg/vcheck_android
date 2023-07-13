package com.vcheck.sdk.core.domain

object BaseClientErrors {
    const val NO_TOKEN_AVAILABLE_STR = "Client error: No token available"

    const val VERIFICATION_NOT_INITIALIZED = 0
    const val USER_INTERACTED_COMPLETED = 1
    const val STAGE_NOT_FOUND = 2
    const val INVALID_STAGE_TYPE = 3
    const val PRIMARY_DOCUMENT_EXISTS = 4
    const val UPLOAD_ATTEMPTS_EXCEEDED = 5
    const val INVALID_DOCUMENT_TYPE = 6
    const val INVALID_PAGES_COUNT = 7
    const val INVALID_FILES = 8
    const val PHOTO_TOO_LARGE = 9
    const val PARSING_ERROR = 10
    const val INVALID_PAGE = 11
}



