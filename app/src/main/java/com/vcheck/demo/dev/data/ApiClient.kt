package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.CreateVerificationRequestBody
import com.vcheck.demo.dev.domain.VerificationInitResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiClient {

    @POST("verifications")
    fun createVerificationRequest(@Body verificationRequestBody: CreateVerificationRequestBody)
        : Call<CreateVerificationAttemptResponse>

    @PUT("verifications/init")
    fun initVerification(@Header("Authorization") verifToken: String) : Call<VerificationInitResponse>
}