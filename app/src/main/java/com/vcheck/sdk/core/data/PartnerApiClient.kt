package com.vcheck.sdk.core.data

import com.vcheck.sdk.core.domain.CreateVerificationAttemptResponse
import com.vcheck.sdk.core.domain.CreateVerificationRequestBody
import com.vcheck.sdk.core.domain.FinalVerifCheckResponseModel
import retrofit2.Call
import retrofit2.http.*

interface PartnerApiClient {

    @POST("verifications")
    fun createVerificationRequest(@Body verificationRequestBody: CreateVerificationRequestBody)
            : Call<CreateVerificationAttemptResponse>

    @GET("verifications/{verification_id}")
    fun checkFinalVerificationStatus(
        @Header("Authorization") verifToken: String,
        @Path("verification_id") verifId: Int,
        @Query("partner_id") partnerId: Int,
        @Query("timestamp") timestamp: Int,
        @Query("sign") sign: String
    ) : Call<FinalVerifCheckResponseModel>
}