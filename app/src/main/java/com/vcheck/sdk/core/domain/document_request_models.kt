package com.vcheck.sdk.core.domain

import com.google.gson.annotations.SerializedName

data class DocumentUploadRequestBody(
    val country: String,
    val document_type: Int,
    val manual: Boolean
)

data class DocUserDataRequestBody(
    @SerializedName("user_data")
    val user_data: ParsedDocFieldsData,
    @SerializedName("is_forced")
    val isForced: Boolean
)