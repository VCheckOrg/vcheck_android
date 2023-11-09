package com.vcheck.sdk.core.domain

import com.google.gson.annotations.SerializedName

data class VerificationInitResponse(
    @SerializedName("data")
    val data: VerificationInitResponseData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class VerificationInitResponseData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("locale")
    val locale: String?,
    @SerializedName("return_url")
    val returnUrl: String?
    // removed "theme" property reading; not used in SDK ATM
)