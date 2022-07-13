package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class DocumentUploadRequestBody(
    val country: String = "code",
    val document_type: Int = 1,
    //val is_handwritten: Boolean? = null //deprecated field
)

data class DocUserDataRequestBody(
    @SerializedName("user_data")
    val user_data: ParsedDocFieldsData
)