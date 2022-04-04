package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class DocumentUploadResponse(
    val data: DocumentUploadResponseData
)

data class DocumentUploadResponseData(
    @SerializedName("status")
    val status: Int,
    @SerializedName("document")
    val document: Int
)