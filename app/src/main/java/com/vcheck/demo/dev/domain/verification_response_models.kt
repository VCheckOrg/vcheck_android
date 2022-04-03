package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName
import com.google.protobuf.Api

data class CreateVerificationAttemptResponse(
    val data: CreateVerificationAttemptData
)

data class CreateVerificationAttemptData(
    @SerializedName("application_id")
    val applicationId: Int,
    @SerializedName("redirect_url")
    var redirectUrl: String,
    @SerializedName("create_time")
    var createTime: String)


data class VerificationInitResponse(
    val data: VerificationInitResponseData
)

data class VerificationInitResponseData(
    @SerializedName("stage")
    val stage: Int,
    @SerializedName("document")
    val document: Int,
    @SerializedName("locale")
    val locale: String,
    @SerializedName("return_url")
    val returnUrl: String
)