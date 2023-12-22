package com.vcheck.demo

import com.google.gson.annotations.SerializedName
import com.vcheck.sdk.core.domain.VerificationSchemeType

data class PartnerApplicationRequestData(
    @SerializedName("company")
    val company: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String
)

data class CreateVerificationAttemptResponse(
    @SerializedName("data")
    val data: CreateVerificationAttemptData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class CreateVerificationAttemptData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("url")
    var url: String?,
    @SerializedName("token")
    val token: String
)

data class CreateVerificationRequestBody(
    @SerializedName("partner_id")
    val partner_id: Int,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("scheme")
    val scheme: String,
    @SerializedName("locale")
    val locale: String,
    @SerializedName("partner_user_id")
    val partner_user_id: String,
    @SerializedName("partner_verification_id")
    val partner_verification_id: String,
    @SerializedName("callback_url")
    val callback_url: String,
    @SerializedName("sign")
    val sign: String
)

data class VerificationClientCreationModel(
    val partnerId: Int,
    val partnerSecret: String,
    var verificationType: VerificationSchemeType = VerificationSchemeType.FULL_CHECK,
    var partnerUserId: String? = null,
    var partnerVerificationId: String? = null,
    var sessionLifetime: Int? = null)


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
