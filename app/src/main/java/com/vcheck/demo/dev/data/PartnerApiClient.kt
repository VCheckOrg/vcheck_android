package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.CreateVerificationRequestBody
import com.vcheck.demo.dev.domain.FinalVerifCheckResponseModel
import retrofit2.Call
import retrofit2.http.*

interface PartnerApiClient {

    @POST("verifications")
    fun createVerificationRequest(@Body verificationRequestBody: CreateVerificationRequestBody)
            : Call<CreateVerificationAttemptResponse>

    @GET("verifications/{verification_id}")
    fun checkFinalVerificationStatus(
        @Header("Authorization") verifToken: String,
        @Path("verification_id") verifId: Int
    ) : Call<FinalVerifCheckResponseModel>
}