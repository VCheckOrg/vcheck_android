package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.CreateVerificationRequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiClient {

    @POST("verifications")
    fun createVerificationRequest(@Body verificationRequestBody: CreateVerificationRequestBody)
        : Call<CreateVerificationAttemptResponse>
}