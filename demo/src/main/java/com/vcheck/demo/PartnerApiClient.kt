package com.vcheck.demo

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

    @POST("form/partner_request")
    fun sendPartnerApplicationRequest(
        @Body body: PartnerApplicationRequestData
    ) : Call<FinalVerifCheckResponseModel>
}
