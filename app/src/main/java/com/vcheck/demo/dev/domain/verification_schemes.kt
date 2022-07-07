package com.vcheck.demo.dev.domain

enum class VerificationSchemeType {
    FULL_CHECK,// = «full_check»
    DOCUMENT_UPLOAD_ONLY,// = «document_upload_only»
    LIVENESS_CHALLENGE_ONLY// = «liveness_challenge_only»
}

fun VerificationSchemeType.toStringRepresentation(): String {
    return this.name.lowercase()
}