package com.vcheck.sdk.core.domain

import com.google.gson.annotations.SerializedName

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
    @SerializedName("session_lifetime")
    val session_lifetime: Int,
    @SerializedName("sign")
    val sign: String
)