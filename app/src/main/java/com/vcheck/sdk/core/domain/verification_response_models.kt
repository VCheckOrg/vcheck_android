package com.vcheck.sdk.core.domain

import com.google.gson.annotations.SerializedName

data class CreateVerificationAttemptResponse(
    @SerializedName("data")
    val data: CreateVerificationAttemptData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class CreateVerificationAttemptData(
    @SerializedName("application_id")
    val applicationId: Int,
    @SerializedName("redirect_url")
    var redirectUrl: String?,
    @SerializedName("create_time")
    var createTime: String,
    @SerializedName("token")
    val token: String
)


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
    val returnUrl: String?,
    @SerializedName("theme")
    val theme: String?
)