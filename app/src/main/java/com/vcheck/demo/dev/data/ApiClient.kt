package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {

    //TODO should be removed with new architecture!
    @POST("verifications")
    fun createVerificationRequest(@Body verificationRequestBody: CreateVerificationRequestBody)
            : Call<CreateVerificationAttemptResponse>

    @PUT("verifications/init")
    fun initVerification(@Header("Authorization") verifToken: String): Call<VerificationInitResponse>

    @GET("countries")
    fun getCountries(@Header("Authorization") verifToken: String): Call<CountriesResponse>

    @GET("countries/{country}/documents")
    fun getCountryAvailableDocTypeInfo(
        @Header("Authorization") verifToken: String,
        @Path("country") countryCode: String
    ): Call<DocumentTypesForCountryResponse>

    @Headers("multipart: true")
    @Multipart
    @POST("documents") //TODO: change to POST /document/upload
    fun uploadVerificationDocumentsForOnePage(
        @Header("Authorization") verifToken: String,
        @Part photo1: MultipartBody.Part,
        @Part country: MultipartBody.Part,
        @Part document_type: MultipartBody.Part, // TODO rename to category = fields.Integer()
    ): Call<DocumentUploadResponse>

    @Headers("multipart: true")
    @Multipart
    @POST("documents") //TODO: change to POST /document/upload
    fun uploadVerificationDocumentsForTwoPages(
        @Header("Authorization") verifToken: String,
        @Part photo1: MultipartBody.Part,
        @Part photo2: MultipartBody.Part,
        @Part country: MultipartBody.Part,
        @Part document_type: MultipartBody.Part, // TODO rename to category = fields.Integer()
    ): Call<DocumentUploadResponse>

    @GET("documents/{document}") //TODO: change to GET documents/{document}/info
    fun getDocumentInfo(
        @Header("Authorization") verifToken: String,
        @Path("document") docId: Int
    ): Call<PreProcessedDocumentResponse>

    @PUT("documents/{document}")
    fun updateAndConfirmDocInfo(
        @Header("Authorization") verifToken: String,
        @Path("document") docId: Int,
        @Body parsedDocFieldsData: ParsedDocFieldsData
    ): Call<Response<Void>>

    @PUT("documents/{document}/primary")
    fun setDocumentAsPrimary(
        @Header("Authorization") verifToken: String,
        @Path("document") docId: Int) : Call<Response<Void>>

    @GET("timestamp")
    fun getServiceTimestamp() : Call<String>

    @Headers("multipart: true")
    @Multipart
    @POST("liveness")
    fun uploadLivenessVideo(
        @Header("Authorization") verifToken: String,
        @Part video: MultipartBody.Part
    ) : Call<LivenessUploadResponse>

    @GET("stage/current")
    fun getCurrentStage() : Call<StageResponse>
}