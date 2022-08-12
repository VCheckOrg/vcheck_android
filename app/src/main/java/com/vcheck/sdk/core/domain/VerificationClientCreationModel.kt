package com.vcheck.sdk.core.domain

data class VerificationClientCreationModel(
    val partnerId: Int,
    val partnerSecret: String,
    var verificationType: VerificationSchemeType = VerificationSchemeType.FULL_CHECK,
    var partnerUserId: String? = null,
    var partnerVerificationId: String? = null,
    var sessionLifetime: Int? = null)

//    var customVerificationServiceURL: String? = null,
//    var customPartnerServiceURL: String? = null,