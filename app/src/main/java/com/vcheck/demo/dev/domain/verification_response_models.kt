package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

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
