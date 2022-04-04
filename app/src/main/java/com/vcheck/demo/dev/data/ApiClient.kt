package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    @POST("verifications")
    fun createVerificationRequest(@Body verificationRequestBody: CreateVerificationRequestBody)
            : Call<CreateVerificationAttemptResponse>

    @PUT("verifications/init")
    fun initVerification(@Header("Authorization") verifToken: String): Call<VerificationInitResponse>

    @Multipart
    @POST("documents")
    fun uploadVerificationDocument(
        @Header("Authorization") verifToken: String,
        @Body documentUploadRequestBody: DocumentUploadRequestBody,
        @Part image: MultipartBody.Part
    ): Call<DocumentUploadResponse>
}