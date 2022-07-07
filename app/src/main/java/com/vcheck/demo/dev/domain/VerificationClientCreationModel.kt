package com.vcheck.demo.dev.domain

data class VerificationClientCreationModel(
    val partnerId: Int,
    val partnerSecret: String,
    var verificationType: VerificationSchemeType = VerificationSchemeType.FULL_CHECK,
    var partnerUserId: String? = null,
    var partnerVerificationId: String? = null,
    var customServiceURL: String? = null,
    var sessionLifetime: Int? = null)