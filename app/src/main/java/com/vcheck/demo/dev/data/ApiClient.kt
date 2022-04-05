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
    fun initVerification(@Header("Authorization") verifToken: String) : Call<VerificationInitResponse>

    @GET("countries")
    fun getCountries(@Header("Authorization") verifToken: String) : Call<CountriesResponse>

    @GET("countries/{country}/documents")
    fun getCountryAvailableDocTypeInfo(@Header("Authorization") verifToken: String,
                                       @Path("country") countryId: Int)
                                        : Call<DocumentTypesForCountryResponse>

    @Multipart
    @POST("documents")
    fun uploadVerificationDocument(
        @Header("Authorization") verifToken: String,
        @Body documentUploadRequestBody: DocumentUploadRequestBody,
        @Part image: MultipartBody.Part
    ): Call<DocumentUploadResponse>
}