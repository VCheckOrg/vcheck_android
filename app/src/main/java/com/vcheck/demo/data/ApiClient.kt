package com.vcheck.demo.data

import com.vcheck.demo.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.domain.CreateVerificationRequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiClient {

    @POST("verifications")
    fun createVerificationRequest(@Body verificationRequestBody: CreateVerificationRequestBody)
        : Call<CreateVerificationAttemptResponse>
}