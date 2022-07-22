package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class VerificationResult(
    val status: Int,
    val reasonCode: String?,
    val reasonLocalized: String?
)

data class FinalVerifCheckResponseModel(
    @SerializedName("data")
    val data: FinalVerifCheckResponseData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class FinalVerifCheckResponseData(
    @SerializedName("status")
    val status: Int
)