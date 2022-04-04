package com.vcheck.demo.dev.domain

data class DocumentUploadRequestBody(
    val country: String = "code",
    val document_type: Int = 1,
    val is_handwritten: Boolean = false
)