package com.vcheck.sdk.core.domain

import com.google.gson.annotations.SerializedName

//TODO rename to VerificationCheckResult
data class VerificationResult(
    val isFinalizedAndSuccessful: Boolean,
    val isFinalizedAndFailed: Boolean,
    val isWaitingForManualCheck: Boolean,
    val status: String,
    val scheme: String,
    val createdAt: String?,
    val finalizedAt: String?,
    val rejectionReasons: List<String>?
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
    val status: String,
    @SerializedName("is_success")
    val isSuccess: Boolean?,
    @SerializedName("scheme")
    val scheme: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("finalized_at")
    val finalizedAt: String?,
    @SerializedName("rejection_reasons")
    val rejectionReasons: List<String>?
)